package cheetah.example.serializationerrorcatch.serde;

import org.apache.flink.formats.json.JsonDeserializationSchema;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/** DeserializationSchema is a custom deserialization schema that extends the JsonDeserializationSchema.
 * It is used to catch deserialization errors and handle them in a tailored manner.
 * In this case, it returns null if the deserialization fails and logs an error message. */
public class DeserializationSchema<T> extends JsonDeserializationSchema<T> {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    public DeserializationSchema(Class<T> clazz) {
        super(clazz);
    }

    @Override
    public T deserialize(byte[] message) {
        try {
            return super.deserialize(message);
        } catch (IOException e) {
            logger.warn("The message failed to be serialized with error message => " + e);
            return null;
        }
    }

    @Override
    public void deserialize(byte[] message, Collector<T> out)  {
        T deserialize = deserialize(message);
        out.collect(deserialize);
    }
}
