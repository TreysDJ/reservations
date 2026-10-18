package spring.reservations;

import java.time.LocalDateTime;

public record ErrorResponseDataTransferObject(
        String message,
        String detailedMessage,
        LocalDateTime errorTime
) {

}
