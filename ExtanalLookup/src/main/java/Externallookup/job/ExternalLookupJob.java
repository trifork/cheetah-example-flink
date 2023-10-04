package Externallookup.job;

import Externallookup.model.ExternalLookupInputEvent;
import Externallookup.model.ExternalLookupOutputEvent;
import com.trifork.cheetah.processing.connector.kafka.KafkaDataStreamBuilder;
import com.trifork.cheetah.processing.connector.kafka.KafkaSinkBuilder;
import com.trifork.cheetah.processing.connector.serialization.SimpleKeySerializationSchema;
import com.trifork.cheetah.processing.job.Job;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.AsyncDataStream;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

/** ExternalLookupJob sets up the data processing job. */
public class ExternalLookupJob extends Job implements Serializable {

    @SuppressWarnings("PMD.SignatureDeclareThrowsException") // Fix once lib-processing is fixed
    public static void main(final String[] args) throws Exception {
        new ExternalLookupJob().start(args);
    }

    @Override
    protected void setup() {
        // Input source
        final DataStream<ExternalLookupInputEvent> inputStream =
                KafkaDataStreamBuilder.forSource(this, ExternalLookupInputEvent.class)
                        .offsetsInitializer(OffsetsInitializer.earliest())
                        .build();

        // Transform stream
        final SingleOutputStreamOperator<ExternalLookupOutputEvent> outputStream =
                AsyncDataStream.unorderedWait(inputStream, new ExternalLookupMapper(), 1000, TimeUnit.MILLISECONDS, 100);

        // Output sink
        final KafkaSink<ExternalLookupOutputEvent> kafkaSink =
                KafkaSinkBuilder.defaultKafkaConfig(this, ExternalLookupOutputEvent.class)
                        .keySerializationSchema(
                                new SimpleKeySerializationSchema<>() {

                                    @Override
                                    public Object getKey(final ExternalLookupOutputEvent outputEvent) {
                                        return outputEvent.getDeviceId();
                                    }
                                })
                        .build();

        // Connect transformed stream to sink
        outputStream.sinkTo(kafkaSink).name(ExternalLookupJob.class.getSimpleName());
    }
}
