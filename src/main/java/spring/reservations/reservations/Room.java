package spring.reservations.reservations;

import jakarta.annotation.Nonnull;
import jakarta.validation.constraints.Null;

record Room(
        @Null
        Long id,

        @Nonnull
        Long roomId,

        @Nonnull String title,

        @Nonnull String img
) {

}
