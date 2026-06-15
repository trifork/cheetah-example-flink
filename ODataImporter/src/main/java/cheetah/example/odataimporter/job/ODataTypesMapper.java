package cheetah.example.odataimporter.job;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.apache.flink.api.common.functions.MapFunction;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ODataTypesMapper implements MapFunction<JSONObject, JSONObject> {

    private static final Logger LOG = LoggerFactory.getLogger(ODataTypesMapper.class);

    private static final String DATE_PREFIX = "/Date(";
    private static final String DATE_SUFFIX = ")/";
    private boolean shouldConvertDates;
    private boolean shouldConvertDurations;
    private boolean removeFieldsWithNullValues;

    public ODataTypesMapper(boolean shouldConvertDates, boolean shouldConvertDurations,
            boolean removeFieldsWithNullValues) {
        super();
        this.shouldConvertDates = shouldConvertDates;
        this.shouldConvertDurations = shouldConvertDurations;
        this.removeFieldsWithNullValues = removeFieldsWithNullValues;
    }

    @Override
    public JSONObject map(JSONObject jsonObject) throws Exception {
        convertDates(jsonObject);
        return jsonObject;
    }

    private void convertDates(JSONObject jsonObject) {
        jsonObject.keys().forEachRemaining(key -> {
            try {
                Object value = jsonObject.get(key);
                if (value instanceof JSONObject) {
                    convertDates((JSONObject) value);
                } else if (removeFieldsWithNullValues && value == JSONObject.NULL) {
                    jsonObject.remove(key);
                } else if (value instanceof String) {
                    processStringValue(jsonObject, key, (String) value);
                }
            } catch (Exception e) {
                LOG.error("Error processing key: " + key, e);
            }
        });
    }

    private void processStringValue(JSONObject jsonObject, String key, String valueStr) {
        if (shouldConvertDates && valueStr.startsWith(DATE_PREFIX) && valueStr.endsWith(DATE_SUFFIX)) {
            parseAndSetDate(jsonObject, key, valueStr);
        } else if (shouldConvertDurations && valueStr.startsWith("PT")) {
            parseAndSetDuration(jsonObject, key, valueStr);
        }
    }

    private void parseAndSetDate(JSONObject jsonObject, String key, String valueStr) {
        try {
            Pair<Long, ZoneId> timestampAndZone = parseTimestampAndZone(valueStr);
            ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(timestampAndZone.getFirst()),
                    timestampAndZone.getSecond());
            jsonObject.put(key, zonedDateTime.toInstant());
        } catch (Exception e) {
            LOG.error("Key: " + key + " - Not a valid date format: " + valueStr);
        }
    }

    private Pair<Long, ZoneId> parseTimestampAndZone(String valueStr) {
        int timezoneIndex = Math.max(valueStr.indexOf('+', DATE_PREFIX.length()),
                valueStr.indexOf('-', DATE_PREFIX.length()));
        long timestamp;
        ZoneId zoneId;
        if (timezoneIndex != -1) {
            timestamp = Long.parseLong(valueStr.substring(DATE_PREFIX.length(), timezoneIndex));
            String timezone = valueStr.substring(timezoneIndex, valueStr.length() - DATE_SUFFIX.length());
            zoneId = ZoneId.ofOffset("GMT", java.time.ZoneOffset.of(timezone));
        } else {
            timestamp = Long
                    .parseLong(valueStr.substring(DATE_PREFIX.length(), valueStr.length() - DATE_SUFFIX.length()));
            zoneId = ZoneId.of("Z");
        }
        return new Pair<>(timestamp, zoneId);
    }

    private void parseAndSetDuration(JSONObject jsonObject, String key, String valueStr) {
        try {
            Duration duration = Duration.parse(valueStr);
            jsonObject.put(key, duration.toString());
        } catch (Exception e) {
            LOG.error("Key: " + key + " - Not a valid duration format: " + valueStr);
        }
    }

    // Simple pair class if you don't already have one
    static class Pair<T1, T2> {
        private T1 first;
        private T2 second;

        public Pair(T1 first, T2 second) {
            this.first = first;
            this.second = second;
        }

        public T1 getFirst() {
            return first;
        }

        public T2 getSecond() {
            return second;
        }
    }
}