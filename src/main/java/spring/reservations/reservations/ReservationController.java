package spring.reservations.reservations;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
 
@RestController
@RequestMapping("/reservation")
public class ReservationController {
    private final ReservationService reservationService;
    private final ReservationValidator reservationValidator;
    private static final Logger log = LoggerFactory.getLogger(ReservationController.class);

    public ReservationController(@NotNull ReservationService reservationService,
                                 @NotNull ReservationValidator reservationValidator
    ) {
        this.reservationService = reservationService;
        this.reservationValidator = reservationValidator;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Reservation> getReservationById(@NotNull @PathVariable("id") Long id) {
        log.info("Called getReservationById: {}", id);

        return ResponseEntity.status(HttpStatus.OK)
                .body(reservationService.getReservationById(id));
    }

    @GetMapping()
    public ResponseEntity<List<Reservation>> getAllReservations(
            @RequestParam(name = "roomId", required = false) Long roomId,
            @RequestParam(name = "userId", required = false) Long userId,
            @RequestParam(name = "pageSize", required = false) Integer pageSize,
            @RequestParam(name = "pageNumber", required = false) Integer pageNumber
    ) {
        log.info("Called getAllReservations");
        ReservationsFindFilter filter = new ReservationsFindFilter(
                roomId,
                userId,
                pageSize,
                pageNumber
        );
        return ResponseEntity.ok(reservationService.findAllByFilter(filter));
    }

    @PostMapping()
    public ResponseEntity<Reservation> createReservation(
            @RequestBody Reservation reservationToCreate
    ) {
        reservationValidator.validate(reservationToCreate);
        log.info("Called createReservation");
        return ResponseEntity.status(201)
                .body(reservationService.createReservation(reservationToCreate));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Reservation> updateReservation(
            @PathVariable("id") Long id,
            @RequestBody @Valid Reservation reservationToUpdate
    ) {
        log.info("Called updateReservation id={}, reservationToUpdate={}", id, reservationToUpdate);
        Reservation updated = reservationService.updateReservation(id, reservationToUpdate);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}/cancel")
    public ResponseEntity<Void> deleteReservation(
            @PathVariable("id") Long id
    ) {
        log.info("Called deleteReservation: id={}", id);
        reservationService.cancelReservation(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<Reservation> approveReservation(
            @PathVariable("id") Long id
    ) {
        log.info("Called approveReservation: id={}", id);
        Reservation reservation = reservationService.approveReservation(id);
        return ResponseEntity.ok(reservation);
    }
}
