package cheetah.example.tumblingwindow.job;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import cheetah.example.tumblingwindow.function.TumblingWindowAggregate;
import cheetah.example.tumblingwindow.model.InputEvent;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class TumblingWindowFunctionTest {
    private TumblingWindowAggregate aggregate;

    @BeforeEach
    public void setup() {
        aggregate = new TumblingWindowAggregate();
    }

    @Test
    public void testAggregation() {
        ArrayList<Double> inputEvents = new ArrayList<>();
        aggregate.add(new InputEvent("test_device", 0, 1.0), inputEvents);
        aggregate.add(new InputEvent("test_device", 0, 2.0), inputEvents);
        aggregate.add(new InputEvent("test_device", 0, 3.0), inputEvents);
        Double[] doublesArray = new Double[]{1.0, 2.0, 3.0};

        assertArrayEquals(doublesArray, aggregate.getResult(inputEvents));
    }

}
