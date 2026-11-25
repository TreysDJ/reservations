package spring.reservations.reservations;

import jakarta.annotation.Nonnull;

import java.time.LocalDate;

public record ReservationGetResponseDTO(
        @Nonnull Long roomId,

        @Nonnull LocalDate startDate,

        @Nonnull LocalDate endDate,

        @Nonnull ReservationStatus status
) {
}
