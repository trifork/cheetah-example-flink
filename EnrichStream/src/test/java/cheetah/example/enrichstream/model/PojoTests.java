package cheetah.example.enrichstream.model;

import org.junit.jupiter.api.Test;

import static org.apache.flink.types.PojoTestUtils.assertSerializedAsPojoWithoutKryo;

class PojoTests {
    @Test
    void isSerializedAsPojo() {
        assertSerializedAsPojoWithoutKryo(EnrichingEvent.class);
        assertSerializedAsPojoWithoutKryo(InputEvent.class);
        assertSerializedAsPojoWithoutKryo(OutputEvent.class);
    }
}
