package spring.reservations.scheduler;

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

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
    private final ReservationService reservationService;

    public SchedulingTasks(ReservationService reservationService) {
        this.reservationService = reservationService;
    }
    @Scheduled(fixedRate = 1000)
    public void reportCurrentTime() {
        log.info("The time is now {}", dateFormat.format(new Date()));
    }

    @Scheduled(fixedRate = 60000)
    public void cancelExpiredReservations() {
        reservationService.cancelExpiredReservations();
    }
}
