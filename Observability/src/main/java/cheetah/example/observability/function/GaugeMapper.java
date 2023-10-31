package cheetah.example.observability.function;

import cheetah.example.observability.model.InputEvent;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.metrics.Gauge;

/**
 * The purpose of the GaugeMapper is to count the number of messages processed
 */
public class GaugeMapper extends RichMapFunction<InputEvent, InputEvent> {

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
    public InputEvent map(InputEvent value) throws Exception {
        this.messagesProcessed++;
        return value;
    }
}
