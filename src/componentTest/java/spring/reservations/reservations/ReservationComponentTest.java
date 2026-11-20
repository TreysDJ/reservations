package spring.reservations.reservations;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import spring.reservations.BaseComponentTest;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static spring.reservations.reservations.HotelConstants.CHECK_IN_TIME;
import static spring.reservations.reservations.HotelConstants.CHECK_OUT_TIME;
import static spring.reservations.reservations.HotelConstants.HOTEL_ZONE;

class ReservationComponentTest extends BaseComponentTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Test
    void shouldCreateReservations() {
        RoomEntity room = createRoom(nextRoomNumber());
        LocalDate arrivalDate = LocalDate.now(HOTEL_ZONE).plusDays(10);
        LocalDate departureDate = arrivalDate.plusDays(2);

        Reservation created = createReservation(
                1001L,
                room.getRoomId(),
                arrivalDate.minusDays(1).atTime(22, 0).atZone(ZoneOffset.UTC),
                departureDate.minusDays(1).atTime(22, 0).atZone(ZoneOffset.UTC)
        );

        assertThat(created.id()).isPositive();
        assertThat(created.startDate().getZone()).isEqualTo(HOTEL_ZONE);
        assertThat(created.startDate().toLocalDate()).isEqualTo(arrivalDate);
        assertThat(created.startDate().toLocalTime()).isEqualTo(CHECK_IN_TIME);
        assertThat(created.endDate().getZone()).isEqualTo(HOTEL_ZONE);
        assertThat(created.endDate().toLocalDate()).isEqualTo(departureDate);
        assertThat(created.endDate().toLocalTime()).isEqualTo(CHECK_OUT_TIME);

        ReservationEntity saved = reservationRepository.findById(created.id()).orElseThrow();
        ZonedDateTime expectedStart = arrivalDate.atTime(CHECK_IN_TIME).atZone(HOTEL_ZONE);
        ZonedDateTime expectedEnd = departureDate.atTime(CHECK_OUT_TIME).atZone(HOTEL_ZONE);

        assertThat(saved.getStartDate().toInstant()).isEqualTo(expectedStart.toInstant());
        assertThat(saved.getEndDate().toInstant()).isEqualTo(expectedEnd.toInstant());
    }

    @Test
    void shouldNotLetRequestedCheckoutTimeBlockNextGuest() {
        RoomEntity room = createRoom(nextRoomNumber());
        long roomId = room.getRoomId();
        LocalDate firstGuestArrivalDate = LocalDate.now(HOTEL_ZONE).plusDays(10);
        LocalDate firstGuestDepartureDate = firstGuestArrivalDate.plusDays(2);
        LocalDate secondGuestArrivalDate = firstGuestDepartureDate;
        LocalDate secondGuestDepartureDate = secondGuestArrivalDate.plusDays(2);

        // время отъезда первого клиента не должно влиять на заселение второго клиента
        Reservation firstGuest = createReservation(
                1001L,
                roomId,
                firstGuestArrivalDate.atTime(9, 0).atZone(ZoneOffset.UTC),
                firstGuestDepartureDate.atTime(18, 0).atZone(ZoneOffset.UTC)
        );

        Reservation secondGuest = createReservation(
                1002L,
                roomId,
                secondGuestArrivalDate.atTime(11, 0).atZone(ZoneOffset.UTC),
                secondGuestDepartureDate.atTime(18, 0).atZone(ZoneOffset.UTC)
        );

        assertThat(firstGuest.id()).isPositive();
        assertThat(secondGuest.id()).isPositive();
        assertThat(secondGuest.id()).isNotEqualTo(firstGuest.id());

        List<Reservation> reservations = reservationService.findAllByFilter(
                new ReservationsFindFilter(roomId, null, null, null)
        );
        assertThat(reservations)
                .extracting(Reservation::id)
                .containsExactlyInAnyOrder(firstGuest.id(), secondGuest.id());
    }

    @Test
    void shouldNotCreateReservationWithIncorrectDate() {
        ZonedDateTime startDate = ZonedDateTime.of(
                LocalDate.of(2030, 12, 15),
                LocalTime.of(14, 0),
                ZoneId.of("Europe/Moscow")
        );

        ZonedDateTime endDate = ZonedDateTime.of(
                LocalDate.of(2026, 11, 15),
                LocalTime.of(14, 0),
                ZoneId.of("Europe/Moscow")
        );

        Reservation request = new Reservation(null, 1001L, nextRoomNumber(), startDate, endDate, null);

        assertThatThrownBy(() -> reservationService.createReservation(request))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage("Start date must be earlier then end date");

        List<Reservation> reservations = reservationService.findAllByFilter(
                new ReservationsFindFilter(request.roomId(), request.id(), null, null)
        );
        assertThat(reservations).hasSize(0);
    }

    @Test
    void shouldApproveReservations() {
        RoomEntity room = createRoom(nextRoomNumber());
        Reservation created = createReservation(1001L, room.getRoomId());

        assertThat(created.status()).isEqualTo(ReservationStatus.PENDING);
        reservationService.approveReservation(created.id());

        Reservation approvedReservation = reservationService.getReservationById(created.id());
        assertThat(approvedReservation.status()).isEqualTo(ReservationStatus.APPROVED);
    }

    @Test
    void shouldCancelReservation() {
        RoomEntity room = createRoom(nextRoomNumber());
        Reservation created = createReservation(1001L, room.getRoomId());

        assertThat(created.status()).isEqualTo(ReservationStatus.PENDING);
        reservationService.cancelReservation(created.id());
        Reservation cancelledReservation = reservationService.getReservationById(created.id());
        assertThat(cancelledReservation.status()).isEqualTo(ReservationStatus.CANCELLED);
    }

    @Test
    void shouldNotApproveCancelledReservation() {
        RoomEntity room = createRoom(nextRoomNumber());
        Reservation created = createReservation(1001L, room.getRoomId());
        assertThat(created.status()).isEqualTo(ReservationStatus.PENDING);

        reservationService.cancelReservation(created.id());
        Reservation cancelledReservation = reservationService.getReservationById(created.id());
        assertThat(cancelledReservation.status()).isEqualTo(ReservationStatus.CANCELLED);

        assertThatThrownBy(() -> reservationService.approveReservation(created.id()))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessage("Cannot approve reservation: status=" + ReservationStatus.CANCELLED);
    }

    @Test
    void shouldntCancelApprovedReservation() {
        RoomEntity room = createRoom(nextRoomNumber());
        Reservation created = createReservation(1001L, room.getRoomId());
        assertThat(created.status()).isEqualTo(ReservationStatus.PENDING);

        reservationService.approveReservation(created.id());
        Reservation approvedReservation = reservationService.getReservationById(created.id());
        assertThat(approvedReservation.status()).isEqualTo(ReservationStatus.APPROVED);

        assertThatThrownBy(() -> reservationService.cancelReservation(created.id()))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot cancel reservation: status=APPROVED");
    }

    @Test
    void shouldCreateReservationAndFindItByRoom() {
        long userId = 1001L;
        RoomEntity room = createRoom(nextRoomNumber());
        long roomId = room.getRoomId();

        Reservation created = createReservation(userId, roomId);

        List<Reservation> reservations = reservationService.findAllByFilter(
                new ReservationsFindFilter(roomId, userId, null, null)
        );

        assertThat(created.id()).isPositive();
        assertThat(reservations).hasSize(1);

        Reservation reservation = reservations.getFirst();
        assertThat(reservation.id()).isEqualTo(created.id());
        assertThat(reservation.userId()).isEqualTo(userId);
        assertThat(reservation.roomId()).isEqualTo(roomId);
        assertThat(reservation.startDate().toInstant()).isCloseTo(created.startDate().toInstant(), within(1, ChronoUnit.MICROS));
        assertThat(reservation.endDate().toInstant()).isCloseTo(created.endDate().toInstant(), within(1, ChronoUnit.MICROS));
        assertThat(reservation.status()).isEqualTo(ReservationStatus.PENDING);
    }

    private Reservation createReservation(long userId, long roomId) {
        ZonedDateTime startDate = ZonedDateTime.now(ZoneId.of("Europe/Moscow")).plusDays(10);
        ZonedDateTime endDate = startDate.plusDays(2);
        return createReservation(userId, roomId, startDate, endDate);
    }

    private Reservation createReservation(long userId, long roomId, ZonedDateTime startDate, ZonedDateTime endDate) {
        Reservation request = new Reservation(null, userId, roomId, startDate, endDate, null);
        return reservationService.createReservation(request);
    }

    private RoomEntity createRoom(long roomNumber) {
        return roomRepository.save(new RoomEntity(null, roomNumber));
    }
}
