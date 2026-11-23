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

        LocalDate today = LocalDate.now(HotelConstants.HOTEL_ZONE);
        LocalDate startLocal = reservation.startDate()
                .withZoneSameInstant(HotelConstants.HOTEL_ZONE)
                .toLocalDate();
        if (startLocal.isBefore(today)) {
            throw new IllegalArgumentException("Start date cannot be in the past");
        }
    }
}
