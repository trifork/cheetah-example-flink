package cheetah.exmaple.job;

import cheetah.exmaple.model.avrorecord.InputEventAvro;
import cheetah.exmaple.model.avrorecord.OutputEventAvro;
import cheetah.exmaple.model.json.InputEvent;
import org.apache.flink.api.common.functions.MapFunction;

/** jsonToAvroMapper converts from InputEvent to OutputEvent. */
public class jsonToAvroMapper implements MapFunction<InputEvent, OutputEventAvro> {
    private final String extraField;

    public jsonToAvroMapper(final String extraField) {
        this.extraField = extraField;
    }

    @Override
    public OutputEventAvro map(final InputEvent InputEvent) {
        return new OutputEventAvro(InputEvent.getDeviceId(), InputEvent.getValue(), InputEvent.getTimestamp(), extraField);
    }
}
