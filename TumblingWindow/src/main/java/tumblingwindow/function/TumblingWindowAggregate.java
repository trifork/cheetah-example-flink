package tumblingwindow.function;

import org.apache.flink.api.common.functions.AggregateFunction;
import tumblingwindow.model.InputEvent;

import java.util.ArrayList;
import java.util.List;

public class TumblingWindowAggregate implements AggregateFunction<InputEvent, List<Double>, Double[]> {
    @Override
    public List<Double> createAccumulator() {
        return new ArrayList<>();
    }

    @Override
    public List<Double> add(InputEvent in, List<Double> acc) {
        acc.add(in.getValue());
        return acc;
    }

    @Override
    public Double[] getResult(List<Double> acc) {
        return acc.toArray(Double[]::new);
    }

    @Override
    public List<Double> merge(List<Double> acc1, List<Double> acc2) {
        acc1.addAll(acc2);
        return acc1;
    }
}
