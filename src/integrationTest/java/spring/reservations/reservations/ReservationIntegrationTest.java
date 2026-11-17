package spring.reservations.reservations;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import spring.reservations.BaseIntegrationTest;
import tools.jackson.core.type.TypeReference;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReservationIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private RoomRepository roomRepository;

    @Test
    void shouldCreateReservationAndListIt() {
        long userId = 1001L;
        Room room = createRoom(nextRoomNumber());
        long roomId = room.getRoomId();
        LocalDate startDate = LocalDate.now().plusDays(10);
        LocalDate endDate = startDate.plusDays(2);
        Reservation request = new Reservation(null, userId, roomId, startDate, endDate, null);

        var createResponse = restClient.post()
                .uri("/reservation/create")
                .contentType(MediaType.APPLICATION_JSON)
                .body(jsonMapper.writeValueAsString(request))
                .exchange()
                .returnResult(String.class);

        assertThat(createResponse.getStatus()).isEqualTo(HttpStatus.CREATED);

        Reservation created = jsonMapper.readValue(createResponse.getResponseBody(), Reservation.class);

        assertThat(created).isNotNull();
        assertThat(created.id()).isPositive();
        assertThat(created.userId()).isEqualTo(userId);
        assertThat(created.roomId()).isEqualTo(roomId);
        assertThat(created.startDate()).isEqualTo(startDate);
        assertThat(created.endDate()).isEqualTo(endDate);
        assertThat(created.status()).isEqualTo(ReservationStatus.PENDING);

        var listResponse = restClient.get()
                .uri("/reservation/list?userId={userId}&roomId={roomId}", userId, roomId)
                .exchange()
                .returnResult(String.class);

        assertThat(listResponse.getStatus()).isEqualTo(HttpStatus.OK);

        List<Reservation> reservations = jsonMapper.readValue(
                listResponse.getResponseBody(), new TypeReference<List<Reservation>>() {}
        );

        assertThat(reservations).hasSize(1);

        Reservation reservation = reservations.getFirst();
        assertThat(reservation.id()).isEqualTo(created.id());
        assertThat(reservation.userId()).isEqualTo(userId);
        assertThat(reservation.roomId()).isEqualTo(roomId);
        assertThat(reservation.startDate()).isEqualTo(startDate);
        assertThat(reservation.endDate()).isEqualTo(endDate);
        assertThat(reservation.status()).isEqualTo(ReservationStatus.PENDING);
    }

    private Room createRoom(long roomNumber) {
        return roomRepository.save(new Room(roomNumber));
    }
}
