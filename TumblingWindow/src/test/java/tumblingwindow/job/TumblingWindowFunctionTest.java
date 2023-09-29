package tumblingwindow.job;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tumblingwindow.model.TumblingWindowInputEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
        aggregate.add(new TumblingWindowInputEvent("test_device", 0, 1.0), inputEvents);
        aggregate.add(new TumblingWindowInputEvent("test_device", 0, 2.0), inputEvents);
        aggregate.add(new TumblingWindowInputEvent("test_device", 0, 3.0), inputEvents);
        Double[] doublesArray = new Double[]{1.0, 2.0, 3.0};

        assertArrayEquals(doublesArray, aggregate.getResult(inputEvents));
    }

}
