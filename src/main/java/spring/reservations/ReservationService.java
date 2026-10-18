package spring.reservations;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ReservationService {
    private static final Logger log = LoggerFactory.getLogger(ReservationService.class);
    private final ReservationRepository repository;

    public ReservationService(ReservationRepository repository) {
        this.repository = repository;
    }


    public Reservation getReservationById(Long id) {
        ReservationEntity reservationEntity = repository.findById(id).orElseThrow(() -> new EntityNotFoundException(
                "Not found reservation by id = " + id));
        return toDomainReservation(reservationEntity);
    }

    public List<Reservation> findAllReservations() {
        List<ReservationEntity> allEntities = repository.findAll();
        
        return allEntities.stream()
                .map(this::toDomainReservation).toList();
    }

    public Reservation createReservation(Reservation reservationToCreate) {
        if (reservationToCreate.status() != null) {
            throw new IllegalArgumentException("Status should be empty");
        }

        if (!reservationToCreate.endDate().isAfter(reservationToCreate.startDate())) {
            throw new IllegalArgumentException("Start date must be earlier then end date");
        }

        var reservationEntity = new ReservationEntity(
                null,
                reservationToCreate.userId(),
                reservationToCreate.roomId(),
                reservationToCreate.startDate(),
                reservationToCreate.endDate(),
                ReservationStatus.PENDING
        );

        var savedEntity = repository.save(reservationEntity);
        return toDomainReservation(savedEntity);
    }

    public Reservation updateReservation(Long id, Reservation reservationToUpdate) {
        var reservationEntity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Not found reservation by id = "  + id));

        if (reservationEntity.getStatus() != ReservationStatus.PENDING) {
            throw new IllegalStateException("Cannot modify reservation: status=" + reservationEntity.getStatus());
        }

        if (!reservationToUpdate.endDate().isAfter(reservationToUpdate.startDate())) {
            throw new IllegalArgumentException("Start date must be earlier then end date");
        }

        var reservationToSave = new ReservationEntity(
                reservationEntity.getId(),
                reservationToUpdate.userId(),
                reservationToUpdate.roomId(),
                reservationToUpdate.startDate(),
                reservationToUpdate.endDate(),
                ReservationStatus.PENDING
        );
        var updatedReservation = repository.save(reservationToSave);
        return toDomainReservation(updatedReservation);
    }

    @Transactional
    public void cancelReservation(Long id) {
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("Not found reservation by id = " + id);
        }

        var reservation = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Not found reservation by id = "  + id));

        if (reservation.getStatus().equals(ReservationStatus.APPROVED)) {
            throw new EntityNotFoundException("Cannot cancel approved reservation");
        }

        if (reservation.getStatus().equals(ReservationStatus.CANCELLED)) {
            throw new EntityNotFoundException("Cannot cancel the reservation. Reservation was already cancelled");
        }

        repository.setStatus(id, ReservationStatus.CANCELLED);
        log.info("Successfully cancelled reservation: id={}", id);
    }


    public Reservation approveReservation(Long id) {
        var reservationEntity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Not found reservation by id = "  + id));

        if (reservationEntity.getStatus() != ReservationStatus.PENDING) {
            throw new IllegalStateException("Cannot approve reservation: status=" + reservationEntity.getStatus());
        }

        var isConflict = isReservationConflict(toDomainReservation(reservationEntity));

        if (isConflict) {
            throw new IllegalStateException("Cannot approve reservation: isConflict = true");
        }

        var approvedReservation = new ReservationEntity(
                reservationEntity.getId(),
                reservationEntity.getUserId(),
                reservationEntity.getRoomId(),
                reservationEntity.getStartDate(),
                reservationEntity.getEndDate(),
                ReservationStatus.APPROVED
        );
        repository.save(approvedReservation);
        return toDomainReservation(approvedReservation);
    }

    private boolean isReservationConflict(Reservation reservation) {
        for (ReservationEntity existingReservation: repository.findAll()) {
            if (reservation.id().equals(existingReservation.getId())) {
                continue;
            }

            if (!reservation.roomId().equals(existingReservation.getRoomId())) {
                continue;
            }

            if (!existingReservation.getStatus().equals(ReservationStatus.APPROVED)) {
                continue;
            }

            if (reservation.startDate().isBefore(existingReservation.getEndDate()) &&
                    existingReservation.getStartDate().isBefore(reservation.endDate())) {
                return true;
            }
        }
        return false;
    }

    private Reservation toDomainReservation(ReservationEntity entity) {
        return new Reservation(
                entity.getId(),
                entity.getUserId(),
                entity.getRoomId(),
                entity.getStartDate(),
                entity.getEndDate(),
                entity.getStatus()
        );
    }
}
