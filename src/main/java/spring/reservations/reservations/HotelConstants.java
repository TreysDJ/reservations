package spring.reservations.reservations;

import java.time.LocalTime;
import java.time.ZoneId;

public final class HotelConstants {

    public static final LocalTime CHECK_IN_TIME = LocalTime.of(14, 0);
    public static final LocalTime CHECK_OUT_TIME = LocalTime.of(12, 0);
    public static final ZoneId HOTEL_ZONE = ZoneId.of("Europe/Moscow");

    private HotelConstants() {
    }
}
