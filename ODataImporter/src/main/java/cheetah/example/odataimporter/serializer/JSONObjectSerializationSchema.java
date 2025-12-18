package cheetah.example.odataimporter.serializer;

import org.apache.flink.annotation.PublicEvolving;
import org.apache.flink.api.common.serialization.SerializationSchema;

/** SerializationSchema that serializes JSONObject. */
@PublicEvolving
public class JSONObjectSerializationSchema implements SerializationSchema<org.json.JSONObject> {

    private static final long serialVersionUID = 1L;

    public JSONObjectSerializationSchema() {
    }

    @Override
    public void open(InitializationContext context) {
    }

    @Override
    public byte[] serialize(org.json.JSONObject element) {
        return element.toString().getBytes();
    }
}
