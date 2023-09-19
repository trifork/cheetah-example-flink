package tumblingWindow.job;

import tumblingWindow.model.TumblingWindowInputEvent;
import tumblingWindow.model.TumblingWindowOutputEvent;
import org.apache.flink.api.common.functions.MapFunction;

/** TumblingWindowMapper converts from TumblingWindowInputEvent to TumblingWindowOutputEvent. */
public class TumblingWindowMapper implements MapFunction<TumblingWindowInputEvent, TumblingWindowOutputEvent> {
    private final String extraField;

    public TumblingWindowMapper(final String extraField) {
        this.extraField = extraField;
    }

    @Override
    public TumblingWindowOutputEvent map(final TumblingWindowInputEvent TumblingWindowInputEvent) {
        return new TumblingWindowOutputEvent(TumblingWindowInputEvent, extraField);
    }
}
