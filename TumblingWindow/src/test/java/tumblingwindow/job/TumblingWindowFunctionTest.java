package tumblingwindow.job;

import org.apache.flink.api.common.ExecutionConfig;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.state.ReducingStateDescriptor;
import org.apache.flink.api.common.typeinfo.BasicTypeInfo;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.streaming.api.functions.windowing.PassThroughWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.triggers.EventTimeTrigger;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.streaming.runtime.operators.windowing.WindowOperator;
import org.apache.flink.streaming.runtime.operators.windowing.functions.InternalSingleValueWindowFunction;
import org.apache.flink.streaming.util.KeyedOneInputStreamOperatorTestHarness;
import org.junit.jupiter.api.BeforeEach;
import tumblingwindow.model.TumblingWindowInputEvent;
import tumblingwindow.model.TumblingWindowOutputEvent;

import java.util.concurrent.TimeUnit;


class TumblingWindowFunctionTest {

    private KeyedOneInputStreamOperatorTestHarness<String, TumblingWindowInputEvent, TumblingWindowOutputEvent> testHarness;
    private TumblingWindowFunction tumblingWindowFunction;

    @BeforeEach
    public void setup() throws Exception {
        tumblingWindowFunction = new TumblingWindowFunction();
        var stateDesc = new ReducingStateDescriptor<>(
                "window-contents",
                (acc, elem) -> acc.addValue(acc.getValue() + elem.getValue()),
                TypeInformation.of(TumblingWindowOutputEvent.class)
        );
        var a = new WindowOperator<String, TumblingWindowInputEvent, TumblingWindowOutputEvent, TumblingWindowOutputEvent, TimeWindow>(
                TumblingProcessingTimeWindows.of(Time.of(3, TimeUnit.SECONDS)),
                new TimeWindow.Serializer(),
                message -> message.getDeviceId(),
                BasicTypeInfo.STRING_TYPE_INFO.createSerializer(new ExecutionConfig()),
                stateDesc,
                new InternalSingleValueWindowFunction<>(new PassThroughWindowFunction<>()),
                EventTimeTrigger.create(),
                0L,
                null
        );

        MapStateDescriptor<TumblingWindowInputEvent, TumblingWindowOutputEvent> stateDesc =
                new MapStateDescriptor<>(
                        "window-contents",
                        TypeInformation.of(TumblingWindowInputEvent.class),
                        TypeInformation.of(TumblingWindowOutputEvent.class));

        TumblingProcessingTimeWindows a = TumblingProcessingTimeWindows.of(
                Time.of(windowSize, TimeUnit.SECONDS));

//        WindowOperator<String, TumblingWindowInputEvent, TumblingWindowOutputEvent, TumblingWindowOutputEvent, TimeWindow> operator = new WindowOperator<>(
//                TumblingProcessingTimeWindows.of(
//                        Time.of(windowSize, TimeUnit.SECONDS)),
//                new TimeWindow.Serializer(),
//                (KeySelector<TumblingWindowInputEvent, String>) TumblingWindowInputEvent::getDeviceId,
//                BasicTypeInfo.STRING_TYPE_INFO.createSerializer(
//                        new ExecutionConfig()),
//                stateDesc,
//                new InternalSingleValueWindowFunction<>(
//                        new ReduceApplyWindowFunction<
//                                String,
//                                TimeWindow,
//                                TumblingWindowInputEvent,
//                                TumblingWindowOutputEvent>()),
//                ProcessingTimeTrigger.create(),
//                0,
//                null

//                );
//        harness = new KeyedOneInputStreamOperatorTestHarness<>()

    }

}
