package cheetah.example.job;

import cheetah.example.model.avrorecord.InputEventAvro;
import cheetah.example.model.json.OutputEventJson;
import org.apache.flink.api.common.functions.MapFunction;

/** AvroToJsonMapper converts from InputEvent to OutputEvent. */
public class AvroToJsonMapper implements MapFunction<InputEventAvro, OutputEventJson> {

    @Override
    public OutputEventJson map(final InputEventAvro inputEventAvro) {
        return new OutputEventJson(inputEventAvro.getDeviceId(), inputEventAvro.getValue(), inputEventAvro.getTimestamp());
    }
}
