package cheetah.example.flinkstates.function;

import cheetah.example.flinkstates.model.InputEvent;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.api.common.state.AggregatingState;
import org.apache.flink.api.common.state.AggregatingStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.util.Collector;

/**
 * The FlinkAggregatingStatesMapper is an example of using the AggregatingState to calculate the sum of the incoming elements value.
 * The AggregatingState can contain other elements during aggregation than the one returned. This is in contrast to the ReducingState,
 * where the temporary storage must be the same as the final result
 */
public class FlinkAggregatingStatesMapper extends RichFlatMapFunction<InputEvent, Double> {
    private transient AggregatingState<InputEvent, Double> sum;

    /**
     * Mapping the incoming messages to the sum of all elements seen.
     *
     * @param value The input value.
     * @param out   The collector for returning result values.
     */
    @Override
    public void flatMap(InputEvent value, Collector<Double> out) throws Exception {
        sum.add(value);
        out.collect(sum.get());
    }

    @Override
    public void open(Configuration config) {
        AggregatingStateDescriptor<InputEvent, Double, Double> descriptor =
                new AggregatingStateDescriptor<>(
                        "values", // the state name
                        getAggregateFunction(),
                        TypeInformation.of(new TypeHint<>() {
                        }));
        sum = getRuntimeContext().getAggregatingState(descriptor);
    }

    /**
     * Returns the aggregateFunction telling how to aggregate the elements.
     */
    private static AggregateFunction<InputEvent, Double, Double> getAggregateFunction() {
        return new AggregateFunction<>() {
            @Override
            public Double createAccumulator() {
                return 0.0;
            }

            @Override
            public Double add(InputEvent value, Double accumulator) {
                return accumulator + value.getValue();
            }

            @Override
            public Double getResult(Double accumulator) {
                return accumulator;
            }

            @Override
            public Double merge(Double a, Double b) {
                return a + b;
            }
        };
    }

}
