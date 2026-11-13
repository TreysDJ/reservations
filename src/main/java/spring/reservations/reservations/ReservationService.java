package spring.reservations.reservations;

import jakarta.annotation.Nonnull;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
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
    private final TransactionTemplate tx;
    private final TransactionTemplate txRead;

    public ReservationService(
            @Nonnull ReservationRepository repository,
            @Nonnull ReservationMapper mapper,
            @Nonnull ReservationAvaillabilityService availlabilityService,
            @Nonnull PlatformTransactionManager txManager
    ) {
        this.repository = repository;
        this.availlabilityService = availlabilityService;
        this.mapper = mapper;
        this.tx = new TransactionTemplate(txManager);
        this.txRead = new TransactionTemplate(txManager);
        this.txRead.setReadOnly(true);
    }

    public Reservation getReservationById(@Nonnull Long id) {
        return txRead.execute(status -> {
            ReservationEntity reservationEntity = repository.findById(id).orElseThrow(() -> new EntityNotFoundException(
                    "Not found reservation by id = " + id));
            return mapper.toDomain(reservationEntity);
        });
    }

    public List<Reservation> findAllByFilter(@Nonnull ReservationsFindFilter filter) {
        int pageSize = filter.pageSize() != null ? filter.pageSize() : DEFAULT_PAGE_SIZE;
        int pageNumber = filter.pageNumber() != null ? filter.pageNumber() : DEFAULT_PAGE_NUMBER;

        Pageable pageable = Pageable.ofSize(pageSize).withPage(pageNumber);

        return txRead.execute(status -> {
            List<ReservationEntity> allEntities = repository.findAllByFilter(
                    filter.roomId(),
                    filter.userId(),
                    pageable
            );

            return allEntities.stream()
                    .map(mapper::toDomain).toList();
        });
    }

    public Reservation createReservation(@Nonnull Reservation reservationToCreate) {
        if (reservationToCreate.status() != null) {
            throw new IllegalArgumentException("Status should be empty");
        }

        if (!reservationToCreate.endDate().isAfter(reservationToCreate.startDate())) {
            throw new IllegalArgumentException("Start date must be earlier then end date");
        }
        return tx.execute(status -> {
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
        });
    }

    public Reservation updateReservation(@Nonnull Long id, @Nonnull Reservation reservationToUpdate) {
        return tx.execute(status -> {
            ReservationEntity reservationEntity = repository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Not found reservation by id = " + id));

            if (reservationEntity.getStatus() != ReservationStatus.PENDING) {
                throw new IllegalStateException("Cannot modify reservation: status=" + reservationEntity.getStatus());
            }

            if (!reservationToUpdate.endDate().isAfter(reservationToUpdate.startDate())) {
                throw new IllegalArgumentException("Start date must be earlier then end date");
            }

            boolean isConflict = availlabilityService.isReservationConflict(
                    reservationToUpdate.roomId(),
                    reservationToUpdate.startDate(),
                    reservationToUpdate.endDate(),
                    reservationToUpdate.id()
            );

            if (isConflict) {
                throw new IllegalArgumentException("Cannot create reservation: Date is incorrect");
            }
            reservationEntity.setStartDate(reservationToUpdate.startDate());
            reservationEntity.setEndDate(reservationToUpdate.endDate());
            reservationEntity.setRoomId(reservationToUpdate.roomId());
            return mapper.toDomain(reservationEntity);
        });
    }

    public void cancelReservation(@Nonnull Long id) {
         tx.executeWithoutResult(status -> {
            ReservationEntity reservation = repository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Not found reservation by id = " + id));

            if (reservation.getStatus().equals(ReservationStatus.APPROVED)) {
                throw new IllegalArgumentException("Cannot cancel reservation: status=APPROVED");
            }

            if (reservation.getStatus().equals(ReservationStatus.CANCELLED)) {
                return;
            }

            reservation.setStatus(ReservationStatus.CANCELLED);
            log.info("Successfully cancelled reservation: id={}", id);
        });
    }


    public Reservation approveReservation(@Nonnull Long id) {
        return tx.execute(status -> {
            ReservationEntity reservationEntity = repository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Not found reservation by id = " + id));

            if (reservationEntity.getStatus() == ReservationStatus.APPROVED) {
                return mapper.toDomain(reservationEntity);
            }

            if (reservationEntity.getStatus() != ReservationStatus.PENDING) {
                throw new IllegalStateException("Cannot approve reservation: status=" + reservationEntity.getStatus());
            }

            boolean isConflict = availlabilityService.isReservationConflict(
                    reservationEntity.getRoomId(),
                    reservationEntity.getStartDate(),
                    reservationEntity.getEndDate(),
                    reservationEntity.getId()
            );

            if (isConflict) {
                throw new IllegalStateException("Cannot approve reservation: isConflict = true");
            }
            reservationEntity.setStatus(ReservationStatus.APPROVED);
            return mapper.toDomain(reservationEntity);
        });
    }
}
