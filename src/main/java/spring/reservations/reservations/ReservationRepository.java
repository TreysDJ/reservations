package spring.reservations.reservations;

import jakarta.annotation.Nonnull;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
            @Nonnull @Param("startDate") LocalDate startDate,
            @Nonnull @Param("endDate") LocalDate endDate,
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
            @Nonnull @Param("roomId") Long roomId,
            @Nonnull @Param("userId") Long userId,
            Pageable pageable
    );

    @Query("""
            SELECT r.roomId FROM ReservationEntity r
            WHERE r.userId = :userId
            """)
    List<ReservationEntity> findMyReservations(
            @Nonnull @Param("userid") Long userId
    );

    @Query("""
            SELECT r FROM ReservationEntity r
            WHERE r.timeForPayment < :currentTime
            AND r.status = :status
            """)
    List<ReservationEntity> findExpiredReservations(
            @Nonnull @Param("currentTime") LocalDateTime currentTime,
            @Nonnull @Param("status") ReservationStatus status
    );
}
