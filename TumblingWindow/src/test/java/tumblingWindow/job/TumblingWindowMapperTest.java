package tumblingWindow.job;

import org.apache.flink.api.common.ExecutionConfig;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.state.ReducingStateDescriptor;
import org.apache.flink.api.common.typeinfo.BasicTypeInfo;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.streaming.api.functions.windowing.PassThroughWindowFunction;
import org.apache.flink.streaming.api.functions.windowing.ReduceApplyWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.triggers.ProcessingTimeTrigger;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.streaming.runtime.operators.windowing.WindowOperator;
import org.apache.flink.streaming.runtime.operators.windowing.functions.InternalSingleValueWindowFunction;
import org.apache.flink.streaming.util.KeyedOneInputStreamOperatorTestHarness;
import org.apache.flink.streaming.util.OneInputStreamOperatorTestHarness;
import org.junit.jupiter.api.BeforeEach;
import tumblingWindow.model.TumblingWindowInputEvent;
import tumblingWindow.model.TumblingWindowOutputEvent;

import java.util.concurrent.TimeUnit;


class TumblingWindowMapperTest {

    private KeyedOneInputStreamOperatorTestHarness<String, TumblingWindowInputEvent, TumblingWindowOutputEvent> harness;
    private final int windowSize = 3;

    @BeforeEach
    public void setup() throws Exception {

        TumblingWindowMapper sut = new TumblingWindowMapper();

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
