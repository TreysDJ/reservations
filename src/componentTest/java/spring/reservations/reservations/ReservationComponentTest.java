package spring.reservations.reservations;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import spring.reservations.BaseComponentTest;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReservationComponentTest extends BaseComponentTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private RoomRepository roomRepository;

    @Test
    void shouldCreateReservationAndFindItByRoom() {
        long userId = 1001L;
        Room room = createRoom(nextRoomNumber());
        long roomId = room.getRoomId();
        LocalDate startDate = LocalDate.now().plusDays(10);
        LocalDate endDate = startDate.plusDays(2);
        Reservation request = new Reservation(null, userId, roomId, startDate, endDate, null);

        Reservation created = reservationService.createReservation(request);
        List<Reservation> reservations = reservationService.findAllByFilter(
                new ReservationsFindFilter(roomId, userId, null, null)
        );

        assertThat(created.id()).isPositive();
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
