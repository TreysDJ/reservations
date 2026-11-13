package spring.reservations.reservations;

public enum ReservationStatus {
    PENDING("pending"),
    APPROVED("approved"),
    CANCELLED("cancelled");

    private final String value;

    ReservationStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
