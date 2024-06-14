package cheetah.example.job;

import cheetah.example.model.avrorecord.InputEventAvro;
import cheetah.example.model.json.OutputEventJson;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AvroToJsonMapperTest {
    private final AvroToJsonMapper mapper = new AvroToJsonMapper();

    @Test
    public void testEnrichment() {
        final String deviceId = UUID.randomUUID().toString();
        final double value = 1.0;
        final long timestamp = 0;
        final InputEventAvro input = new InputEventAvro(deviceId, value, timestamp);

        final OutputEventJson actual = mapper.map(input);

        assertEquals(deviceId, actual.getDeviceId());
        assertEquals(value, actual.getValue());
        assertEquals(timestamp, actual.getTimestamp());
    }
}
