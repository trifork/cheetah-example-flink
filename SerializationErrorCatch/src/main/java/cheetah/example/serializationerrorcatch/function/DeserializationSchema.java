package cheetah.example.serializationerrorcatch.function;

import cheetah.example.serializationerrorcatch.model.InputEvent;
import org.apache.flink.formats.json.JsonDeserializationSchema;

import java.io.IOException;

public class DeserializationSchema<T> extends JsonDeserializationSchema<T> {

    public DeserializationSchema(Class<T> clazz) {
        super(clazz);
    }
    @Override
    public T deserialize(byte[] message) {
        try {
            return super.deserialize(message);
        } catch (IOException e) {
            System.out.println("The message failed to be serialized with error message =>" + e);
            return (T) new InputEvent();
        }

    }
}
