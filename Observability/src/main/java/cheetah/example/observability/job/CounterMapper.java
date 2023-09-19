package cheetah.example.observability.job;

import cheetah.example.observability.model.ObservabilityInputEvent;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.metrics.Counter;

public class CounterMapper extends RichMapFunction<ObservabilityInputEvent, ObservabilityInputEvent> {

    private transient Counter counter;

    @Override
    public void open(Configuration config) {
        this.counter = getRuntimeContext()
                .getMetricGroup()
                .counter("CountOfMessages");
    }

    @Override
    public ObservabilityInputEvent map(ObservabilityInputEvent value) throws Exception {
        this.counter.inc();
        return value;
    }
}
