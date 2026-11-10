package spring.reservations.web;

import java.time.LocalDateTime;

public record ErrorResponseDataTransferObject(
        String message,
        String detailedMessage,
        LocalDateTime errorTime
) {

}
