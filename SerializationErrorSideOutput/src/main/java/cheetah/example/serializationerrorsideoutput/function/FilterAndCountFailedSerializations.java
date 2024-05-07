package cheetah.example.serializationerrorsideoutput.function;

import cheetah.example.serializationerrorsideoutput.model.InputEvent;
import org.apache.flink.api.common.functions.RichFilterFunction;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.metrics.Gauge;

/**
 * FilterAndCountFailedSerializations is a RichFilterFunction that checks if the DeviceId from in the message is null.
 * If it is null, it increments a metric counter called "FailedMessagesProcessed" and returns false,
 * otherwise it returns true
 */
public class FilterAndCountFailedSerializations extends RichFilterFunction<InputEvent> {
    private transient int messagesFailed = 0;

    @Override
    public void open(Configuration parameters) {
        getRuntimeContext()
                .getMetricGroup()
                .gauge("FailedMessagesProcessed", (Gauge<Integer>) () -> messagesFailed);
    }

    @Override
    public boolean filter(InputEvent value) {
        if (value == null) {
            messagesFailed++;
            return false;
        }
        return true;
    }
}
