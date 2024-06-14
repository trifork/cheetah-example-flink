package com.cheetah.example.job;

import com.cheetah.example.model.InputEvent;
import com.cheetah.example.model.OutputEvent;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class KeySerializationSchemaMapperTest {
    private final KeySerializationSchemaMapper mapper = new KeySerializationSchemaMapper("extraFieldValue");

    @Test
    public void testEnrichment() {
        final String deviceId = UUID.randomUUID().toString();
        final double value = 1.0;
        final long timestamp = 0;
        final String keys = "keys";
        final InputEvent input = new InputEvent(deviceId, value, timestamp, keys);

        final OutputEvent actual = mapper.map(input);

        assertEquals(deviceId, actual.getDeviceId());
        assertEquals(value, actual.getValue());
        assertEquals(timestamp, actual.getTimestamp());
        assertEquals(keys, actual.getKeys());
        assertEquals("extraFieldValue", actual.getExtraField());
    }
}
