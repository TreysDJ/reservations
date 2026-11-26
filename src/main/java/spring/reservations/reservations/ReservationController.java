package spring.reservations.reservations;

import jakarta.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ReservationController {
    private final ReservationService reservationService;
    private static final Logger log = LoggerFactory.getLogger(ReservationController.class);

    public ReservationController(@Nonnull ReservationService reservationService,
                                 @Nonnull ReservationValidator reservationValidator
    ) {
        this.reservationService = reservationService;
    }

    @GetMapping("/reservation/{id}")
    public ResponseEntity<ReservationGetResponseDTO> getReservationById(@Nonnull @PathVariable("id") Long id) {
        log.info("Called getReservationById: {}", id);

        Reservation reservation = reservationService.getReservationById(id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(toReservationGetResponse(reservation));
    }

    @GetMapping("/reservation/list")
    public ResponseEntity<List<ReservationGetResponseDTO>> getAllReservations(
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
        return ResponseEntity.ok(
                reservationService.findAllByFilter(filter).stream().map(this::toReservationGetResponse).toList()
        );
    }

    @GetMapping("/rooms")
    public ResponseEntity<List<RoomGetResponseDTO>> getAllRooms() {
        log.info("Called getAllRooms");
        return ResponseEntity.ok(
                reservationService.getAllRooms().stream().map(this::toRoomGetResponse).toList()
        );
    }

    @GetMapping("/reservation/list/{userId}")
    public ResponseEntity<List<ReservationGetResponseDTO>> getMyReservations(@PathVariable(name = "userId") Long userId) {
        log.info("called getMyReservations");
        return ResponseEntity.ok(
                reservationService.getMyReservations(userId).stream().map(this::toReservationGetResponse).toList()
        );
    }

    @PostMapping("/reservation/create")
    public ResponseEntity<ReservationCreateResponseDTO> createReservation(
            @Nonnull @RequestBody ReservationCreateRequestDTO requestDTO
    ) {
        log.info("Called createReservation");
        return ResponseEntity.status(201)
                .body(toReservationCreateResponse(reservationService.createReservation(
                        requestDTO.userId(),
                        requestDTO.roomId(),
                        requestDTO.startDate(),
                        requestDTO.endDate()
                )));
    }

    @DeleteMapping("/reservation/{id}/cancel")
    public ResponseEntity<Void> deleteReservation(
            @Nonnull @PathVariable("id") Long id
    ) {
        log.info("Called deleteReservation: id={}", id);
        reservationService.cancelReservation(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reservation/{id}/approve")
    public ResponseEntity<ReservationGetResponseDTO> approveReservation(
            @Nonnull @PathVariable("id") Long id
    ) {
        log.info("Called approveReservation: id={}", id);
        Reservation reservation = reservationService.approveReservation(id);
        return ResponseEntity.ok(toReservationGetResponse(reservation));
    }

    private ReservationCreateResponseDTO toReservationCreateResponse(Reservation reservation) {
        return new ReservationCreateResponseDTO(
                reservation.id(),
                reservation.userId(),
                reservation.roomId(),
                reservation.startDate().toLocalDate(),
                reservation.endDate().toLocalDate(),
                reservation.status()
        );
    }

    private ReservationGetResponseDTO toReservationGetResponse(Reservation reservation) {
        return new ReservationGetResponseDTO(
                reservation.id(),
                reservation.roomId(),
                reservation.startDate().toLocalDate(),
                reservation.endDate().toLocalDate(),
                reservation.status()
        );
    }

    private RoomGetResponseDTO toRoomGetResponse(Room room) {
        return new RoomGetResponseDTO(
                room.roomId(),
                room.title(),
                room.img()
        );
    }
}
