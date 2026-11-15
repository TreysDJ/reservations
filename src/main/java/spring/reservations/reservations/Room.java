package spring.reservations.reservations;

import jakarta.persistence.*;

@Table(name = "rooms")
@Entity
public class Room {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "roomId")
    private Long roomId;

    public Room() {

    }

    public Long getRoomId() {
        return roomId;
    }
}
