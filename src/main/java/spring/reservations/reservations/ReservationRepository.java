package spring.reservations.reservations;

import jakarta.annotation.Nonnull;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;

public interface ReservationRepository extends JpaRepository<ReservationEntity, Long> {
    @Query("""
            SELECT r.id from ReservationEntity r
            WHERE r.roomId = :roomId 
            AND :startDate < r.endDate
            AND :endDate > r.startDate
            AND (r.status = :approved
             OR r.status = :pending)
            """)
    List<Long> findConflictReservations(
            @Nonnull @Param("roomId") Long roomId,
            @Nonnull @Param("startDate") ZonedDateTime startDate,
            @Nonnull @Param("endDate") ZonedDateTime endDate,
            @Nonnull @Param("approved") ReservationStatus approved,
            @Nonnull @Param("pending") ReservationStatus pending
    );

    @Query("""
            SELECT r from ReservationEntity r
            WHERE (:roomId IS NULL OR r.roomId = :roomId)
            AND (:userId IS NULL OR r.userId = :userId)
            ORDER BY r.id ASC
            """)
    List<ReservationEntity> findAllByFilter(
            @Param("roomId") Long roomId,
            @Param("userId") Long userId,
            Pageable pageable
    );

    @Query("""
            SELECT r FROM ReservationEntity r
            WHERE r.userId = :userId
            AND r.status != :status
            """)
    List<Reservation> findMyReservations(
            @Nonnull @Param("userId") Long userId,
            @Nonnull @Param("status") ReservationStatus status
    );

    @Query("""
            SELECT r.id FROM ReservationEntity r
            WHERE r.timeForPayment < :currentTime
            AND r.status = :status
            ORDER BY r.timeForPayment DESC
            LIMIT 200
            """)
    List<Long> findExpiredReservations(
            @Nonnull @Param("currentTime") Instant currentTime,
            @Nonnull @Param("status") ReservationStatus status
    );
}
