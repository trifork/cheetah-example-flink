package cheetah.example.observability.function;

import cheetah.example.observability.model.InputEvent;
import com.codahale.metrics.SlidingWindowReservoir;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.dropwizard.metrics.DropwizardHistogramWrapper;
import org.apache.flink.metrics.Histogram;

/**
 * The purpose of the HistogramMapper is to make a Histogram over the values in the messages.
 */
public class HistogramMapper extends RichMapFunction<InputEvent, InputEvent> {

    private Histogram histogram;

    @Override
    public void open(Configuration config) {
        com.codahale.metrics.Histogram dropwizardHistogram =
                new com.codahale.metrics.Histogram(new SlidingWindowReservoir(500));
        this.histogram = getRuntimeContext()
                .getMetricGroup()
                .histogram("ValueSpread", new DropwizardHistogramWrapper(dropwizardHistogram));
    }

    @Override
    public InputEvent map(InputEvent value) throws Exception {
        this.histogram.update((long) value.getValue());
        return value;
    }
}
