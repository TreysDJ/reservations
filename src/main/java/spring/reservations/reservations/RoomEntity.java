package spring.reservations.reservations;

import jakarta.persistence.*;

@Table(name = "rooms")
@Entity
public class RoomEntity {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_id")
    private Long roomId;

    protected RoomEntity() {

    }

    public RoomEntity(Long id, Long roomId) {
        this.id = id;
        this.roomId = roomId;
    }

    public Long getId() {
        return id;
    }

    public Long getRoomId() {
        return roomId;
    }
}
