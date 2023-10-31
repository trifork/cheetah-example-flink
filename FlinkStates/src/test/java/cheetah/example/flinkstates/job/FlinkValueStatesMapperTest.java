package cheetah.example.flinkstates.job;


import cheetah.example.flinkstates.function.FlinkValueStatesMapper;
import cheetah.example.flinkstates.model.InputEvent;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.streaming.api.operators.StreamFlatMap;
import org.apache.flink.streaming.runtime.streamrecord.StreamRecord;
import org.apache.flink.streaming.util.KeyedOneInputStreamOperatorTestHarness;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FlinkValueStatesMapperTest {

    private KeyedOneInputStreamOperatorTestHarness<String, InputEvent, Double> harness;

    //Setup test harness for all the below tests
    @BeforeEach
    public void setup() throws Exception {
        var sut = new FlinkValueStatesMapper();
        harness = new KeyedOneInputStreamOperatorTestHarness<>((new StreamFlatMap<>(sut)), InputEvent::getDeviceId, Types.STRING);
        harness.setup();
        harness.open();
    }

    @Test
    public void ensureAverageIsCalculated() throws Exception {
        harness.processElement(new StreamRecord<>(new InputEvent("device", 1.0, System.currentTimeMillis())));
        harness.processElement(new StreamRecord<>(new InputEvent("device", 2.0, System.currentTimeMillis())));
        var output = harness.extractOutputValues();
        Assertions.assertEquals(1, (long) output.size());
        Assertions.assertEquals(1.5, output.get(0));
    }

    @Test
    public void ensureNoOutputIfOnlyOneMessage() throws Exception {
        harness.processElement(new StreamRecord<>(new InputEvent("device", 1.0, System.currentTimeMillis())));
        var output = harness.extractOutputValues();
        Assertions.assertEquals(0, (long) output.size());
    }
}
