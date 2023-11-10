package cheetah.example.serializationerrorcatch.function;

import cheetah.example.serializationerrorcatch.model.InputEvent;
import cheetah.example.serializationerrorcatch.model.OutputEvent;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.RichFilterFunction;
import org.apache.flink.metrics.Gauge;

/** SerializationErrorCatchMapper converts from InputEvent to OutputEvent. */
public class SerializationErrorCatchMapper implements MapFunction<InputEvent, OutputEvent> {
    private final String extraField;

    public SerializationErrorCatchMapper(final String extraField) {
        this.extraField = extraField;
    }

    @Override
    public OutputEvent map(final InputEvent InputEvent) {
        return new OutputEvent(InputEvent, extraField);
    }

}

