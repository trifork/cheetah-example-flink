package sideOutputExample.job;

import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;
import sideOutputExample.model.InputEvent;

public class MultipleSideOutputExampleProcess extends KeyedProcessFunction<String, InputEvent, InputEvent> {

    private transient ValueState<Double> stateA;
    private transient ValueState<Double> stateB;
    private transient ValueState<Double> stateC;
    private transient ValueState<Double> stateD;

    @Override
    public void processElement(InputEvent inputEvent, KeyedProcessFunction<String, InputEvent, InputEvent>.Context context, Collector<InputEvent> collector) throws Exception {
        // Update the states if the event id hasn't been seen before.
        // Or if the state if different from the last known state.
        if(stateA.value() == null || stateA.value() != inputEvent.getValueA()){
            context.output(MultipleSideOutputExampleJob.outputA, inputEvent);
            stateA.update(inputEvent.getValueA());
        }

        if(stateB.value() == null || stateB.value() != inputEvent.getValueB()){
            context.output(MultipleSideOutputExampleJob.outputB, inputEvent);
            stateB.update(inputEvent.getValueB());
        }

        if(stateC.value() == null || stateC.value() != inputEvent.getValueC()){
            context.output(MultipleSideOutputExampleJob.outputC, inputEvent);
            stateC.update(inputEvent.getValueC());
        }

        if(stateD.value() == null || stateC.value() != inputEvent.getValueC()){
            context.output(MultipleSideOutputExampleJob.outputC, inputEvent);
            stateC.update(inputEvent.getValueC());
        }
    }

    @Override
    public void open(Configuration parameters) throws Exception {
        ValueStateDescriptor<Double> descriptorA = new ValueStateDescriptor<>(
                "stateA",
                Types.DOUBLE
        );
        stateA = getRuntimeContext().getState(descriptorA);

        ValueStateDescriptor<Double> descriptorB = new ValueStateDescriptor<>(
                "stateA",
                Types.DOUBLE
        );
        stateB = getRuntimeContext().getState(descriptorB);

        ValueStateDescriptor<Double> descriptorC = new ValueStateDescriptor<>(
                "stateA",
                Types.DOUBLE
        );
        stateC = getRuntimeContext().getState(descriptorC);

        ValueStateDescriptor<Double> descriptorD = new ValueStateDescriptor<>(
                "stateA",
                Types.DOUBLE
        );
        stateD = getRuntimeContext().getState(descriptorD);
    }
}
