package spring.reservations.reservations;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import spring.reservations.BaseComponentTest;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class ReservationComponentTest extends BaseComponentTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private RoomRepository roomRepository;

    @Test
    void shouldCreateReservations() {
        RoomEntity room = createRoom(nextRoomNumber());
        Reservation created = createReservation(1001L, room.getRoomId());
        assertThat(created.id()).isPositive();
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
        Reservation request = new Reservation(null, userId, roomId, startDate, endDate, null);
        return reservationService.createReservation(request);
    }

    private RoomEntity createRoom(long roomNumber) {
        return roomRepository.save(new RoomEntity(null, roomNumber));
    }
}
