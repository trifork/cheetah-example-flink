package cheetah.example.serializationerrorcatch.function;

import cheetah.example.serializationerrorcatch.model.InputEvent;
import org.apache.flink.api.common.functions.RichFilterFunction;
import org.apache.flink.metrics.Gauge;

/** FilterAndCountFailedSerializations is a RichFilterFunction that checks if the DeviceId from in the message is null.
 * If it is null, it increments a metric counter called "FailedMessagesProcessed" and returns false,
 * otherwise it returns true */
public class FilterAndCountFailedSerializations extends RichFilterFunction<InputEvent> {
    private transient int messagesFailed = 0;

    @Override
    public boolean filter(InputEvent value) {

        if (value == null) {
            messagesFailed++;
            getRuntimeContext()
                    .getMetricGroup()
                    .gauge("FailedMessagesProcessed", (Gauge<Integer>) () -> messagesFailed);
            return false;
        }
        return true;
    }
}
