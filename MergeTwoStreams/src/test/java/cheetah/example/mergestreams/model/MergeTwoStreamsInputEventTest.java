package cheetah.example.mergestreams.model;

import cheetah.example.mergestreams.model.MergeTwoStreamsInputEventA;
import cheetah.example.mergestreams.model.MergeTwoStreamsInputEventB;
import org.junit.jupiter.api.Test;

import static org.apache.flink.types.PojoTestUtils.assertSerializedAsPojo;
import static org.apache.flink.types.PojoTestUtils.assertSerializedAsPojoWithoutKryo;

class MergeTwoStreamsInputEventTest {
    /*
     This test ensures that the input event class can be serialized without using the Kryo serializer. Similar tests
     should be implemented for any class that is serialized often, i.e. input/output models, objects that are stored in
     state, etc.

     Using the Kryo serializer infers a substantial performance decrease compared to alternative serializers, giving
     reason to this test. This happens most often with generic classes, which cannot usually be serialized with
     other, more efficient serializers.
     */
    @Test
    void isSerializedAsPojo() {
        assertSerializedAsPojo(MergeTwoStreamsInputEventA.class);
        assertSerializedAsPojoWithoutKryo(MergeTwoStreamsInputEventA.class);
        assertSerializedAsPojo(MergeTwoStreamsInputEventB.class);
        assertSerializedAsPojoWithoutKryo(MergeTwoStreamsInputEventB.class);
    }
}