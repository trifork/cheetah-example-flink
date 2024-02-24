package fepa.job;

import fepa.model.InputEvent;
import fepa.model.OutputEvent;
import org.apache.flink.api.common.functions.MapFunction;

/** fepaMapper converts from InputEvent to OutputEvent. */
public class fepaMapper implements MapFunction<InputEvent, OutputEvent> {
    private final String extraField;

    public fepaMapper(final String extraField) {
        this.extraField = extraField;
    }

    @Override
    public OutputEvent map(final InputEvent InputEvent) {
        return new OutputEvent(InputEvent, extraField);
    }
}
