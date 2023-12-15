package cheetah.example.transformandstore.job;

import cheetah.example.transformandstore.model.InputEvent;
import cheetah.example.transformandstore.model.OutputEvent;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TransformAndStoreMapperTest {
    private final TransformAndStoreMapper mapper = new TransformAndStoreMapper();

    @Test
    public void testEnrichment() {
        final String deviceId = UUID.randomUUID().toString();
        final double value = 55;
        final long timestamp = 0;
        final InputEvent input = new InputEvent(deviceId, value, timestamp);

        final OutputEvent actual = mapper.map(input);

        assertEquals(deviceId, actual.getDeviceId());
        assertEquals(value, actual.getValue());
        assertEquals(timestamp, actual.getTimestamp());
        assertEquals("Value is acceptable", actual.getStatus());
    }
}
