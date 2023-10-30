package cheetah.example.job;


import cheetah.example.model.FlinkStatesInputEvent;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.streaming.api.operators.StreamFlatMap;
import org.apache.flink.streaming.runtime.streamrecord.StreamRecord;
import org.apache.flink.streaming.util.KeyedOneInputStreamOperatorTestHarness;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FlinkAggregatingStatesMapperTest {

    private KeyedOneInputStreamOperatorTestHarness<String, FlinkStatesInputEvent, Double> harness;

    //Setup test harness for all the below tests
    @BeforeEach
    public void setup() throws Exception {
        var sut = new FlinkAggregatingStatesMapper();
        harness = new KeyedOneInputStreamOperatorTestHarness<>((new StreamFlatMap<>(sut)), FlinkStatesInputEvent::getDeviceId, Types.STRING);
        harness.setup();
        harness.open();
    }

    @Test
    public void outputIfOnlyOneMessage() throws Exception {
        harness.processElement(new StreamRecord<>(new FlinkStatesInputEvent("device", 1.0, System.currentTimeMillis())));
        var output = harness.extractOutputValues();
        Assertions.assertEquals(1, (long) output.size());
        Assertions.assertEquals(1, output.get(0));
    }

    @Test
    public void ensureSumIsCalculated() throws Exception {
        harness.processElement(new StreamRecord<>(new FlinkStatesInputEvent("device", 1.0, System.currentTimeMillis())));
        harness.processElement(new StreamRecord<>(new FlinkStatesInputEvent("device", 2.0, System.currentTimeMillis())));
        var output = harness.extractOutputValues();
        Assertions.assertEquals(2, (long) output.size());
        Assertions.assertEquals(1, output.get(0));
        Assertions.assertEquals(3, output.get(1));
    }

}