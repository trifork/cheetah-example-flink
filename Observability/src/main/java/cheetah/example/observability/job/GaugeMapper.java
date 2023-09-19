package cheetah.example.observability.job;

import cheetah.example.observability.model.ObservabilityInputEvent;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.metrics.Gauge;

public class GaugeMapper extends RichMapFunction<ObservabilityInputEvent, ObservabilityInputEvent> {

    private transient int messagesProcessed = 0;

    @Override
    public void open(Configuration config) {
        getRuntimeContext()
                .getMetricGroup()
                .gauge("MessagesProcessed", new Gauge<Integer>() {

                    @Override
                    public Integer getValue() {
                        return messagesProcessed;
                    }
                });
    }

    @Override
    public ObservabilityInputEvent map(ObservabilityInputEvent value) throws Exception {
        this.messagesProcessed++;
        return value;
    }
}
