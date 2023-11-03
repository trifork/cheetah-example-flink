package cheetah.example.multiplesideoutput.job;

import cheetah.example.multiplesideoutput.function.MultipleSideOutputExampleProcess;
import cheetah.example.multiplesideoutput.model.InputEvent;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.streaming.api.operators.KeyedProcessOperator;

import org.apache.flink.streaming.runtime.streamrecord.StreamRecord;
import org.apache.flink.streaming.util.KeyedOneInputStreamOperatorTestHarness;
import org.apache.flink.util.OutputTag;
import org.junit.jupiter.api.*;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MultipleSideOutputExampleJobTest {

    private KeyedOneInputStreamOperatorTestHarness<String, InputEvent, InputEvent> harness;

    // Setup the job before each state
    @BeforeEach
    public void setup() throws Exception {
        var sut = new MultipleSideOutputExampleProcess();
        var operator = new KeyedProcessOperator<>(sut);
        harness = new KeyedOneInputStreamOperatorTestHarness<>(operator, InputEvent::getDeviceId, Types.STRING);
        harness.setup();
        harness.open();
    }

    @Test
    public void outputOnEverySideOutput() throws Exception{
        harness.processElement(new StreamRecord<>(new InputEvent("device_1", 1.0, 2.0, 3.0, 4.0, Instant.now().toEpochMilli())));
        harness.processElement(new StreamRecord<>(new InputEvent("device_1", 1.0, 2.0, 3.0, 4.0, Instant.now().toEpochMilli()))); // Sending the same event again, should not result in a new output since it's the same as state
        assertEquals(1, harness.getSideOutput(new OutputTag<>("output-a"){}).size());
        assertEquals(1, harness.getSideOutput(new OutputTag<>("output-b"){}).size());
        assertEquals(1, harness.getSideOutput(new OutputTag<>("output-cd"){}).size());
    }

    @Test
    public void outputOnSideOutputA() throws Exception{
        harness.processElement(new StreamRecord<>(new InputEvent("device_1", 1.0, 2.0, 3.0, 4.0, Instant.now().toEpochMilli())));
        harness.processElement(new StreamRecord<>(new InputEvent("device_1", 1.5, 2.0, 3.0, 4.0, Instant.now().toEpochMilli()))); // Sending an event with the changed value in A should result in a new output on side output A
        assertEquals(2, harness.getSideOutput(new OutputTag<>("output-a"){}).size());
        assertEquals(1, harness.getSideOutput(new OutputTag<>("output-b"){}).size());
        assertEquals(1, harness.getSideOutput(new OutputTag<>("output-cd"){}).size());
    }

    @Test
    public void outputOnSideOutputB() throws Exception{
        harness.processElement(new StreamRecord<>(new InputEvent("device_1", 1.0, 2.0, 3.0, 4.0, Instant.now().toEpochMilli())));
        harness.processElement(new StreamRecord<>(new InputEvent("device_1", 1.0, 2.5, 3.0, 4.0, Instant.now().toEpochMilli()))); // Sending an event with the changed value in B should result in a new output on side output B
        assertEquals(1, harness.getSideOutput(new OutputTag<>("output-a"){}).size());
        assertEquals(2, harness.getSideOutput(new OutputTag<>("output-b"){}).size());
        assertEquals(1, harness.getSideOutput(new OutputTag<>("output-cd"){}).size());
    }

    @Test
    public void outputOnSideOutputC() throws Exception{
        harness.processElement(new StreamRecord<>(new InputEvent("device_1", 1.0, 2.0, 3.0, 4.0, Instant.now().toEpochMilli())));
        harness.processElement(new StreamRecord<>(new InputEvent("device_1", 1.0, 2.0, 3.5, 4.0, Instant.now().toEpochMilli()))); // Sending an event with the changed value in C should result in a new output on side output CD
        assertEquals(1, harness.getSideOutput(new OutputTag<>("output-a"){}).size());
        assertEquals(1, harness.getSideOutput(new OutputTag<>("output-b"){}).size());
        assertEquals(2, harness.getSideOutput(new OutputTag<>("output-cd"){}).size());
    }

    @Test
    public void outputOnSideOutputD() throws Exception{
        harness.processElement(new StreamRecord<>(new InputEvent("device_1", 1.0, 2.0, 3.0, 4.0, Instant.now().toEpochMilli())));
        harness.processElement(new StreamRecord<>(new InputEvent("device_1", 1.0, 2.0, 3.0, 4.5, Instant.now().toEpochMilli()))); // Sending an event with the changed value in D should result in a new output on side output CD
        assertEquals(1, harness.getSideOutput(new OutputTag<>("output-a"){}).size());
        assertEquals(1, harness.getSideOutput(new OutputTag<>("output-b"){}).size());
        assertEquals(2, harness.getSideOutput(new OutputTag<>("output-cd"){}).size());
    }

    @Test
    public void outputOnSideOutputCD() throws Exception{
        harness.processElement(new StreamRecord<>(new InputEvent("device_1", 1.0, 2.0, 3.0, 4.0, Instant.now().toEpochMilli())));
        harness.processElement(new StreamRecord<>(new InputEvent("device_1", 1.0, 2.0, 3.5, 4.5, Instant.now().toEpochMilli()))); // Sending an event with the changed value in C and D should result in a new output on side output CD
        assertEquals(1, harness.getSideOutput(new OutputTag<>("output-a"){}).size());
        assertEquals(1, harness.getSideOutput(new OutputTag<>("output-b"){}).size());
        assertEquals(2, harness.getSideOutput(new OutputTag<>("output-cd"){}).size());
    }
}