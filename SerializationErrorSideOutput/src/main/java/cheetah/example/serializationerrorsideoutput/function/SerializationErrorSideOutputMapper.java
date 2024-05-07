package cheetah.example.serializationerrorsideoutput.function;

import cheetah.example.serializationerrorsideoutput.model.InputEvent;
import cheetah.example.serializationerrorsideoutput.model.OutputEvent;
import org.apache.flink.api.common.functions.MapFunction;

/** SerializationErrorSideOutputMapper is a simple MapFunction that converts from InputEvent to OutputEvent. */
public class SerializationErrorSideOutputMapper implements MapFunction<InputEvent, OutputEvent> {
    private final String extraField;

    public SerializationErrorSideOutputMapper(final String extraField) {
        this.extraField = extraField;
    }

    @Override
    public OutputEvent map(final InputEvent inputEvent) {
        return new OutputEvent(inputEvent, extraField);
    }

}

