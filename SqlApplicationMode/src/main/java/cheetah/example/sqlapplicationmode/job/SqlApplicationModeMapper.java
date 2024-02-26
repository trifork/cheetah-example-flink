package cheetah.example.sqlapplicationmode.job;

import cheetah.example.sqlapplicationmode.model.InputEvent;
import cheetah.example.sqlapplicationmode.model.OutputEvent;
import org.apache.flink.api.common.functions.MapFunction;

/** SqlApplicationModeMapper converts from InputEvent to OutputEvent. */
public class SqlApplicationModeMapper implements MapFunction<InputEvent, OutputEvent> {
    private final String extraField;

    public SqlApplicationModeMapper(final String extraField) {
        this.extraField = extraField;
    }

    @Override
    public OutputEvent map(final InputEvent InputEvent) {
        return new OutputEvent(InputEvent, extraField);
    }
}
