package cheetah.example.tumblingwindow.job;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import cheetah.example.tumblingwindow.tumblingwindow.function.TumblingWindowAggregate;
import cheetah.example.tumblingwindow.tumblingwindow.model.InputEvent;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;


class TumblingWindowFunctionTest {

    private TumblingWindowAggregate aggregate;

    @BeforeEach
    public void setup() throws Exception {
        aggregate = new TumblingWindowAggregate();
    }

    @Test
    public void testAggregation() throws Exception {
        ArrayList<Double> inputEvents = new ArrayList<>();
        aggregate.add(new InputEvent("test_device", 0, 1.0), inputEvents);
        aggregate.add(new InputEvent("test_device", 0, 2.0), inputEvents);
        aggregate.add(new InputEvent("test_device", 0, 3.0), inputEvents);
        Double[] doublesArray = new Double[]{1.0, 2.0, 3.0};

        assertArrayEquals(doublesArray, aggregate.getResult(inputEvents));
    }

}
