package spring.reservations.reservations;

import jakarta.annotation.Nonnull;
import jakarta.persistence.*;

import java.time.ZonedDateTime;

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
    private ZonedDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private ZonedDateTime endDate;

    @Column(name = "status")
    private ReservationStatus status;

    @Column(name = "time_for_payment")
    private ZonedDateTime timeForPayment;

    @Version
    @Column(name = "reservation_version")
    private Long version;

    public ReservationEntity() {

    }

    public ReservationEntity(Long id, Long userId, Long roomId, ZonedDateTime startDate, ZonedDateTime endDate, ReservationStatus status) {
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

    public ZonedDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(@Nonnull ZonedDateTime startDate) {
        this.startDate = startDate;
    }

    public ZonedDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(@Nonnull ZonedDateTime endDate) {
        this.endDate = endDate;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public void setStatus(@Nonnull ReservationStatus status) {
        this.status = status;
    }

    public ZonedDateTime getTimeForPayment() {
        return timeForPayment;
    }

    public void setTimeForPayment(ZonedDateTime now) {
        this.timeForPayment = now;
    }
}
