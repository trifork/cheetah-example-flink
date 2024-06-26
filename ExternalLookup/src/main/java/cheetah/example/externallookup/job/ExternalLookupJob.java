package cheetah.example.externallookup.job;

import cheetah.example.externallookup.config.ConfigProvider;
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
        final KafkaSource<InputEvent> kafkaSource = CheetahKafkaSourceConfig.builder(this, "main-source")
                .toKafkaSourceBuilder(InputEvent.class)
                .setStartingOffsets(OffsetsInitializer.earliest())
                .build();

        final DataStream<InputEvent> inputStream = CheetahKafkaSource.toDataStream(this, kafkaSource, "Event Input Source", "Event Input Source");

        final ConfigProvider configProvider = new ConfigProvider(this);

        // Get configuration from ENV
        final String idServiceHost = configProvider.getIdServiceUrl();
        final String tokenUrl = configProvider.getIdServiceToken();
        final String clientId = configProvider.getIdServiceClientId();
        final String clientSecret = configProvider.getIdServiceClientSecret();
        final String scope = configProvider.getIdServiceScope();

        // Transform stream
        final SingleOutputStreamOperator<OutputEvent> outputStream =
                AsyncDataStream.unorderedWait(inputStream, new ExternalLookupMapper(idServiceHost, tokenUrl, clientId, clientSecret, scope), 1000, TimeUnit.MILLISECONDS, 100)
                .name("ExternalLookupMapper")
                .uid("ExternalLookupMapper");

        // Output sink
        final KafkaSink<OutputEvent> kafkaSink = CheetahKafkaSinkConfig.builder(this, "main-sink")
                .toKafkaSinkBuilder(OutputEvent.class).build();

        // Connect transformed stream to sink
        outputStream.sinkTo(kafkaSink).name(ExternalLookupJob.class.getSimpleName())
                .name("ExternalLookupKafkaSink")
                .uid("ExternalLookupKafkaSink");
    }
}
