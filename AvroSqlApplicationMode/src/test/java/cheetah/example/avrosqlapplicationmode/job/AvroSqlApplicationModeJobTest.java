package cheetah.example.avrosqlapplicationmode.job;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AvroSqlApplicationModeJobTest {

    @Nested
    class MultipleSchemaTests {
        private List<String> rawSchemaFromApicurio;

        @BeforeEach
        void specificSetUp() {

            List<String> value = new ArrayList<>();
            List<String> type = new ArrayList<>();

            rawSchemaFromApicurio = new ArrayList<>();

            value.add("deviceId");
            value.add("value");
            value.add("timestamp");
            value.add("extraField");

            type.add("string");
            type.add("double");
            type.add("long");
            type.add("string");

            rawSchemaFromApicurio.add("{\"schema\":\"{\\\"type\\\":\\\"record\\\",\\\"name\\\":\\\"OutputEventAvro\\\",\\\"namespace\\\":\\\"cheetah.example.model.avrorecord\\\",\\\"fields\\\":[{\\\"name\\\":\\\"" + value.get(0) + "\\\",\\\"type\\\":{\\\"type\\\":\\\""+ type.get(0) +
                    "\\\",\\\"avro.java.string\\\":\\\"String\\\"}},{\\\"name\\\":\\\"" + value.get(1) + "\\\",\\\"type\\\":\\\"" + type.get(1) +
                    "\\\"},{\\\"name\\\":\\\"" + value.get(2) + "\\\",\\\"type\\\":\\\"" + type.get(2) +
                    "\\\"},{\\\"name\\\":\\\"" + value.get(3) + "\\\",\\\"type\\\":{\\\"type\\\":\\\"" + type.get(3) +
                    "\\\",\\\"avro.java.string\\\":\\\"String\\\"}}]}\",\"references\":[]}");

            value.clear();
            type.clear();

            value.add("deviceNo");
            value.add("time");
            value.add("date");
            value.add("newField");

            type.add("string");
            type.add("double");
            type.add("long");
            type.add("float");

            rawSchemaFromApicurio.add("{\"schema\":\"{\\\"type\\\":\\\"record\\\",\\\"name\\\":\\\"OutputEventAvro\\\",\\\"namespace\\\":\\\"cheetah.example.model.avrorecord\\\",\\\"fields\\\":[{\\\"name\\\":\\\"" + value.get(0) + "\\\",\\\"type\\\":{\\\"type\\\":\\\""+ type.get(0) +
                    "\\\",\\\"avro.java.string\\\":\\\"String\\\"}},{\\\"name\\\":\\\"" + value.get(1) + "\\\",\\\"type\\\":\\\"" + type.get(1) +
                    "\\\"},{\\\"name\\\":\\\"" + value.get(2) + "\\\",\\\"type\\\":\\\"" + type.get(2) +
                    "\\\"},{\\\"name\\\":\\\"" + value.get(3) + "\\\",\\\"type\\\":{\\\"type\\\":\\\"" + type.get(3) +
                    "\\\",\\\"avro.java.string\\\":\\\"String\\\"}}]}\",\"references\":[]}");

            value.clear();
            type.clear();

            value.add("devId");
            value.add("map");
            value.add("multiset");
            value.add("newField");

            type.add("string");
            type.add("float");
            type.add("long");
            type.add("bytes");

            rawSchemaFromApicurio.add("{\"schema\":\"{\\\"type\\\":\\\"record\\\",\\\"name\\\":\\\"OutputEventAvro\\\",\\\"namespace\\\":\\\"cheetah.example.model.avrorecord\\\",\\\"fields\\\":[{\\\"name\\\":\\\"" + value.get(0) + "\\\",\\\"type\\\":{\\\"type\\\":\\\""+ type.get(0) +
                    "\\\",\\\"avro.java.string\\\":\\\"String\\\"}},{\\\"name\\\":\\\"" + value.get(1) + "\\\",\\\"type\\\":\\\"" + type.get(1) +
                    "\\\"},{\\\"name\\\":\\\"" + value.get(2) + "\\\",\\\"type\\\":\\\"" + type.get(2) +
                    "\\\"},{\\\"name\\\":\\\"" + value.get(3) + "\\\",\\\"type\\\":{\\\"type\\\":\\\"" + type.get(3) +
                    "\\\",\\\"avro.java.string\\\":\\\"String\\\"}}]}\",\"references\":[]}");
        }

        @Test
        public void testJsonSchemaToSql () throws IOException {

            List<String> sqlMetadatas = new ArrayList<>();

            for (String schema : rawSchemaFromApicurio) {
                JsonNode jsonSchema = AvroSqlApplicationModeJob.getJsonNode (schema);
                sqlMetadatas.add(AvroSqlApplicationModeJob.jsonSchemaToSql(jsonSchema));
            }

            assertEquals("`deviceId` VARCHAR, `value` DOUBLE, `timestamp` BIGINT, `extraField` VARCHAR", sqlMetadatas.get(0));
            assertEquals("`deviceNo` VARCHAR, `time` DOUBLE, `date` BIGINT, `newField` FLOAT", sqlMetadatas.get(1));
            assertEquals("`devId` VARCHAR, `map` FLOAT, `multiset` BIGINT, `newField` BINARY", sqlMetadatas.get(2));
        }

        @Test
        void testSendGetRequest() {
            try {
                String url = "https://google.com";
                String response = AvroSqlApplicationModeJob.sendGetRequest(url);
                Assertions.assertNotNull(response);
            } catch (IOException e) {
                Assertions.fail("IOException occurred: " + e.getMessage());
            }
        }

        @Test
        void testGetJsonNode() {
            String jsonString = "{\"key\": \"value\"}";
            try {
                JsonNode node = AvroSqlApplicationModeJob.getJsonNode(jsonString);
                Assertions.assertNotNull(node);
                assertEquals("value", node.get("key").asText());
            } catch (IOException e) {
                Assertions.fail("IOException occurred: " + e.getMessage());
            }
        }

        @Test
        void testGetStringArray() {
            String jsonArrayString = "[\"value1\", \"value2\"]";
            try {
                String[] array = AvroSqlApplicationModeJob.getStringArray(jsonArrayString);
                Assertions.assertNotNull(array);
                assertEquals(2, array.length);
                assertEquals("value1", array[0]);
                assertEquals("value2", array[1]);
                // Add more assertions as needed
            } catch (IOException e) {
                Assertions.fail("IOException occurred: " + e.getMessage());
            }
        }
    }
}
