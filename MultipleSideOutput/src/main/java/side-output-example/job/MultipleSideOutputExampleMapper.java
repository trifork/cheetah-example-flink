package side-output-example.job;

import side-output-example.model.MultipleSideOutputExampleInputEvent;
import side-output-example.model.MultipleSideOutputExampleOutputEvent;
import org.apache.flink.api.common.functions.MapFunction;

/** MultipleSideOutputExampleMapper converts from MultipleSideOutputExampleInputEvent to MultipleSideOutputExampleOutputEvent. */
public class MultipleSideOutputExampleMapper implements MapFunction<MultipleSideOutputExampleInputEvent, MultipleSideOutputExampleOutputEvent> {
    private final String extraField;

    public MultipleSideOutputExampleMapper(final String extraField) {
        this.extraField = extraField;
    }

    @Override
    public MultipleSideOutputExampleOutputEvent map(final MultipleSideOutputExampleInputEvent MultipleSideOutputExampleInputEvent) {
        return new MultipleSideOutputExampleOutputEvent(MultipleSideOutputExampleInputEvent, extraField);
    }
}
