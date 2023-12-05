package cheetah.exmaple.job;

import cheetah.exmaple.model.avrorecord.OutputEventAvro;
import cheetah.exmaple.model.json.InputEvent;
import org.apache.flink.api.common.functions.MapFunction;

/** jsonToAvroMapper converts from InputEvent to OutputEvent. */
public class JsonToAvroMapper implements MapFunction<InputEvent, OutputEventAvro> {
    private final String extraField;

    public JsonToAvroMapper(final String extraField) {
        this.extraField = extraField;
    }

    @Override
    public OutputEventAvro map(final InputEvent inputEvent) {
        return new OutputEventAvro(inputEvent.getDeviceId(), inputEvent.getValue(), inputEvent.getTimestamp(), extraField);
    }
}
