package cheetah.example.tumblingwindow.model;

import cheetah.example.tumblingwindow.tumblingwindow.model.InputEvent;
import org.junit.jupiter.api.Test;

import static org.apache.flink.types.PojoTestUtils.assertSerializedAsPojo;
import static org.apache.flink.types.PojoTestUtils.assertSerializedAsPojoWithoutKryo;

class TumblingWindowInputEventTest {
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
        assertSerializedAsPojo(InputEvent.class);
        assertSerializedAsPojoWithoutKryo(InputEvent.class);
    }
}
