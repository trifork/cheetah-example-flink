package sideOutputExample.job;

import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;
import sideOutputExample.model.InputEvent;
import sideOutputExample.model.OutputEvent;
import sideOutputExample.model.OutputEvent2;

public class MultipleSideOutputExampleProcess extends KeyedProcessFunction<String, InputEvent, InputEvent> {

    private transient ValueState<OutputEvent> stateA;
    private transient ValueState<OutputEvent> stateB;
    private transient ValueState<OutputEvent2> stateCD;

    @Override
    public void processElement(InputEvent inputEvent, KeyedProcessFunction<String, InputEvent, InputEvent>.Context context, Collector<InputEvent> collector) throws Exception {
        // Update the states if the event id hasn't been seen before.
        // Or if the state is different from the last known state.
        if(stateA.value() == null ||
                stateA.value().getValue() != inputEvent.getValueA()){
            OutputEvent outputEvent = new OutputEvent(
                    inputEvent.getDeviceId(),
                    inputEvent.getValueA(),
                    inputEvent.getTimestamp()
            );
            context.output(MultipleSideOutputExampleJob.outputA, outputEvent);
            stateA.update(outputEvent);
        }

        if(stateB.value() == null ||
                stateB.value().getValue() != inputEvent.getValueB()){
            OutputEvent outputEvent = new OutputEvent(
                    inputEvent.getDeviceId(),
                    inputEvent.getValueB(),
                    inputEvent.getTimestamp()
            );
            context.output(MultipleSideOutputExampleJob.outputB, outputEvent);
            stateB.update(outputEvent);
        }

        if(stateCD.value() == null ||
                stateCD.value().valueC != inputEvent.getValueC() ||
                stateCD.value().valueD != inputEvent.getValueD()){
            OutputEvent2 outputEvent2 = new OutputEvent2(
                    inputEvent.getDeviceId(),
                    inputEvent.getValueC(),
                    inputEvent.getValueD(),
                    inputEvent.getTimestamp()
            );
            context.output(MultipleSideOutputExampleJob.outputCD, outputEvent2);
            stateCD.update(outputEvent2);
        }

    }

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
