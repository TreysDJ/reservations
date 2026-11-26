package spring.reservations.reservations;

import jakarta.annotation.Nonnull;

public record RoomGetResponseDTO(
        @Nonnull Long roomId,

        @Nonnull String title,

        @Nonnull String img
) {
}
