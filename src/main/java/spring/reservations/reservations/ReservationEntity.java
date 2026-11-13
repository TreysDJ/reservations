package spring.reservations.reservations;

import jakarta.annotation.Nonnull;
import jakarta.persistence.*;

import java.time.LocalDate;

@Table(name = "reservations")
@Entity
public class ReservationEntity {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "room_id", nullable = false)
    private Long roomId;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "status")
    private ReservationStatus status;

    @Version
    @Column(name = "reservation_version")
    private Long version;

    public ReservationEntity() {

    }

    public ReservationEntity(Long id, Long userId, Long roomId, LocalDate startDate, LocalDate endDate, ReservationStatus status) {
        this.id = id;
        this.userId = userId;
        this.roomId = roomId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
    }

    public void setId(@Nonnull Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(@Nonnull Long userId) {
        this.userId = userId;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(@Nonnull Long roomId) {
        this.roomId = roomId;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(@Nonnull LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(@Nonnull LocalDate endDate) {
        this.endDate = endDate;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public void setStatus(@Nonnull ReservationStatus status) {
        this.status = status;
    }
}
