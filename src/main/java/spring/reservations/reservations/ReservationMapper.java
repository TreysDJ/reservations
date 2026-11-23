package spring.reservations.reservations;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.springframework.stereotype.Component;

@Component
public class ReservationMapper {
    @Nullable
    public Reservation toDomain(@Nonnull ReservationEntity reservationEntity) {
        return new Reservation(
                reservationEntity.getId(),
                reservationEntity.getUserId(),
                reservationEntity.getRoomId(),
                reservationEntity.getStartDate().withZoneSameInstant(HotelConstants.HOTEL_ZONE),
                reservationEntity.getEndDate().withZoneSameInstant(HotelConstants.HOTEL_ZONE),
                reservationEntity.getStatus()
        );
    }

    public ReservationEntity toEntity(@Nonnull Reservation reservation) {
        return new ReservationEntity(
                reservation.id(),
                reservation.userId(),
                reservation.roomId(),
                reservation.startDate(),
                reservation.endDate(),
                reservation.status()
        );
    }

    public Room toDomain(@Nonnull RoomEntity room) {
        return new Room(
                room.getId(),
                room.getRoomId()
        );
    }

    public RoomEntity toEntity(@Nonnull Room room) {
        return new RoomEntity(
                room.id(),
                room.roomId()
        );
    }
}
