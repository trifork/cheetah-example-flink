package cheetah.example.transformandstore.job;

import cheetah.example.transformandstore.model.InputEvent;
import cheetah.example.transformandstore.model.OutputEvent;
import org.apache.flink.api.common.functions.MapFunction;

/**
 * Straightforward transformation of the InputEvent data by appending a status based on the object's value field.
 */
public class TransformAndStoreMapper implements MapFunction<InputEvent, OutputEvent> {
    @Override
    public OutputEvent map(final InputEvent inputEvent) {
        if (inputEvent.getValue() <= 50) {
            return new OutputEvent(inputEvent, "Value is too low");
        }
        if (inputEvent.getValue() <= 100) {
            return new OutputEvent(inputEvent, "Value is acceptable");
        }
        return new OutputEvent(inputEvent, "Value is too high");
    }
}
