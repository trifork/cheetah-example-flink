package cheetah.example.function;

import cheetah.example.model.InputEvent;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.util.Collector;
import org.apache.flink.util.IterableUtils;

/**
 * The FlinkListStateMapper is an example of how to use the ListState to store a list of values between messages.
 * The example outputs the list for every two messages.
 */
public class FlinkListStatesMapper extends RichFlatMapFunction<InputEvent, Double[]> {

    private transient ListState<Double> values;

    /**
     * Outputs every two elements received in lists
     * @param value The input value.
     * @param out The collector for returning result values.
     */
    @Override
    public void flatMap(InputEvent value, Collector<Double[]> out) throws Exception {
        values.add(value.getValue());
        var iterator = values.get();
        int counter = 0;
        for (Double ignored : iterator) {
            counter++;
            if (counter == 2) {
                out.collect(IterableUtils.toStream(values.get()).toArray(Double[]::new));
                values.clear();
            }
        }
    }

    @Override
    public void open(Configuration config) {
        ListStateDescriptor<Double> descriptor =
                new ListStateDescriptor<>(
                        "values", // the state name
                        TypeInformation.of(new TypeHint<>() {
                        }));
        values = getRuntimeContext().getListState(descriptor);
    }

}
