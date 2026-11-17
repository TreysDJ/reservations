package spring.reservations.scheduler;

import jakarta.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import spring.reservations.reservations.ReservationEntity;
import spring.reservations.reservations.ReservationService;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Component
public class SchedulingTasks {
    private static final Logger log = LoggerFactory.getLogger(SchedulingTasks.class);

    private final ReservationService reservationService;

    public SchedulingTasks(@Nonnull ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @Scheduled(fixedDelay = 60000)
    public void cancelExpiredReservations() {
        log.info("called cancelExpiredReservations");
        reservationService.cancelExpiredReservations();
    }
}
