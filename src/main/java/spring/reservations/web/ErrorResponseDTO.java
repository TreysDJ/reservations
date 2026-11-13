package spring.reservations.web;

import java.time.LocalDateTime;

public record ErrorResponseDTO(
        String message,
        LocalDateTime errorTime
) {

}
