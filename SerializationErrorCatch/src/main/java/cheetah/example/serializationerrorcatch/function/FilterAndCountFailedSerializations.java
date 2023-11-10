package cheetah.example.serializationerrorcatch.function;

import cheetah.example.serializationerrorcatch.model.InputEvent;
import org.apache.flink.api.common.functions.RichFilterFunction;
import org.apache.flink.metrics.Gauge;

public class FilterAndCountFailedSerializations extends RichFilterFunction<InputEvent> {
    private transient int messagesFailed = 0;

    @Override
    public boolean filter(InputEvent value) {

        if (value.getDeviceId() == null) {
            messagesFailed++;
            getRuntimeContext()
                    .getMetricGroup()
                    .gauge("FailedMessagesProcessed", (Gauge<Integer>) () -> messagesFailed);
            return false;
        }
        return true;
    }
}
