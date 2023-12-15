package cheetah.example.multiplesideoutput.function;

import cheetah.example.multiplesideoutput.job.MultipleSideOutputExampleJob;
import cheetah.example.multiplesideoutput.model.InputEvent;
import cheetah.example.multiplesideoutput.model.OutputEvent2;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.TimeDomain;
import org.apache.flink.streaming.api.TimerService;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;
import cheetah.example.multiplesideoutput.model.OutputEvent;

/**
 * Takes an input event and checks the values of the event, depending on the values it produces a side output.
 * Each value is stored in the state.
 */
public class MultipleSideOutputExampleProcess extends KeyedProcessFunction<String, InputEvent, InputEvent> {

    private transient ValueState<OutputEvent> stateA;
    private transient ValueState<OutputEvent> stateB;
    private transient ValueState<OutputEvent2> stateCD;

    /**
     * Stores state A, B and CD - these are based on the values of the same name.
     *
     * @param inputEvent The incoming event.
     * @param context    A {@link Context} that allows querying the timestamp of the element, querying the
     *                   {@link TimeDomain} of the firing timer and getting a {@link TimerService} for registering
     *                   timers and querying the time. The context is only valid during the invocation of this
     *                   method, do not store it.
     * @param collector  The main output - it's not used in this example.
     * @throws Exception
     */
    @Override
    public void processElement(InputEvent inputEvent, KeyedProcessFunction<String, InputEvent, InputEvent>.Context context, Collector<InputEvent> collector) throws Exception {
        /* Update the states if the event id hasn't been seen before.
         * Or if the state is different from the last known state.
         * Then output the given event onto its corresponding side output
         */
        if (stateA.value() == null ||
                stateA.value().getValue() != inputEvent.getValueA()) {
            OutputEvent outputEvent = new OutputEvent(
                    inputEvent.getDeviceId(),
                    inputEvent.getValueA(),
                    inputEvent.getTimestamp()
            );
            context.output(MultipleSideOutputExampleJob.OUTPUT_A, outputEvent);
            stateA.update(outputEvent);
        }

        if (stateB.value() == null ||
                stateB.value().getValue() != inputEvent.getValueB()) {
            OutputEvent outputEvent = new OutputEvent(
                    inputEvent.getDeviceId(),
                    inputEvent.getValueB(),
                    inputEvent.getTimestamp()
            );
            context.output(MultipleSideOutputExampleJob.OUTPUT_B, outputEvent);
            stateB.update(outputEvent);
        }

        if (stateCD.value() == null ||
                stateCD.value().valueC != inputEvent.getValueC() ||
                stateCD.value().valueD != inputEvent.getValueD()) {
            OutputEvent2 outputEvent2 = new OutputEvent2(
                    inputEvent.getDeviceId(),
                    inputEvent.getValueC(),
                    inputEvent.getValueD(),
                    inputEvent.getTimestamp()
            );
            context.output(MultipleSideOutputExampleJob.OUTPUT_CD, outputEvent2);
            stateCD.update(outputEvent2);
        }

    }

    /**
     * Set up all the states store for the given values.
     *
     * @param parameters The configuration containing the parameters attached to the contract.
     * @throws Exception
     */
    @Override
    public void open(Configuration parameters) throws Exception {
        ValueStateDescriptor<OutputEvent> descriptorA = new ValueStateDescriptor<>(
                "stateA",
                Types.POJO(OutputEvent.class)
        );
        stateA = getRuntimeContext().getState(descriptorA);

        ValueStateDescriptor<OutputEvent> descriptorB = new ValueStateDescriptor<>(
                "stateB",
                Types.POJO(OutputEvent.class)
        );
        stateB = getRuntimeContext().getState(descriptorB);

        ValueStateDescriptor<OutputEvent2> descriptorC = new ValueStateDescriptor<>(
                "stateCD",
                Types.POJO(OutputEvent2.class)
        );
        stateCD = getRuntimeContext().getState(descriptorC);
    }
}
