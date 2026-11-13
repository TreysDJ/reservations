package spring.reservations.web;

import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGenericException(Exception e) {
        log.error("Handle exception", e);

        ErrorResponseDTO errorDto = new ErrorResponseDTO(
                "Internal server error",
                LocalDateTime.now()
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorDto);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleEntityNotFoundException(EntityNotFoundException e) {
        log.error("Handle entityNotFoundException", e);

        ErrorResponseDTO errorDto = new ErrorResponseDTO(
                "Entity not found",
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
    public ResponseEntity<ErrorResponseDTO> handleBadRequest(Exception e) {
        log.error("Handle bad request exception", e);

        ErrorResponseDTO errorDto = new ErrorResponseDTO(
                "Bad request",
                LocalDateTime.now()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorDto);
    }

    @ExceptionHandler({
            ObjectOptimisticLockingFailureException.class
    })
    public ResponseEntity<ErrorResponseDTO> handleConflict(ObjectOptimisticLockingFailureException e) {
        log.error("Handle conflict exception", e);

        ErrorResponseDTO errorDto = new ErrorResponseDTO(
                "Conflict",
                LocalDateTime.now()
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(errorDto);
    }
}
