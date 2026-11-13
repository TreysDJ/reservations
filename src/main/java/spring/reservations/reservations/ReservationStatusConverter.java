package spring.reservations.reservations;

import jakarta.annotation.Nonnull;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Converter(autoApply = true)
public class ReservationStatusConverter implements AttributeConverter<ReservationStatus, String> {
    private static final Logger log = LoggerFactory.getLogger(ReservationService.class);

    @Override
    public String convertToDatabaseColumn(@Nonnull ReservationStatus status) {
        return status.getValue();
    }

    @Override
    public ReservationStatus convertToEntityAttribute(@Nonnull String dbData) {
        for (ReservationStatus status : ReservationStatus.values()) {
            if (status.getValue().equals(dbData)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown status in table: " + dbData);
    }
}
