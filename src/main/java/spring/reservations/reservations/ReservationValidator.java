package spring.reservations.reservations;

import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class ReservationValidator {
    public void validate(Reservation reservation) {
        if (reservation.roomId() == null) {
            throw new IllegalArgumentException("Room ID cannot be null");
        }

        if (reservation.startDate() == null || reservation.endDate() == null) {
            throw new IllegalArgumentException("Dates cannot be null");
        }

        if (!reservation.startDate().isBefore(reservation.endDate())) {
            throw new IllegalArgumentException("Start date must be earlier than end date");
        }

        if (!reservation.startDate().isAfter(LocalDate.now()) || !reservation.endDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Dates must be future or present time");
        }
    }
}
