package cheetah.example.flinkstates.job;

import cheetah.example.flinkstates.function.FlinkMapStatesMapper;
import cheetah.example.flinkstates.model.InputEvent;

import java.util.List;

import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.streaming.api.operators.StreamFlatMap;
import org.apache.flink.streaming.runtime.streamrecord.StreamRecord;
import org.apache.flink.streaming.util.KeyedOneInputStreamOperatorTestHarness;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FlinkMapStatesMapperTest {

    private KeyedOneInputStreamOperatorTestHarness<String, InputEvent, Double> harness;

    //Setup test harness for all the below tests
    @BeforeEach
    public void setup() throws Exception {
        FlinkMapStatesMapper sut = new FlinkMapStatesMapper();
        harness = new KeyedOneInputStreamOperatorTestHarness<>(new StreamFlatMap<>(sut), InputEvent::getDeviceId, Types.STRING);
        harness.setup();
        harness.open();
    }

    @Test
    public void outputIfOnlyOneMessage() throws Exception {
        harness.processElement(new StreamRecord<>(new InputEvent("device", 1.0, System.currentTimeMillis())));
        List<Double> output = harness.extractOutputValues();
        Assertions.assertEquals(1, (long) output.size());
        Assertions.assertEquals(1, output.get(0));
    }

    @Test
    public void ensureSumIsCalculated() throws Exception {
        harness.processElement(new StreamRecord<>(new InputEvent("device", 1.0, System.currentTimeMillis())));
        harness.processElement(new StreamRecord<>(new InputEvent("device", 2.0, System.currentTimeMillis())));
        List<Double> output = harness.extractOutputValues();
        Assertions.assertEquals(2, (long) output.size());
        Assertions.assertEquals(1, output.get(0));
        Assertions.assertEquals(3, output.get(1));
    }

    @Test
    public void ensureSumIsCalculatedPerDevice() throws Exception {
        harness.processElement(new StreamRecord<>(new InputEvent("device", 1.0, System.currentTimeMillis())));
        harness.processElement(new StreamRecord<>(new InputEvent("device2", 2.0, System.currentTimeMillis())));
        harness.processElement(new StreamRecord<>(new InputEvent("device", 3.0, System.currentTimeMillis())));
        List<Double> output = harness.extractOutputValues();
        Assertions.assertEquals(3, (long) output.size());
        Assertions.assertEquals(1, output.get(0));
        Assertions.assertEquals(2, output.get(1));
        Assertions.assertEquals(4, output.get(2));
    }

}
