package cheetah.example.multiplesideoutput.model;

import org.junit.jupiter.api.Test;

import static org.apache.flink.types.PojoTestUtils.assertSerializedAsPojo;
import static org.apache.flink.types.PojoTestUtils.assertSerializedAsPojoWithoutKryo;

class MultipleSideOutputExampleOutputEventTest {
    @Test
    void isSerializedAsPojo() {
        assertSerializedAsPojo(OutputEvent.class);
        assertSerializedAsPojoWithoutKryo(OutputEvent.class);
    }
}

