package spring.reservations.reservations;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReservationValidatorTest {

    private final ReservationValidator validator = new ReservationValidator();

    @Test
    void shouldRejectEndDateBeforeStartDate() {
        LocalDate startDate = LocalDate.now().plusDays(10);
        ReservationCreateRequestDTO reservation = new ReservationCreateRequestDTO(
                1001L, 21L, startDate, startDate.minusDays(1)
        );

        assertThatThrownBy(() -> validator.validate(reservation))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Start date must be earlier than end date");
    }
}
