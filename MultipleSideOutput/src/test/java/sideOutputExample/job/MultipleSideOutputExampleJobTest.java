package sideOutputExample.job;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import sideOutputExample.model.InputEvent;
import org.junit.jupiter.api.Test;
import sideOutputExample.model.OutputEvent;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MultipleSideOutputExampleJobTest {
    private final MultipleSideOutputExampleJob mapper = new MultipleSideOutputExampleJob();


    private List<InputEvent> runPipelineAndReturnEvents(List<InputEvent> input) throws Exception {
        final String deviceId = UUID.randomUUID().toString();
        final double valueA = 1.0;
        final double valueB = 1.0;
        final double valueC = 1.0;
        final double valueD = 1.0;
        final long timestamp = 0;
        StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironment(1);

        DataStream<InputEvent> inputStream = env.fromCollection(input);

        return List.of();
    };

    @Test
    public void testEnrichment() {
        final String deviceId = UUID.randomUUID().toString();
        final double valueA = 1.0;
        final double valueB = 1.0;
        final double valueC = 1.0;
        final double valueD = 1.0;
        final long timestamp = 0;
        final var input = new InputEvent(deviceId, valueA, valueB, valueC, valueD, timestamp);


    }
}