package cheetah.example.enricher;

import cheetah.example.model.MergeTwoStreamsInputEventA;
import cheetah.example.model.MergeTwoStreamsInputEventB;
import cheetah.example.model.MergeTwoStreamsOutputEvent;
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
public class MergeTwoStreamsEnricher extends KeyedCoProcessFunction<String, MergeTwoStreamsInputEventA, MergeTwoStreamsInputEventB, MergeTwoStreamsOutputEvent> {

    private ValueState<MergeTwoStreamsOutputEvent> outputEventState;

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
    public void processElement1(MergeTwoStreamsInputEventA value, KeyedCoProcessFunction<String, MergeTwoStreamsInputEventA, MergeTwoStreamsInputEventB, MergeTwoStreamsOutputEvent>.Context ctx, Collector<MergeTwoStreamsOutputEvent> out) throws Exception {
        var output = new MergeTwoStreamsOutputEvent(value);
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
    public void processElement2(MergeTwoStreamsInputEventB value, KeyedCoProcessFunction<String, MergeTwoStreamsInputEventA, MergeTwoStreamsInputEventB, MergeTwoStreamsOutputEvent>.Context ctx, Collector<MergeTwoStreamsOutputEvent> out) throws Exception {
        MergeTwoStreamsOutputEvent output = outputEventState.value();
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
        ValueStateDescriptor<MergeTwoStreamsOutputEvent> descriptor =
                new ValueStateDescriptor<>(
                        "deviceIdState",
                        TypeInformation.of(new TypeHint<>() {
                        })
                );
        outputEventState = getRuntimeContext().getState(descriptor);
    }

}
