package cheetah.example.avrosqlapplicationmode.job;

import cheetah.example.avrosqlapplicationmode.model.InputEvent;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.avro.Schema;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AvroSqlApplicationModeJobTest {
    private final AvroSqlApplicationModeJob mapper = new AvroSqlApplicationModeJob();

    @Test
    public void testjsonSchemaToSql() throws IOException {
        final String rawSchemaFromApicurio = "{\"type\":\"record\",\"name\":\"OutputEventAvro\",\"namespace\":\"cheetah.example.model.avrorecord\",\"fields\":[{\"name\":\"deviceId\",\"type\":{\"type\":\"string\",\"avro.java.string\":\"String\"}},{\"name\":\"value\",\"type\":\"double\"},{\"name\":\"timestamp\",\"type\":\"long\"},{\"name\":\"extraField\",\"type\":{\"type\":\"string\",\"avro.java.string\":\"String\"}}]}";

        final JsonNode jsonSchemaFromApicurio = AvroSqlApplicationModeJob.getJsonNode (rawSchemaFromApicurio);

        final var sqlMetadata = AvroSqlApplicationModeJob.jsonSchemaToSql(jsonSchemaFromApicurio);

        assertEquals(sqlMetadata, "deviceId VARCHAR, value DOUBLE, timestamp BIGINT, extraField VARCHAR");
    }
}
