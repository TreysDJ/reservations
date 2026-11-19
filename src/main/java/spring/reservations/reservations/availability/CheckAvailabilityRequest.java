package spring.reservations.reservations.availability;

import jakarta.annotation.Nonnull;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.ZonedDateTime;

public record CheckAvailabilityRequest(
        @Nonnull Long roomId,

        @Nonnull ZonedDateTime startDate,

        @Nonnull ZonedDateTime endDate
) {
}
