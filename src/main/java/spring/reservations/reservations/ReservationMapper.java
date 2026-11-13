package spring.reservations.reservations;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.springframework.stereotype.Component;

@Component
public class ReservationMapper {
    @Nullable
    public Reservation toDomain(@Nonnull ReservationEntity reservation) {
        return new Reservation(
                reservation.getId(),
                reservation.getUserId(),
                reservation.getRoomId(),
                reservation.getStartDate(),
                reservation.getEndDate(),
                reservation.getStatus()
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
}
