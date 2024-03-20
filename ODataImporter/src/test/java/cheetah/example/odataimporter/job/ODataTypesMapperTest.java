package cheetah.example.odataimporter.job;

import java.time.Duration;
import java.time.Instant;

import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ODataTypesMapperTest {

    @Test
    public void testDateConversion() throws Exception {
        // Setup
        JSONObject input = new JSONObject();
        input.put("CreationDate", "/Date(1636675200000)/");
        ODataTypesMapper mapper = new ODataTypesMapper(true, false, false);

        // Action
        JSONObject result = mapper.map(input);

        // Verify
        Assertions.assertTrue(result.get("CreationDate") instanceof Instant);
        Assertions.assertEquals(Instant.ofEpochMilli(1636675200000L), result.get("CreationDate"));
    }

    @Test
    public void testDateConversion2() throws Exception {
        // Setup
        JSONObject input = new JSONObject();
        input.put("CreationDate", "/Date(1539954222000+0000)/");
        ODataTypesMapper mapper = new ODataTypesMapper(true, false, false);

        // Action
        JSONObject result = mapper.map(input);

        // Verify
        Assertions.assertTrue(result.get("CreationDate") instanceof Instant);
        Assertions.assertEquals(Instant.ofEpochMilli(1539954222000L), result.get("CreationDate"));
    }

    @Test
    public void testNoDateConversion() throws Exception {
        // Setup
        JSONObject input = new JSONObject();
        input.put("CreationDate", "/Date(1636675200000)/");
        ODataTypesMapper mapper = new ODataTypesMapper(false, false, false);

        // Action
        JSONObject result = mapper.map(input);

        // Verify
        Assertions.assertEquals("/Date(1636675200000)/", result.getString("CreationDate"));
    }

    @Test
    public void testRecursiveDateConversion() throws Exception {
        // Setup
        JSONObject nestedObject = new JSONObject();
        nestedObject.put("NestedDate", "/Date(1609459200000)/");
        JSONObject input = new JSONObject();
        input.put("NestedObject", nestedObject);
        ODataTypesMapper mapper = new ODataTypesMapper(true, false, false);

        // Action
        JSONObject result = mapper.map(input);
        JSONObject nestedResult = result.getJSONObject("NestedObject");

        // Verify
        Assertions.assertTrue(nestedResult.get("NestedDate") instanceof Instant);
        Assertions.assertEquals(Instant.ofEpochMilli(1609459200000L), nestedResult.get("NestedDate"));
    }

    @Test
    public void testDurationConversion() throws Exception {
        // Setup
        JSONObject input = new JSONObject();
        input.put("ProcessTime", "PT10H30M");
        ODataTypesMapper mapper = new ODataTypesMapper(false, true, false);

        // Action
        JSONObject result = mapper.map(input);

        // Verify
        Assertions.assertEquals("PT10H30M", result.getString("ProcessTime"));
        Assertions.assertTrue(Duration.parse("PT10H30M").equals(Duration.parse(result.getString("ProcessTime"))));
    }

    @Test
    public void testNoDurationConversion() throws Exception {
        // Setup
        JSONObject input = new JSONObject();
        input.put("ProcessTime", "PT10H30M");
        ODataTypesMapper mapper = new ODataTypesMapper(false, false, false);

        // Action
        JSONObject result = mapper.map(input);

        // Verify
        Assertions.assertEquals("PT10H30M", result.getString("ProcessTime"));
    }

    @Test
    public void testRecursiveDurationConversion() throws Exception {
        // Setup
        JSONObject nestedObject = new JSONObject();
        nestedObject.put("NestedProcessTime", "PT20H45M");
        JSONObject input = new JSONObject();
        input.put("NestedObject", nestedObject);
        ODataTypesMapper mapper = new ODataTypesMapper(false, true, false);

        // Action
        JSONObject result = mapper.map(input);
        JSONObject nestedResult = result.getJSONObject("NestedObject");

        // Verify
        Assertions.assertEquals("PT20H45M", nestedResult.getString("NestedProcessTime"));
        Assertions.assertTrue(
                Duration.parse("PT20H45M").equals(Duration.parse(nestedResult.getString("NestedProcessTime"))));
    }

    @Test
    public void testNullRemoval() throws Exception {
        // Setup
        JSONObject input = new JSONObject();
        input.put("LatestAcceptableCompletionDate", JSONObject.NULL);
        ODataTypesMapper mapper = new ODataTypesMapper(false, false, true);

        // Action
        JSONObject result = mapper.map(input);
        var nestedResult = result.opt("LatestAcceptableCompletionDate");

        // Verify
        Assertions.assertNull(nestedResult);
    }

}
