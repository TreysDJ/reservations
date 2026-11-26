package spring.reservations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.client.RestTestClient;
import spring.reservations.support.PostgresTestConfiguration;
import tools.jackson.databind.json.JsonMapper;

import java.util.concurrent.atomic.AtomicLong;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
@Import(PostgresTestConfiguration.class)
public abstract class BaseIntegrationTest {

    private static final AtomicLong ROOM_NUMBERS = new AtomicLong(1000);

    @Autowired
    protected RestTestClient restClient;

    @Autowired
    protected JsonMapper jsonMapper;

    protected long nextRoomNumber() {
        return ROOM_NUMBERS.incrementAndGet();
    }
}
