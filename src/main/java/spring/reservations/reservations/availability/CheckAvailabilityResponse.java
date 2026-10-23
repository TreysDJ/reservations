package spring.reservations.reservations.availability;

public record CheckAvailabilityResponse(
        String message,

        AvailabilityStatus status
) {
}
