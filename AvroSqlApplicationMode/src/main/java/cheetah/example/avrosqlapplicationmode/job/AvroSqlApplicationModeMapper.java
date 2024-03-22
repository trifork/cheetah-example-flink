package cheetah.example.avrosqlapplicationmode.job;

import cheetah.example.avrosqlapplicationmode.model.InputEvent;
import cheetah.example.avrosqlapplicationmode.model.OutputEvent;
import org.apache.flink.api.common.functions.MapFunction;

/** AvroSqlApplicationModeMapper converts from InputEvent to OutputEvent. */
public class AvroSqlApplicationModeMapper implements MapFunction<InputEvent, OutputEvent> {
    private final String extraField;

    public AvroSqlApplicationModeMapper(final String extraField) {
        this.extraField = extraField;
    }

    @Override
    public OutputEvent map(final InputEvent inputEvent) {
        return new OutputEvent(inputEvent, extraField);
    }
}
