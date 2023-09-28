package cheetah.example.job;

import cheetah.example.model.FlinkStatesInputEvent;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.util.Collector;

/**
 * The FlinkValueStatesMapper is an example of using the ValueState to calculate the average of every two messages seen.
 */
public class FlinkValueStatesMapper extends RichFlatMapFunction<FlinkStatesInputEvent, Double> {

    private transient ValueState<Tuple2<Long, Double>> sum;

    @Override
    public void flatMap(FlinkStatesInputEvent value, Collector<Double> out) throws Exception {
        var currentSum = sum.value();
        if (currentSum == null) {
            currentSum = new Tuple2<>(0L, 0.0);
        }
        currentSum.f0 += 1;
        currentSum.f1 += value.getValue();
        sum.update(currentSum);
        if (currentSum.f0 == 2) {
            sum.clear();
            out.collect(currentSum.f1 / currentSum.f0);
        }
    }

    @Override
    public void open(Configuration config) {
        ValueStateDescriptor<Tuple2<Long, Double>> descriptor =
                new ValueStateDescriptor<>(
                        "average", // the state name
                        TypeInformation.of(new TypeHint<>() {
                        }));
        sum = getRuntimeContext().getState(descriptor);
    }

}
