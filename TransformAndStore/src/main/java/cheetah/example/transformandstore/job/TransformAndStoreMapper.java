package cheetah.example.transformandstore.job;

import cheetah.example.transformandstore.model.InputEvent;
import cheetah.example.transformandstore.model.OutputEvent;
import org.apache.flink.api.common.functions.MapFunction;

/** TransformAndStoreMapper converts from InputEvent to OutputEvent. */
public class TransformAndStoreMapper implements MapFunction<InputEvent, OutputEvent> {
    @Override
    public OutputEvent map(final InputEvent InputEvent) {
        if (InputEvent.getValue() <= 50)
        {
            return new OutputEvent(InputEvent, "Value is too low");
        }
        else if (InputEvent.getValue() > 50 && InputEvent.getValue() <= 100)
        {
            return new OutputEvent(InputEvent, "Value is good");
        }
        return new OutputEvent(InputEvent, "Value is too high");
    }
}
