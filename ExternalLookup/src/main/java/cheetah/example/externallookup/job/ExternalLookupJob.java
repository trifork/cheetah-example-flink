package cheetah.example.externallookup.job;

import cheetah.example.externallookup.function.ExternalLookupMapper;
import cheetah.example.externallookup.model.InputEvent;
import cheetah.example.externallookup.model.OutputEvent;
import com.trifork.cheetah.processing.connector.kafka.CheetahKafkaSource;
import com.trifork.cheetah.processing.connector.kafka.config.CheetahKafkaSinkConfig;
import com.trifork.cheetah.processing.connector.kafka.config.CheetahKafkaSourceConfig;
import com.trifork.cheetah.processing.job.Job;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.AsyncDataStream;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

/**
 * ExternalLookupJob sets up the data processing job.
 */
public class ExternalLookupJob extends Job implements Serializable {

    @SuppressWarnings("PMD.SignatureDeclareThrowsException") // Fix once lib-processing is fixed
    public static void main(final String[] args) throws Exception {
        new ExternalLookupJob().start(args);
    }

    @Override
    protected void setup() {

        // Setup input stream
        final KafkaSource<InputEvent> kafkaSource = CheetahKafkaSourceConfig.builder(this)
                .toKafkaSourceBuilder(InputEvent.class)
                .setStartingOffsets(OffsetsInitializer.earliest())
                .build();

        final DataStream<InputEvent> inputStream = CheetahKafkaSource.toDataStream(this, kafkaSource, "Event Input Source");

        // Get configuration from ENV
        final String tokenEndpoint = System.getenv("SERVICE_TOKEN_ENDPOINT");
        final String clientId = System.getenv("SERVICE_CLIENT_ID");
        final String clientSecret = System.getenv("SERVICE_CLIENT_SECRET");
        final String scope = System.getenv("SERVICE_SCOPE");

        // Transform stream
        final SingleOutputStreamOperator<OutputEvent> outputStream =
                AsyncDataStream.unorderedWait(inputStream, new ExternalLookupMapper(tokenEndpoint, clientId, clientSecret, scope), 1000, TimeUnit.MILLISECONDS, 100);

        // Output sink
        final KafkaSink<OutputEvent> kafkaSink =
                CheetahKafkaSinkConfig.builder(this).toKafkaSinkBuilder(OutputEvent.class).build();

        // Connect transformed stream to sink
        outputStream.sinkTo(kafkaSink).name(ExternalLookupJob.class.getSimpleName());
    }
}
