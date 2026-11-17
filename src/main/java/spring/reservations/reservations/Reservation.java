package spring.reservations.reservations;

import jakarta.annotation.Nonnull;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Null;

import java.time.LocalDate;
import java.time.ZonedDateTime;

record Reservation (
        @Null
        Long id,

        @Nonnull
        Long userId,

        @Nonnull
        Long roomId,

        @Nonnull
        @FutureOrPresent
        ZonedDateTime startDate,

        @Nonnull
        @FutureOrPresent
        ZonedDateTime endDate,

        ReservationStatus status
) {

}
