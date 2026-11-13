package spring.reservations.reservations;

import jakarta.annotation.Nonnull;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;

import java.time.LocalDate;

record Reservation (
        @Null
        Long id,

        @Nonnull
        Long userId,

        @Nonnull
        Long roomId,

        @Nonnull
        @FutureOrPresent
        LocalDate startDate,

        @Nonnull
        @FutureOrPresent
        LocalDate endDate,

        ReservationStatus status
) {

}
