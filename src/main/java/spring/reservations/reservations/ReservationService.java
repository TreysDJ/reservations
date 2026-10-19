package spring.reservations.reservations;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import spring.reservations.reservations.availability.ReservationAvaillabilityService;

import java.time.LocalDate;
import java.util.*;

@Service
public class ReservationService {
    private static final Logger log = LoggerFactory.getLogger(ReservationService.class);
    private final ReservationRepository repository;
    private final ReservationAvaillabilityService availlabilityService;
    private final ReservationMapper mapper;

    public ReservationService(ReservationRepository repository, ReservationMapper mapper, ReservationAvaillabilityService availlabilityService) {
        this.repository = repository;
        this.availlabilityService = availlabilityService;
        this.mapper = mapper;
    }


    public Reservation getReservationById(Long id) {
        ReservationEntity reservationEntity = repository.findById(id).orElseThrow(() -> new EntityNotFoundException(
                "Not found reservation by id = " + id));
        return mapper.toDomain(reservationEntity);
    }

    public List<Reservation> findAllByFilter(ReservationsFindFilter filter) {
        int pageSize = filter.pageSize() != null ? filter.pageSize() : 10;
        int pageNumber = filter.pageNumber() != null ? filter.pageNumber() : 10;

        var pageable = Pageable.ofSize(pageSize).withPage(pageNumber);

        List<ReservationEntity> allEntities = repository.findAllByFilter(
                filter.roomId(),
                filter.userId(),
                pageable
        );

        return allEntities.stream()
                .map(mapper::toDomain).toList();
    }

    public Reservation createReservation(Reservation reservationToCreate) {
        if (reservationToCreate.status() != null) {
            throw new IllegalArgumentException("Status should be empty");
        }

        if (!reservationToCreate.endDate().isAfter(reservationToCreate.startDate())) {
            throw new IllegalArgumentException("Start date must be earlier then end date");
        }

        var entityToSave = mapper.toEntity(reservationToCreate);
        entityToSave.setStatus(ReservationStatus.PENDING);
        var savedEntity = repository.save(entityToSave);
        return mapper.toDomain(savedEntity);
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

        var reservationToSave = mapper.toEntity(reservationToUpdate);

        reservationToSave.setId(reservationEntity.getId());
        reservationToSave.setStatus(ReservationStatus.PENDING);

        var updatedReservation = repository.save(reservationToSave);
        return mapper.toDomain(updatedReservation);
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

        var isConflict = availlabilityService.isReservationConflict(
                reservationEntity.getRoomId(),
                reservationEntity.getStartDate(),
                reservationEntity.getEndDate()
        );

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
        return mapper.toDomain(approvedReservation);
    }
}
