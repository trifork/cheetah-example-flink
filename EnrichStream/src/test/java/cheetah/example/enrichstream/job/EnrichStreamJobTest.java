package cheetah.example.enrichstream.job;

import cheetah.example.enrichstream.function.EventEnricher;
import cheetah.example.enrichstream.model.EnrichEvent;
import cheetah.example.enrichstream.model.InputEvent;
import cheetah.example.enrichstream.model.OutputEvent;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.streaming.api.operators.co.KeyedCoProcessOperator;
import org.apache.flink.streaming.runtime.streamrecord.StreamRecord;
import org.apache.flink.streaming.util.KeyedTwoInputStreamOperatorTestHarness;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EnrichStreamJobTest {

    private KeyedTwoInputStreamOperatorTestHarness<String, EnrichEvent, InputEvent, OutputEvent>  harness;

    //Setup test harness for all the below tests
    @BeforeEach
    void setup() throws Exception {
        EventEnricher sut = new EventEnricher();
        KeyedCoProcessOperator<String, EnrichEvent, InputEvent, OutputEvent> operator = new KeyedCoProcessOperator<>(sut);
        harness = new KeyedTwoInputStreamOperatorTestHarness<>(operator, EnrichEvent::getDeviceId, InputEvent::getDeviceId, Types.STRING);
        harness.setup();
        harness.open();
    }

    @Test
    void verifyNoOutputIfOnlyStreamAObjects() throws Exception {
        harness.processElement1(new StreamRecord<>(new EnrichEvent("device", 1, 0)));
        Assertions.assertTrue(harness.getOutput().isEmpty());
    }

    @Test
    void verifyNoOutputIfOnlyStreamBObjects() throws Exception {
        harness.processElement2(new StreamRecord<>(new InputEvent("device", 1, 0)));
        Assertions.assertTrue(harness.getOutput().isEmpty());
    }

    @Test
    void verifyNoOutputIfDeviceIdsDoNotMatch() throws Exception {
        harness.processElement1(new StreamRecord<>(new EnrichEvent("device", 1, 0)));
        harness.processElement2(new StreamRecord<>(new InputEvent("device2", 1, 0)));
        Assertions.assertTrue(harness.getOutput().isEmpty());
    }

    @Test
    void verifyOutputIfDeviceIdsDoMatch() throws Exception {
        harness.processElement1(new StreamRecord<>(new EnrichEvent("device", 1, 0)));
        harness.processElement2(new StreamRecord<>(new InputEvent("device", 2, 0)));
        Assertions.assertFalse(harness.getOutput().isEmpty());
        OutputEvent element = harness.extractOutputValues().get(0);
        Assertions.assertEquals("device", element.getDeviceId());
        Assertions.assertEquals(2, element.getValue());
        Assertions.assertEquals(1, element.getEnrichValue());
    }
}