package spring.reservations.reservations;

public record ReservationsFindFilter(
        Long roomId,

        Long userId,

        Integer pageSize,

        Integer pageNumber
) {

}
