package tumblingwindow.job;

import org.apache.flink.api.common.functions.AggregateFunction;
import tumblingwindow.model.TumblingWindowInputEvent;

import java.util.ArrayList;
import java.util.List;

public class TumblingWindowAggregate implements AggregateFunction<TumblingWindowInputEvent, List<Double>, List<Double>> {
    @Override
    public List<Double> createAccumulator() {
        return new ArrayList<>();
    }

    @Override
    public List<Double> add(TumblingWindowInputEvent in, List<Double> acc) {
        acc.add(in.getValue());
        return acc;
    }

    @Override
    public List<Double> getResult(List<Double> acc) {
        return acc;
    }

    @Override
    public List<Double> merge(List<Double> acc1, List<Double> acc2) {
        acc1.addAll(acc2);
        return acc1;
    }
}
