package spring.reservations.reservations.availability;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import spring.reservations.reservations.ReservationRepository;
import spring.reservations.reservations.ReservationStatus;

import java.time.LocalDate;
import java.util.List;

@Service
public class ReservationAvaillabilityService {
    private final static Logger log = LoggerFactory.getLogger(ReservationAvaillabilityService.class);
    private final ReservationRepository repository;

    public ReservationAvaillabilityService(ReservationRepository repository) {
        this.repository = repository;
    }

    public boolean isReservationConflict(
            Long roomId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        if (!endDate.isAfter(startDate)) {
            throw new IllegalArgumentException("Start date must be earlier then end date");
        }

        List<Long> conflicting = repository.findConflictReservations(
                roomId,
                startDate,
                endDate,
                ReservationStatus.APPROVED
        );

        if (conflicting.isEmpty()) {
            return false;
        }
        log.info("Conflicting with: id={}", conflicting);
        return true;
    }
}
