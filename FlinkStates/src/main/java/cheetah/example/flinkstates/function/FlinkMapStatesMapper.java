package cheetah.example.flinkstates.function;

import cheetah.example.flinkstates.model.InputEvent;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.api.common.state.MapState;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.util.Collector;

/**
 * The FlinkMapStatesMapper is an example of using the MapState to calculate the sum of values received per deviceId.
 */
public class FlinkMapStatesMapper extends RichFlatMapFunction<InputEvent, Double> {

    private transient MapState<String, Double> sumPerDevice;

    /**
     * Outputs the sum of values for the given device
     * @param value The input value.
     * @param out The collector for returning result values.
     */
    @Override
    public void flatMap(InputEvent value, Collector<Double> out) throws Exception {
        var sum = sumPerDevice.get(value.getDeviceId());
        if (sum == null) {
            sum = 0.0;
        }
        sum += value.getValue();
        sumPerDevice.put(value.getDeviceId(), sum);
        out.collect(sum);
    }

    @Override
    public void open(Configuration config) {
        MapStateDescriptor<String, Double> descriptor =
                new MapStateDescriptor<String, Double>(
                        "values", // the state name
                        TypeInformation.of(new TypeHint<>() {
                        }),
                        TypeInformation.of(new TypeHint<>() {
                        }));
        sumPerDevice = getRuntimeContext().getMapState(descriptor);
    }

}
