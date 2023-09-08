package sideOutputExample.job;

import sideOutputExample.model.MultipleSideOutputExampleInputEvent;
import sideOutputExample.model.MultipleSideOutputExampleOutputEvent;
import com.trifork.cheetah.processing.connector.kafka.KafkaDataStreamBuilder;
import com.trifork.cheetah.processing.connector.kafka.KafkaSinkBuilder;
import com.trifork.cheetah.processing.connector.serialization.SimpleKeySerializationSchema;
import com.trifork.cheetah.processing.job.Job;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;

import java.io.Serializable;

/** MultipleSideOutputExampleJob sets up the data processing job. */
public class MultipleSideOutputExampleJob extends Job implements Serializable {

    @SuppressWarnings("PMD.SignatureDeclareThrowsException") // Fix once lib-processing is fixed
    public static void main(final String[] args) throws Exception {
        new MultipleSideOutputExampleJob().start(args);
    }

    @Override
    protected void setup() {
        // Input source
        final DataStream<MultipleSideOutputExampleInputEvent> inputStream =
                KafkaDataStreamBuilder.forSource(this, MultipleSideOutputExampleInputEvent.class)
                        .build();

        // Transform stream
        final SingleOutputStreamOperator<MultipleSideOutputExampleOutputEvent> outputStream =
                inputStream.map(new MultipleSideOutputExampleMapper("ExtraFieldValue"));

        // Output sink
        final KafkaSink<MultipleSideOutputExampleOutputEvent> kafkaSink =
                KafkaSinkBuilder.defaultKafkaConfig(this, MultipleSideOutputExampleOutputEvent.class)
                        .keySerializationSchema(
                                new SimpleKeySerializationSchema<>() {

                                    @Override
                                    public Object getKey(final MultipleSideOutputExampleOutputEvent outputEvent) {
                                        return outputEvent.getDeviceId();
                                    }
                                })
                        .build();

        // Connect transformed stream to sink
        outputStream.sinkTo(kafkaSink).name(MultipleSideOutputExampleJob.class.getSimpleName());
    }
}
