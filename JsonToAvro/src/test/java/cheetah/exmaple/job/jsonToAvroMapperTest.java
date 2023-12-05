package cheetah.exmaple.job;

import cheetah.exmaple.model.avrorecord.InputEventAvro;
import cheetah.exmaple.model.json.InputEvent;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class jsonToAvroMapperTest {
    private final jsonToAvroMapper mapper = new jsonToAvroMapper("extraFieldValue");

    @Test
    public void testEnrichment() {
        final String deviceId = UUID.randomUUID().toString();
        final double value = 1.0;
        final long timestamp = 0;
        final var input = new InputEvent(deviceId, value, timestamp);

        final var actual = mapper.map(input);

        assertEquals(deviceId, actual.getDeviceId());
        assertEquals(value, actual.getValue());
        assertEquals(timestamp, actual.getTimestamp());
        assertEquals("extraFieldValue", actual.getExtraField());
    }
}
