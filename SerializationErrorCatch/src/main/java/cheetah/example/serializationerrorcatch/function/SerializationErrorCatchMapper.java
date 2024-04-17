package cheetah.example.serializationerrorcatch.function;

import cheetah.example.serializationerrorcatch.model.InputEvent;
import cheetah.example.serializationerrorcatch.model.OutputEvent;
import org.apache.flink.api.common.functions.MapFunction;

/** SerializationErrorCatchMapper is a simple MapFunction that converts from InputEvent to OutputEvent. */
public class SerializationErrorCatchMapper implements MapFunction<InputEvent, OutputEvent> {
    private final String extraField;

    public SerializationErrorCatchMapper(final String extraField) {
        this.extraField = extraField;
    }

    @Override
    public OutputEvent map(final InputEvent inputEvent) {
        return new OutputEvent(inputEvent, extraField);
    }

}

