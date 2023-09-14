package cheetah.example.job;

import cheetah.example.enricher.MergeTwoStreamsEnricher;
import cheetah.example.model.MergeTwoStreamsInputEventA;
import cheetah.example.model.MergeTwoStreamsInputEventB;
import cheetah.example.model.MergeTwoStreamsOutputEvent;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.streaming.api.operators.co.KeyedCoProcessOperator;
import org.apache.flink.streaming.runtime.streamrecord.StreamRecord;
import org.apache.flink.streaming.util.KeyedTwoInputStreamOperatorTestHarness;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MergeTwoStreamsJobTest {

    private KeyedTwoInputStreamOperatorTestHarness<String, MergeTwoStreamsInputEventA, MergeTwoStreamsInputEventB, MergeTwoStreamsOutputEvent>  harness;

    //Setup test harness for all the below tests
    @BeforeEach
    public void setup() throws Exception {
        var sut = new MergeTwoStreamsEnricher();
        var operator = new KeyedCoProcessOperator<>(sut);
        harness = new KeyedTwoInputStreamOperatorTestHarness<>(operator, MergeTwoStreamsInputEventA::getDeviceId, MergeTwoStreamsInputEventB::getDeviceId, Types.STRING);
        harness.setup();
        harness.open();
    }

    @Test
    public void verifyNoOutputIfOnlyStreamAObjects() throws Exception {
        harness.processElement1(new StreamRecord<>(new MergeTwoStreamsInputEventA("device", 1, 0)));
        Assertions.assertTrue(harness.getOutput().isEmpty());
    }

    @Test
    public void verifyNoOutputIfOnlyStreamBObjects() throws Exception {
        harness.processElement2(new StreamRecord<>(new MergeTwoStreamsInputEventB("device", 1, 0)));
        Assertions.assertTrue(harness.getOutput().isEmpty());
    }

    @Test
    public void verifyNoOutputIfDeviceIdsDoNotMatch() throws Exception {
        harness.processElement1(new StreamRecord<>(new MergeTwoStreamsInputEventA("device", 1, 0)));
        harness.processElement2(new StreamRecord<>(new MergeTwoStreamsInputEventB("device2", 1, 0)));
        Assertions.assertTrue(harness.getOutput().isEmpty());
    }

    @Test
    public void verifyOutputIfDeviceIdsDoMatch() throws Exception {
        harness.processElement1(new StreamRecord<>(new MergeTwoStreamsInputEventA("device", 1, 0)));
        harness.processElement2(new StreamRecord<>(new MergeTwoStreamsInputEventB("device", 2, 0)));
        Assertions.assertFalse(harness.getOutput().isEmpty());
        var element = harness.extractOutputValues().get(0);
        Assertions.assertEquals("device", element.getDeviceId());
        Assertions.assertEquals(1, element.getValueA());
        Assertions.assertEquals(2, element.getValueB());
    }

}
