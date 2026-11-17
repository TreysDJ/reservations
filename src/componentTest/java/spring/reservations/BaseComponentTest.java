package spring.reservations;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import spring.reservations.support.PostgresTestConfiguration;

import java.util.concurrent.atomic.AtomicLong;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Import(PostgresTestConfiguration.class)
public abstract class BaseComponentTest {

    private static final AtomicLong ROOM_NUMBERS = new AtomicLong(1000);

    protected long nextRoomNumber() {
        return ROOM_NUMBERS.incrementAndGet();
    }
}
