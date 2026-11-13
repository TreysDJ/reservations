package spring.reservations.reservations.availability;

import jakarta.annotation.Nonnull;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CheckAvailabilityRequest(
        @Nonnull Long roomId,

        @Nonnull LocalDate startDate,

        @Nonnull LocalDate endDate
) {
}
