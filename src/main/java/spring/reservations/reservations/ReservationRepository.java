package spring.reservations.reservations;

import jakarta.annotation.Nonnull;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ReservationRepository extends JpaRepository<ReservationEntity, Long> {
    @Query("""
            SELECT r.id from ReservationEntity r
            WHERE r.roomId = :roomId 
            AND :startDate < r.endDate
            AND :endDate > r.startDate
            AND r.status = :status
            """)
    List<Long> findConflictReservations(
            @Nonnull @Param("roomId") Long roomId,
            @Nonnull @Param("startDate") LocalDate startDate,
            @Nonnull @Param("endDate") LocalDate endDate,
            @Nonnull @Param("status") ReservationStatus status
    );

    @Query("""
            SELECT r.id from ReservationEntity r
            WHERE r.roomId = :roomId 
            AND r.id != :id
            AND :startDate < r.endDate
            AND :endDate > r.startDate
            AND r.status = :status
            """)
    List<Long> findConflictReservations(
            @Nonnull @Param("roomId") Long roomId,
            @Nonnull @Param("startDate") LocalDate startDate,
            @Nonnull @Param("endDate") LocalDate endDate,
            @Nonnull @Param("status") ReservationStatus status,
            @Nonnull @Param("id") Long id
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
}
