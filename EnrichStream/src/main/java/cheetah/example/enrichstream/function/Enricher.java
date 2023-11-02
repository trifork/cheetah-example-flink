package cheetah.example.enrichstream.function;

import cheetah.example.enrichstream.model.EnrichingEvent;
import cheetah.example.enrichstream.model.InputEvent;
import cheetah.example.enrichstream.model.OutputEvent;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.co.KeyedCoProcessFunction;
import org.apache.flink.util.Collector;

/**
 * Merge results from Stream A and B, by storing the last seen StateA for a given deviceId. When receiving a StateB, we then check if we have already seen a StateA, if so, we merge the two objects, and output the result.
 */
public class Enricher extends KeyedCoProcessFunction<String, EnrichingEvent, InputEvent, OutputEvent> {

    private ValueState<EnrichingEvent> previousEvent;

    /**
     * Stores StateA for further processing.
     */
    @Override
    public void processElement1(EnrichingEvent input, KeyedCoProcessFunction<String, EnrichingEvent, InputEvent, OutputEvent>.Context ctx, Collector<OutputEvent> out) throws Exception {
        previousEvent.update(input);
    }

    /**
     * Checks whether we have a StateA for the given deviceId, if so, combines the two states and outputs them.
     */
    @Override
    public void processElement2(InputEvent value, KeyedCoProcessFunction<String, EnrichingEvent, InputEvent, OutputEvent>.Context ctx, Collector<OutputEvent> out) throws Exception {
        EnrichingEvent previous = previousEvent.value();
        if (previous != null) {
            out.collect(new OutputEvent(previous, value.getValue()));
        }
    }

    /**
     * Sets up state storage for the given device
     * @param parameters The configuration containing the parameters attached to the contract.
     */
    @Override
    public void open(Configuration parameters) throws Exception {
        ValueStateDescriptor<EnrichingEvent> descriptor =
                new ValueStateDescriptor<>(
                        "deviceIdState",
                        TypeInformation.of(EnrichingEvent.class)
                );
        previousEvent = getRuntimeContext().getState(descriptor);
    }

}
