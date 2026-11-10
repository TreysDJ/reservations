package spring.reservations.reservations.availability;

import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reservation/awailability")
public class ReservationAvailabilityController {
    private final static Logger log = LoggerFactory.getLogger(ReservationAvailabilityController.class);
    private final ReservationAvaillabilityService service;

    public ReservationAvailabilityController(@NotNull ReservationAvaillabilityService service) {
        this.service = service;
    }

    @PostMapping("/check")
    public ResponseEntity<CheckAvailabilityResponse> checkAvailability(
            @NotNull CheckAvailabilityRequest request
    ) {
        log.info("Called checkAvailability: request={}", request);
        service.isReservationConflict(
                request.roomId(),
                request.startDate(),
                request.endDate()
        );

        boolean isConflict = service.isReservationConflict(
                request.roomId(),
                request.startDate(),
                request.endDate()
        );

        String message = isConflict ? "Room not available to reservation" : "Room available to reservation";
        AvailabilityStatus status = isConflict ? AvailabilityStatus.RESERVED : AvailabilityStatus.AVAILABLE;

        return ResponseEntity.status(200).body(new CheckAvailabilityResponse(message, status));
    }
}
