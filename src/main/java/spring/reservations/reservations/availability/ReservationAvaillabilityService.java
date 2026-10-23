package spring.reservations.reservations.availability;

import jakarta.validation.constraints.NotNull;
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

    public ReservationAvaillabilityService(@NotNull ReservationRepository repository) {
        this.repository = repository;
    }

    public boolean isReservationConflict(
            @NotNull Long roomId,
            @NotNull LocalDate startDate,
            @NotNull LocalDate endDate
    ) {
        if (!endDate.isAfter(startDate)) {
            throw new IllegalArgumentException("Start date must be earlier than end date");
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
