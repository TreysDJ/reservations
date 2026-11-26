package spring.reservations.reservations;

import jakarta.annotation.Nonnull;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RoomRepository extends JpaRepository<RoomEntity, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT r FROM RoomEntity r
            WHERE r.roomId = :roomId
            """)
    RoomEntity findRoomByIdWithLock(
            @Nonnull @Param("roomId") Long roomId
    );

    @Query("""
            SELECT r FROM RoomEntity r
            """)
    List<Room> findAllRooms();
}
