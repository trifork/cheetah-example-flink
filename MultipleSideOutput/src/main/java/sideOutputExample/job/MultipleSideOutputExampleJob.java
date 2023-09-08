package sideOutputExample.job;

import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.util.OutputTag;
import sideOutputExample.model.InputEvent;
import com.trifork.cheetah.processing.connector.kafka.KafkaDataStreamBuilder;
import com.trifork.cheetah.processing.connector.kafka.KafkaSinkBuilder;
import com.trifork.cheetah.processing.connector.serialization.SimpleKeySerializationSchema;
import com.trifork.cheetah.processing.job.Job;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.streaming.api.datastream.DataStream;

import java.io.Serializable;

/** MultipleSideOutputExampleJob sets up the data processing job. */
public class MultipleSideOutputExampleJob extends Job implements Serializable {

    @SuppressWarnings("PMD.SignatureDeclareThrowsException") // Fix once lib-processing is fixed
    public static void main(final String[] args) throws Exception {
        new MultipleSideOutputExampleJob().start(args);
    }

    public static OutputTag<InputEvent> outputA;
    public static OutputTag<InputEvent> outputB;
    public static OutputTag<InputEvent> outputC;
    public static OutputTag<InputEvent> outputD;

    @Override
    protected void setup() {
        // Input source
        DataStream<InputEvent> inputStream =
                KafkaDataStreamBuilder.forSource(this, InputEvent.class)
                        .build();

        // Process element
        SingleOutputStreamOperator<InputEvent> dataStream = inputStream.keyBy(InputEvent::getDeviceId)
                .process(new MultipleSideOutputExampleProcess());


        // Output sink
        final KafkaSink<InputEvent> kafkaSink =
                KafkaSinkBuilder.defaultKafkaConfig(this, InputEvent.class)
                        .keySerializationSchema(
                                new SimpleKeySerializationSchema<>() {

                                    @Override
                                    public Object getKey(final InputEvent outputEvent) {
                                        return outputEvent.getDeviceId();
                                    }
                                })
                        .build();

        dataStream.getSideOutput(outputA).sinkTo(kafkaSink);
    }
}
