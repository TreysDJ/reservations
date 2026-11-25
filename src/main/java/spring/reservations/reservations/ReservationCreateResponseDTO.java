package spring.reservations.reservations;

import jakarta.annotation.Nonnull;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Null;

import java.time.LocalDate;
import java.time.ZonedDateTime;

public record ReservationCreateResponseDTO(
        @Null
        Long id,

        @Nonnull
        Long userId,

        @Nonnull
        Long roomId,

        @Nonnull
        LocalDate startDate,

        @Nonnull
        LocalDate endDate,

        ReservationStatus status
) {

}
