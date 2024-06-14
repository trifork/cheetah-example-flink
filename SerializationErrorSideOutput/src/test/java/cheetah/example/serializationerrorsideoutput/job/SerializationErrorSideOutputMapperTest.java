package cheetah.example.serializationerrorsideoutput.job;

import cheetah.example.serializationerrorsideoutput.function.SerializationErrorSideOutputMapper;
import cheetah.example.serializationerrorsideoutput.model.InputEvent;
import cheetah.example.serializationerrorsideoutput.model.OutputEvent;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SerializationErrorSideOutputMapperTest {
    private final SerializationErrorSideOutputMapper mapper = new SerializationErrorSideOutputMapper("extraFieldValue");

    @Test
    public void testEnrichment() {
        final String deviceId = UUID.randomUUID().toString();
        final double value = 1.0;
        final long timestamp = 0;
        final InputEvent input = new InputEvent(deviceId, value, timestamp);

        final OutputEvent actual = mapper.map(input);

        assertEquals(deviceId, actual.getDeviceId());
        assertEquals(value, actual.getValue());
        assertEquals(timestamp, actual.getTimestamp());
        assertEquals("extraFieldValue", actual.getExtraField());
    }
}
