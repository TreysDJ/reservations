package spring.reservations.reservations;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import spring.reservations.reservations.availability.ReservationAvaillabilityService;

import java.util.List;

@Service
public class ReservationService {
    private static final Logger log = LoggerFactory.getLogger(ReservationService.class);

    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int DEFAULT_PAGE_NUMBER = 0;

    private final ReservationRepository repository;
    private final ReservationAvaillabilityService availlabilityService;
    private final ReservationMapper mapper;

    public ReservationService(ReservationRepository repository, ReservationMapper mapper, ReservationAvaillabilityService availlabilityService) {
        this.repository = repository;
        this.availlabilityService = availlabilityService;
        this.mapper = mapper;
    }


    @Nonnull
    public Reservation getReservationById(@Nonnull Long id) {
        ReservationEntity reservationEntity = repository.findById(id).orElseThrow(() -> new EntityNotFoundException(
                "Not found reservation by id = " + id));
        return mapper.toDomain(reservationEntity);
    }

    public List<Reservation> findAllByFilter(ReservationsFindFilter filter) {
        int pageSize = filter.pageSize() != null ? filter.pageSize() : DEFAULT_PAGE_SIZE;
        int pageNumber = filter.pageNumber() != null ? filter.pageNumber() : DEFAULT_PAGE_NUMBER;

        Pageable pageable = Pageable.ofSize(pageSize).withPage(pageNumber);

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

        boolean isConflict = availlabilityService.isReservationConflict(
                reservationToCreate.roomId(),
                reservationToCreate.startDate(),
                reservationToCreate.endDate()
        );

        if (isConflict) {
            throw new IllegalArgumentException("Cannot create reservation: Room ID or Date is incorrect");
        }

        ReservationEntity entityToSave = mapper.toEntity(reservationToCreate);
        entityToSave.setStatus(ReservationStatus.PENDING);
        ReservationEntity savedEntity = repository.save(entityToSave);
        return mapper.toDomain(savedEntity);
    }

    public Reservation updateReservation(Long id, Reservation reservationToUpdate) {
        ReservationEntity reservationEntity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Not found reservation by id = " + id));

        if (reservationEntity.getStatus() != ReservationStatus.PENDING) {
            throw new IllegalStateException("Cannot modify reservation: status=" + reservationEntity.getStatus());
        }

        if (!reservationToUpdate.endDate().isAfter(reservationToUpdate.startDate())) {
            throw new IllegalArgumentException("Start date must be earlier then end date");
        }

        ReservationEntity reservationToSave = mapper.toEntity(reservationToUpdate);

        reservationToSave.setId(reservationEntity.getId());
        reservationToSave.setStatus(ReservationStatus.PENDING);

        ReservationEntity updatedReservation = repository.save(reservationToSave);
        return mapper.toDomain(updatedReservation);
    }

    public void cancelReservation(Long id) {
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("Not found reservation by id = " + id);
        }

        ReservationEntity reservation = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Not found reservation by id = " + id));

        if (reservation.getStatus().equals(ReservationStatus.APPROVED)) {
            throw new IllegalArgumentException("Cannot cancel reservation: status=APPROVED");
        }

        if (reservation.getStatus().equals(ReservationStatus.CANCELLED)) {
            return;
        }

        repository.setStatus(id, ReservationStatus.CANCELLED);
        log.info("Successfully cancelled reservation: id={}", id);
    }


    public Reservation approveReservation(Long id) {
        ReservationEntity reservationEntity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Not found reservation by id = " + id));

        if (reservationEntity.getStatus() != ReservationStatus.APPROVED) {
            return mapper.toDomain(reservationEntity);
        }

        if (reservationEntity.getStatus() != ReservationStatus.PENDING) {
            throw new IllegalStateException("Cannot approve reservation: status=" + reservationEntity.getStatus());
        }

        boolean isConflict = availlabilityService.isReservationConflict(
                reservationEntity.getRoomId(),
                reservationEntity.getStartDate(),
                reservationEntity.getEndDate()
        );

        if (isConflict) {
            throw new IllegalStateException("Cannot approve reservation: isConflict = true");
        }

        ReservationEntity approvedReservation = new ReservationEntity(
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
