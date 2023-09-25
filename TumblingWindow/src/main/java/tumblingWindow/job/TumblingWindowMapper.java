package tumblingWindow.job;

import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import tumblingWindow.model.TumblingWindowInputEvent;
import tumblingWindow.model.TumblingWindowOutputEvent;

import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.apache.flink.shaded.curator5.com.google.common.collect.Iterables.getFirst;

/** TumblingWindowMapper converts from TumblingWindowInputEvent to TumblingWindowOutputEvent. */
public class TumblingWindowMapper extends ProcessWindowFunction<TumblingWindowInputEvent, TumblingWindowOutputEvent, String, TimeWindow> {

    @Override
    public void process(String s, ProcessWindowFunction<TumblingWindowInputEvent, TumblingWindowOutputEvent, String, TimeWindow>.Context context, Iterable<TumblingWindowInputEvent> iterable, Collector<TumblingWindowOutputEvent> collector) throws Exception {
        TumblingWindowOutputEvent outputEvent = new TumblingWindowOutputEvent();

        TumblingWindowInputEvent first = getFirst(iterable, null);
        if(first == null) return;

        outputEvent.setStartTime(context.window().getStart());
        outputEvent.setEndTime(context.window().getEnd());
        outputEvent.setDeviceId(first.getDeviceId());

        Double[] inputValues = StreamSupport
                .stream(iterable.spliterator(), false)
                .map(TumblingWindowInputEvent::getValue)
                .toArray(Double[]::new);

        outputEvent.setValue(inputValues);

        collector.collect(outputEvent);
    }
}