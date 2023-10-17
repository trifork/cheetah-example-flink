package cheetah.example.tumblingwindow.tumblingwindow.function;

import cheetah.example.tumblingwindow.tumblingwindow.model.EventWindow;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

/** TumblingWindowMapper converts from TumblingWindowInputEvent to TumblingWindowOutputEvent. */
public class TumblingWindowFunction extends ProcessWindowFunction<Double[], EventWindow, String, TimeWindow> {

    @Override
    public void process(String key, ProcessWindowFunction<Double[], EventWindow, String, TimeWindow>.Context context, Iterable<Double[]> elements, Collector<EventWindow> out) throws Exception {
        out.collect(new EventWindow(key, context.window().getStart(), context.window().getEnd(), elements.iterator().next()));
    }
}