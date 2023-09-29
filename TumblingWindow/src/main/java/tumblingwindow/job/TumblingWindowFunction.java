package tumblingwindow.job;

import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import tumblingwindow.model.TumblingWindowOutputEvent;

import java.util.List;

/** TumblingWindowMapper converts from TumblingWindowInputEvent to TumblingWindowOutputEvent. */
public class TumblingWindowFunction extends ProcessWindowFunction<Double[], TumblingWindowOutputEvent, String, TimeWindow> {

    @Override
    public void process(String key, ProcessWindowFunction<Double[], TumblingWindowOutputEvent, String, TimeWindow>.Context context, Iterable<Double[]> elements, Collector<TumblingWindowOutputEvent> out) throws Exception {
        out.collect(new TumblingWindowOutputEvent(key, context.window().getStart(), context.window().getEnd(), elements.iterator().next()));
    }
}