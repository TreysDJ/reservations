package spring.reservations.reservations;

import jakarta.annotation.Nonnull;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Component
public class ReservationValidator {
    public void validate(@Nonnull Reservation reservation) {
        if (reservation.roomId() == null) {
            throw new IllegalArgumentException("RoomEntity ID cannot be null");
        }

        if (reservation.startDate() == null || reservation.endDate() == null) {
            throw new IllegalArgumentException("Dates cannot be null");
        }

        if (!reservation.startDate().isBefore(reservation.endDate())) {
            throw new IllegalArgumentException("Start date must be earlier than end date");
        }

        if (!reservation.startDate().isAfter(ZonedDateTime.now(ZoneId.of("Europe/Moscow"))) ||
                !reservation.endDate().isAfter(ZonedDateTime.now(ZoneId.of("Europe/Moscow")))) {
            throw new IllegalArgumentException("Dates must be future or present time");
        }
    }
}
