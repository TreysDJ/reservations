package spring.reservations.reservations;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.EntityExchangeResult;
import spring.reservations.BaseIntegrationTest;
import tools.jackson.core.type.TypeReference;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReservationIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private RoomRepository roomRepository;

    @Test
    void shouldCreateReservationAndListIt() {
        ReservationCreateRequestDTO request = createReservationRequest();
        Reservation created = postReservation(request);

        ZonedDateTime expectedStart = request.startDate()
                .atTime(HotelConstants.CHECK_IN_TIME)
                .atZone(HotelConstants.HOTEL_ZONE);
        ZonedDateTime expectedEnd = request.endDate()
                .atTime(HotelConstants.CHECK_OUT_TIME)
                .atZone(HotelConstants.HOTEL_ZONE);

        assertThat(created).isNotNull();
        assertThat(created.id()).isPositive();
        assertThat(created.userId()).isEqualTo(request.userId());
        assertThat(created.roomId()).isEqualTo(request.roomId());
        assertThat(created.startDate()).isEqualTo(expectedStart);
        assertThat(created.endDate()).isEqualTo(expectedEnd);
        assertThat(created.status()).isEqualTo(ReservationStatus.PENDING);

        var listResponse = restClient.get()
                .uri("/reservation/list?userId={userId}&roomId={roomId}", request.userId(), request.roomId())
                .exchange()
                .returnResult(String.class);

        assertThat(listResponse.getStatus()).isEqualTo(HttpStatus.OK);

        List<Reservation> reservations = jsonMapper.readValue(
                listResponse.getResponseBody(), new TypeReference<List<Reservation>>() {}
        );

        assertThat(reservations).hasSize(1);

        Reservation reservation = reservations.getFirst();
        assertThat(reservation.id()).isEqualTo(created.id());
        assertThat(reservation.userId()).isEqualTo(request.userId());
        assertThat(reservation.roomId()).isEqualTo(request.roomId());
        assertThat(reservation.startDate()).isEqualTo(expectedStart);
        assertThat(reservation.endDate()).isEqualTo(expectedEnd);
        assertThat(reservation.status()).isEqualTo(ReservationStatus.PENDING);
    }

    @Test
    void shouldntCreatExistingReservation() {
        ReservationCreateRequestDTO request = createReservationRequest();
        postReservation(request);

        var duplicate = sendCreateRequest(request);
        assertThat(duplicate.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldntCreateReservationWithIncorrectDate() {
        RoomEntity room = createRoom(nextRoomNumber());
        LocalDate startDate = LocalDate.now(HotelConstants.HOTEL_ZONE).plusDays(10);
        LocalDate endDate = startDate.minusDays(1);

        ReservationCreateRequestDTO invalidRequest = new ReservationCreateRequestDTO(
                1001L, room.getRoomId(), startDate, endDate
        );

        var response = sendCreateRequest(invalidRequest);
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldGetReservationById() {
        Reservation created = postReservation();

        var getResponse = restClient.get()
                .uri("/reservation/{id}", created.id())
                .exchange()
                .returnResult(String.class);

        assertThat(getResponse.getStatus()).isEqualTo(HttpStatus.OK);
        Reservation reservation = jsonMapper.readValue(getResponse.getResponseBody(), Reservation.class);

        assertThat(reservation.id()).isEqualTo(created.id());
        assertThat(reservation.userId()).isEqualTo(created.userId());
        assertThat(reservation.roomId()).isEqualTo(created.roomId());
        assertThat(reservation.startDate()).isEqualTo(created.startDate());
        assertThat(reservation.endDate()).isEqualTo(created.endDate());
        assertThat(reservation.status()).isEqualTo(ReservationStatus.PENDING);
    }

    @Test
    void shouldntGetReservationByNotExistingId() {
        var getResponse = restClient.get()
                .uri("/reservation/{id}", 9999)
                .exchange()
                .returnResult(String.class);

        assertThat(getResponse.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldCancelReservation() {
        Reservation created = postReservation();
        assertThat(created.status()).isEqualTo(ReservationStatus.PENDING);

        var cancelResponse = restClient.delete()
                .uri("/reservation/{id}/cancel", created.id())
                .exchange()
                .returnResult(String.class);

        assertThat(cancelResponse.getStatus()).isEqualTo(HttpStatus.OK);

        var getResponse = restClient.get()
                .uri("/reservation/{id}", created.id())
                .exchange()
                .returnResult(String.class);

        assertThat(getResponse.getStatus()).isEqualTo(HttpStatus.OK);
        Reservation cancelled = jsonMapper.readValue(getResponse.getResponseBody(), Reservation.class);

        assertThat(cancelled.status()).isEqualTo(ReservationStatus.CANCELLED);
    }

    @Test
    void shouldApproveReservation() {
        Reservation created = postReservation();
        assertThat(created.status()).isEqualTo(ReservationStatus.PENDING);

        var approveResponse = restClient.post()
                .uri("/reservation/{id}/approve", created.id())
                .exchange()
                .returnResult(String.class);

        assertThat(approveResponse.getStatus()).isEqualTo(HttpStatus.OK);

        var getResponse = restClient.get()
                .uri("/reservation/{id}", created.id())
                .exchange()
                .returnResult(String.class);

        assertThat(getResponse.getStatus()).isEqualTo(HttpStatus.OK);
        Reservation approved = jsonMapper.readValue(getResponse.getResponseBody(), Reservation.class);

        assertThat(approved.status()).isEqualTo(ReservationStatus.APPROVED);
    }

    private Reservation postReservation() {
        return postReservation(createReservationRequest());
    }

    private Reservation postReservation(ReservationCreateRequestDTO request) {
        var createResponse = sendCreateRequest(request);
        assertThat(createResponse.getStatus()).isEqualTo(HttpStatus.CREATED);
        return jsonMapper.readValue(createResponse.getResponseBody(), Reservation.class);
    }

    private EntityExchangeResult<String> sendCreateRequest(ReservationCreateRequestDTO request) {
        return restClient.post()
                .uri("/reservation/create")
                .contentType(MediaType.APPLICATION_JSON)
                .body(jsonMapper.writeValueAsString(request))
                .exchange()
                .returnResult(String.class);
    }

    private ReservationCreateRequestDTO createReservationRequest() {
        long userId = 1001L;
        RoomEntity room = createRoom(nextRoomNumber());
        LocalDate startDate = LocalDate.now(HotelConstants.HOTEL_ZONE).plusDays(10);
        LocalDate endDate = startDate.plusDays(2);
        return new ReservationCreateRequestDTO(userId, room.getRoomId(), startDate, endDate);
    }

    private RoomEntity createRoom(long roomNumber) {
        return roomRepository.save(new RoomEntity(null, roomNumber));
    }
}
