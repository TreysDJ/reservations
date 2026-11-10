package spring.reservations.web;

import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDataTransferObject> handleGenericException(Exception e) {
        log.error("Handle exception", e);

        ErrorResponseDataTransferObject errorDto = new ErrorResponseDataTransferObject(
                "Internal server error",
                e.getMessage(),
                LocalDateTime.now()
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorDto);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponseDataTransferObject> handleEntityNotFoundException(EntityNotFoundException e) {
        log.error("Handle entityNotFoundException", e);

        ErrorResponseDataTransferObject errorDto = new ErrorResponseDataTransferObject(
                "Entity not found",
                e.getMessage(),
                LocalDateTime.now()
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(errorDto);
    }

    @ExceptionHandler({
            IllegalArgumentException.class,
            IllegalStateException.class,
            MethodArgumentNotValidException.class
    })
    public ResponseEntity<ErrorResponseDataTransferObject> handleBadRequest(Exception e) {
        log.error("Handle bad request exception", e);

        ErrorResponseDataTransferObject errorDto = new ErrorResponseDataTransferObject(
                "Bad request",
                e.getMessage(),
                LocalDateTime.now()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorDto);
    }
}
