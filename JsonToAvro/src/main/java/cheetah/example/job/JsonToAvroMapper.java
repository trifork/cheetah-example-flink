package cheetah.example.job;

import cheetah.example.model.avrorecord.OutputEventAvro;
import cheetah.example.model.json.InputEvent;
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
