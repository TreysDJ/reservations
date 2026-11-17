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

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Service
public class ReservationService {
    private static final Logger log = LoggerFactory.getLogger(ReservationService.class);

    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int DEFAULT_PAGE_NUMBER = 0;

    private final ReservationRepository reservationRepository;
    private final RoomRepository roomRepository;
    private final ReservationAvaillabilityService availlabilityService;
    private final ReservationMapper mapper;
    private final TransactionTemplate tx;
    private final TransactionTemplate txRead;

    public ReservationService(
            @Nonnull ReservationRepository reservationRepository,
            @Nonnull RoomRepository roomRepository,
            @Nonnull ReservationMapper mapper,
            @Nonnull ReservationAvaillabilityService availlabilityService,
            @Nonnull PlatformTransactionManager txManager
    ) {
        this.reservationRepository = reservationRepository;
        this.roomRepository = roomRepository;
        this.availlabilityService = availlabilityService;
        this.mapper = mapper;
        this.tx = new TransactionTemplate(txManager);
        this.txRead = new TransactionTemplate(txManager);
        this.txRead.setReadOnly(true);
    }

    public Reservation getReservationById(@Nonnull Long id) {
        ReservationEntity reservationEntity = txRead.execute(status ->
                reservationRepository.findById(id).orElseThrow(() -> new EntityNotFoundException(
                        "Not found reservation by id = " + id))
        );
        return mapper.toDomain(reservationEntity);
    }

    public List<Reservation> findAllByFilter(@Nonnull ReservationsFindFilter filter) {
        int pageSize = filter.pageSize() != null ? filter.pageSize() : DEFAULT_PAGE_SIZE;
        int pageNumber = filter.pageNumber() != null ? filter.pageNumber() : DEFAULT_PAGE_NUMBER;

        Pageable pageable = Pageable.ofSize(pageSize).withPage(pageNumber);

        return txRead.execute(status -> {
            List<ReservationEntity> allEntities = reservationRepository.findAllByFilter(
                    filter.roomId(),
                    filter.userId(),
                    pageable
            );

            return allEntities.stream()
                    .map(mapper::toDomain).toList();
        });
    }

    public List<Room> getAllRooms() {
        return txRead.execute(status -> {
            List<RoomEntity> allRoomEntities = roomRepository.findAllRooms();
            return allRoomEntities.stream().map(mapper::toDomain).toList();
        });
    }

    public List<Reservation> getMyReservations(@Nonnull Long id) {
        return txRead.execute(status -> {
            List<ReservationEntity> allReservations = reservationRepository.findMyReservations(id);
            return allReservations.stream()
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
            RoomEntity roomEntity = roomRepository.findRoomByIdWithLock(reservationToCreate.roomId());

            if (roomEntity == null) {
                throw new EntityNotFoundException("Not found roomEntity by id=" + reservationToCreate.roomId());
            }

            boolean isConflict = availlabilityService.isReservationConflict(
                    reservationToCreate.roomId(),
                    reservationToCreate.startDate(),
                    reservationToCreate.endDate()
            );

            if (isConflict) {
                throw new IllegalArgumentException("Cannot create reservation: RoomEntity ID or Date is incorrect");
            }
            ReservationEntity entityToSave = mapper.toEntity(reservationToCreate);
            entityToSave.setStatus(ReservationStatus.PENDING);
            entityToSave.setRoomId(reservationToCreate.roomId());
            entityToSave.setTimeForPayment(ZonedDateTime.now(ZoneId.of("Europe/Moscow")).plusMinutes(15));
            ReservationEntity savedEntity = reservationRepository.save(entityToSave);
            return mapper.toDomain(savedEntity);
        });
    }

    public void cancelReservation(@Nonnull Long id) {
        tx.executeWithoutResult(status -> {
            ReservationEntity reservationEntity = reservationRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Reservation with id=" + id + " not found"));

            if (reservationEntity.getStatus().equals(ReservationStatus.APPROVED)) {
                throw new IllegalArgumentException("Cannot cancel reservation: status=APPROVED");
            }

            if (reservationEntity.getStatus().equals(ReservationStatus.CANCELLED)) {
                return;
            }

            reservationEntity.setStatus(ReservationStatus.CANCELLED);
            reservationRepository.save(reservationEntity);
            log.info("Successfully cancelled reservation: id={}", id);
        });
    }

    public Reservation approveReservation(@Nonnull Long id) {
        return tx.execute(status -> {
            ReservationEntity reservationEntity = reservationRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Reservation with id=" + id + " not found"));

            if (reservationEntity.getStatus() == ReservationStatus.APPROVED) {
                return mapper.toDomain(reservationEntity);
            }

            if (reservationEntity.getStatus() != ReservationStatus.PENDING) {
                throw new IllegalStateException("Cannot approve reservation: status=" + reservationEntity.getStatus());
            }
            reservationEntity.setStatus(ReservationStatus.APPROVED);
            ReservationEntity savedEntity = reservationRepository.save(reservationEntity);
            return mapper.toDomain(savedEntity);
        });
    }

    public void cancelExpiredReservations() {
        List<ReservationEntity> expiredReservations = reservationRepository.findExpiredReservations(
                ZonedDateTime.now(ZoneId.of("Europe/Moscow")),
                ReservationStatus.PENDING
        );

        for (ReservationEntity reservationEntity : expiredReservations) {
            cancelReservation(reservationEntity.getId());
        }
    }
}
