package spring.reservations.reservations;

import jakarta.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@RestController
@RequestMapping("/reservation")
public class ReservationController {
    private final ReservationService reservationService;
    private static final Logger log = LoggerFactory.getLogger(ReservationController.class);

    public ReservationController(@Nonnull ReservationService reservationService,
                                 @Nonnull ReservationValidator reservationValidator
    ) {
        this.reservationService = reservationService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Reservation> getReservationById(@Nonnull @PathVariable("id") Long id) {
        log.info("Called getReservationById: {}", id);

        Reservation reservation = reservationService.getReservationById(id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(reservation);
    }

    @GetMapping("/list")
    public ResponseEntity<List<Reservation>> getAllReservations(
            @RequestParam(name = "userId", required = false) Long userId,
            @RequestParam(name = "roomId", required = false) Long roomId,
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

    @GetMapping("/rooms")
    public ResponseEntity<List<Long>> getAllRooms() {
        log.info("Called getAllRooms");
        return ResponseEntity.ok(reservationService.getAllRooms());
    }

    @GetMapping("/my/reservations")
    public ResponseEntity<List<Long>> getMyReservations(@RequestParam(name = "userId") Long userId) {
        log.info("called getMyReservations");
        return ResponseEntity.ok(reservationService.getMyReservations(userId));
    }

    @PostMapping("/create")
    public ResponseEntity<ReservationCreateResponseDTO> createReservation(
            @Nonnull @RequestBody ReservationCreateRequestDTO requestDTO
    ) {
        log.info("Called createReservation");
        return ResponseEntity.status(201)
                .body(toResponse(reservationService.createReservation(requestDTO)));
    }

    @DeleteMapping("/{id}/cancel")
    public ResponseEntity<Void> deleteReservation(
            @Nonnull @PathVariable("id") Long id
    ) {
        log.info("Called deleteReservation: id={}", id);
        reservationService.cancelReservation(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<Reservation> approveReservation(
            @Nonnull @PathVariable("id") Long id
    ) {
        log.info("Called approveReservation: id={}", id);
        Reservation reservation = reservationService.approveReservation(id);
        return ResponseEntity.ok(reservation);
    }

    private ReservationCreateResponseDTO toResponse(Reservation reservation) {
        return new ReservationCreateResponseDTO(
                reservation.id(),
                reservation.userId(),
                reservation.roomId(),
                reservation.startDate(),
                reservation.endDate(),
                reservation.status()
        );
    }
}
