package cheetah.example.observability.function;

import cheetah.example.observability.model.InputEvent;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.metrics.Counter;

/**
 * The purpose of the CounterMappper is to count the number of messages passing through.
 */
public class CounterMapper extends RichMapFunction<InputEvent, InputEvent> {

    private transient Counter counter;

    @Override
    public void open(Configuration config) {
        //Create the counter, note that Flink creates a Gauge for this instead of a counter
        this.counter = getRuntimeContext()
                .getMetricGroup()
                .counter("CountOfMessages");
    }

    @Override
    public InputEvent map(InputEvent value) throws Exception {
        this.counter.inc();
        return value;
    }
}
