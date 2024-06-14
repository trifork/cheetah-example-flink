package cheetah.example.job;

import cheetah.example.model.avrorecord.OutputEventAvro;
import cheetah.example.model.json.InputEvent;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JsonToAvroMapperTest {
    private final JsonToAvroMapper mapper = new JsonToAvroMapper();

    @Test
    public void testEnrichment() {
        final String deviceId = UUID.randomUUID().toString();
        final double value = 1.0;
        final long timestamp = 0;
        final InputEvent input = new InputEvent(deviceId, value, timestamp);

        final OutputEventAvro actual = mapper.map(input);

        assertEquals(deviceId, actual.getDeviceId());
        assertEquals(value, actual.getValue());
        assertEquals(timestamp, actual.getTimestamp());
    }
}
