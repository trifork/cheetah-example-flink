package cheetah.example.job;

import cheetah.example.model.avrorecord.EventAvro;
import cheetah.example.model.json.InputEvent;
import org.apache.flink.api.common.functions.MapFunction;

/** jsonToAvroMapper converts from InputEvent to OutputEvent. */
public class JsonToAvroMapper implements MapFunction<InputEvent, EventAvro> {

    public JsonToAvroMapper() {
    }

    @Override
    public EventAvro map(final InputEvent inputEvent) {
        return new EventAvro(inputEvent.getReadingTimestamp(), inputEvent.getAcousticNoise(), 
        inputEvent.getUnit(), inputEvent.getUnit1(), inputEvent.getUnit2(), inputEvent.getUnit3(), 
        inputEvent.getInfocodes(), inputEvent.getLoggedVolume1(),
        inputEvent.getMeterSerialNumber(), inputEvent.getHourCounter(), inputEvent.getVolume1() );
    }
}
