package spring.reservations.reservations;

import jakarta.annotation.Nonnull;
import jakarta.validation.constraints.FutureOrPresent;

import java.time.LocalDate;

public record ReservationCreateRequestDTO(
        @Nonnull
        Long userId,

        @Nonnull
        Long roomId,

        @Nonnull
        LocalDate startDate,

        @Nonnull
        LocalDate endDate
) {
}
