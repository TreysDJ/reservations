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

    @Column(name = "title")
    private String title;

    @Column(name = "img")
    private String img;

    protected RoomEntity() {

    }

    public RoomEntity(Long id, Long roomId, String title, String img) {
        this.id = id;
        this.roomId = roomId;
        this.title = title;
        this.img = img;
    }

    public Long getId() {
        return id;
    }

    public Long getRoomId() {
        return roomId;
    }

    public String getTitle() {
        return title;
    }

    public String getImg() {
        return img;
    }
}
