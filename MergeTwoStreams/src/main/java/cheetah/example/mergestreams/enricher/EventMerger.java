package cheetah.example.mergestreams.enricher;

import cheetah.example.mergestreams.model.InputEventA;
import cheetah.example.mergestreams.model.InputEventB;
import cheetah.example.mergestreams.model.OutputEvent;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.co.KeyedCoProcessFunction;
import org.apache.flink.util.Collector;

/**
 * Merge results from Stream A and B, by storing the last seen StateA for a given deviceId. When receiving a StateB, we then check if we have already seen a StateA, if so, we merge the two objects, and output the result.
 */
public class EventMerger extends KeyedCoProcessFunction<String, InputEventA, InputEventB, OutputEvent> {

    private ValueState<OutputEvent> outputEventState;

    /**
     * Stores StateA for further processing.
     * @param value The stream element
     * @param ctx A {@link Context} that allows querying the timestamp of the element, querying the
     *     {@link TimeDomain} of the firing timer and getting a {@link TimerService} for registering
     *     timers and querying the time. The context is only valid during the invocation of this
     *     method, do not store it.
     * @param out The collector to emit resulting elements to
     */
    @Override
    public void processElement1(InputEventA value, KeyedCoProcessFunction<String, InputEventA, InputEventB, OutputEvent>.Context ctx, Collector<OutputEvent> out) throws Exception {
        var output = new OutputEvent(value);
        outputEventState.update(output);
    }

    /**
     * Checks whether we have a StateA for the given deviceId, if so, combines the two states and outputs them.
     * @param value The stream element
     * @param ctx A {@link Context} that allows querying the timestamp of the element, querying the
     *     {@link TimeDomain} of the firing timer and getting a {@link TimerService} for registering
     *     timers and querying the time. The context is only valid during the invocation of this
     *     method, do not store it.
     * @param out The collector to emit resulting elements to
     */
    @Override
    public void processElement2(InputEventB value, KeyedCoProcessFunction<String, InputEventA, InputEventB, OutputEvent>.Context ctx, Collector<OutputEvent> out) throws Exception {
        OutputEvent output = outputEventState.value();
        if(output != null) {
            output.setValueB(value.getValue());
            out.collect(output);
        }
    }

    /**
     * Sets up state storage for the given device
     * @param parameters The configuration containing the parameters attached to the contract.
     */
    @Override
    public void open(Configuration parameters) throws Exception {
        ValueStateDescriptor<OutputEvent> descriptor =
                new ValueStateDescriptor<>(
                        "deviceIdState",
                        TypeInformation.of(new TypeHint<>() {
                        })
                );
        outputEventState = getRuntimeContext().getState(descriptor);
    }

}
