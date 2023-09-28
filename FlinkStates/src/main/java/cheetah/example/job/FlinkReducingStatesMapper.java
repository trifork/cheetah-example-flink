package cheetah.example.job;

import cheetah.example.model.FlinkStatesInputEvent;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.api.common.state.ReducingState;
import org.apache.flink.api.common.state.ReducingStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.util.Collector;

/**
 * The FlinkReducingStatesMapper is an example of using the ReducingState to calculate the sum of the elements.
 * The ReducingState must contain values like the one returned. This is in contrast to the AggregatingState,
 * where the temporary storage can be different from the final result
 */
public class FlinkReducingStatesMapper extends RichFlatMapFunction<FlinkStatesInputEvent, Double> {

    private transient ReducingState<Double> sum;

    @Override
    public void flatMap(FlinkStatesInputEvent value, Collector<Double> out) throws Exception {
        sum.add(value.getValue());
        out.collect(sum.get());
    }

    @Override
    public void open(Configuration config) {
        ReducingStateDescriptor<Double> descriptor =
                new ReducingStateDescriptor<Double>(
                        "values", // the state name
                        getReduceFunction(),
                        TypeInformation.of(new TypeHint<>() {
                        }));
        sum = getRuntimeContext().getReducingState(descriptor);
    }

    private static ReduceFunction<Double> getReduceFunction() {
        return (a, b) -> Double.sum(a, b);
    }

}
