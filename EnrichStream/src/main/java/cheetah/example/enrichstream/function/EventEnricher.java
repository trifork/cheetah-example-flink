package cheetah.example.enrichstream.function;

import cheetah.example.enrichstream.model.EnrichEvent;
import cheetah.example.enrichstream.model.InputEvent;
import cheetah.example.enrichstream.model.OutputEvent;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.TimeDomain;
import org.apache.flink.streaming.api.TimerService;
import org.apache.flink.streaming.api.functions.co.KeyedCoProcessFunction;
import org.apache.flink.util.Collector;

/**
 * Merge results from Stream A and B, by storing the last seen StateA for a given deviceId. When receiving a StateB, we then check if we have already seen a StateA, if so, we merge the two objects, and output the result.
 */
public class EventEnricher extends KeyedCoProcessFunction<String, EnrichEvent, InputEvent, OutputEvent> {

    private ValueState<EnrichEvent> currentEnrichEvent;

    /**
     * Stores StateA for further processing.
     *
     * @param enrichEvent The stream element
     * @param ctx   A {@link Context} that allows querying the timestamp of the element, querying the
     *              {@link TimeDomain} of the firing timer and getting a {@link TimerService} for registering
     *              timers and querying the time. The context is only valid during the invocation of this
     *              method, do not store it.
     * @param out   The collector to emit resulting elements to
     */
    @Override
    public void processElement1(EnrichEvent enrichEvent, KeyedCoProcessFunction<String, EnrichEvent, InputEvent, OutputEvent>.Context ctx, Collector<OutputEvent> out) throws Exception {
        currentEnrichEvent.update(enrichEvent);
    }

    /**
     * Checks whether we have a StateA for the given deviceId, if so, combines the two states and outputs them.
     *
     * @param inputEvent The stream element
     * @param ctx   A {@link Context} that allows querying the timestamp of the element, querying the
     *              {@link TimeDomain} of the firing timer and getting a {@link TimerService} for registering
     *              timers and querying the time. The context is only valid during the invocation of this
     *              method, do not store it.
     * @param out   The collector to emit resulting elements to
     */
    @Override
    public void processElement2(InputEvent inputEvent, KeyedCoProcessFunction<String, EnrichEvent, InputEvent, OutputEvent>.Context ctx, Collector<OutputEvent> out) throws Exception {
        EnrichEvent enrichEvent = this.currentEnrichEvent.value();
        if (enrichEvent != null) {
            out.collect(new OutputEvent(inputEvent, enrichEvent));
        }
    }

    /**
     * Sets up state storage for the given device.
     *
     * @param parameters The configuration containing the parameters attached to the contract.
     */
    @Override
    public void open(Configuration parameters) {
        ValueStateDescriptor<EnrichEvent> descriptor =
                new ValueStateDescriptor<>(
                        "deviceIdState",
                        TypeInformation.of(EnrichEvent.class)
                );
        currentEnrichEvent = getRuntimeContext().getState(descriptor);
    }
}