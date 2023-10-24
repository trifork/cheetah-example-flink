package cheetah.example.transformandstore.job;

import cheetah.example.transformandstore.model.InputEvent;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TransformAndStoreMapperTest {
    private final TransformAndStoreMapper mapper = new TransformAndStoreMapper();

    @Test
    public void testEnrichment() {
        final String deviceId = UUID.randomUUID().toString();
        final double value = 55;
        final long timestamp = 0;
        final var input = new InputEvent(deviceId, value, timestamp);

        final var actual = mapper.map(input);

        assertEquals(deviceId, actual.getDeviceId());
        assertEquals(value, actual.getValue());
        assertEquals(timestamp, actual.getTimestamp());
        assertEquals("Value is good", actual.getStatus());
    }
}
