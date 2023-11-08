package cheetah.example.observability.function;

import cheetah.example.observability.model.InputEvent;
import com.trifork.cheetah.processing.metrics.Histogram;
import com.trifork.cheetah.processing.metrics.QuantileHistogram;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.configuration.Configuration;

public class CheetahHistogramMapper extends RichMapFunction<InputEvent, InputEvent> {

    private transient QuantileHistogram histogram;

    @Override
    public void open(Configuration config) {
        this.histogram = new QuantileHistogram(
                "Cheetah Histogram",
                getRuntimeContext(),
                Histogram.getBuckets(5, 20),
                new double[]{0.05, 0.25, 0.5, 0.75, 0.95});

    }

    @Override
    public InputEvent map(InputEvent value) throws Exception {
        histogram.update((int) value.getValue());
        return value;
    }
}
