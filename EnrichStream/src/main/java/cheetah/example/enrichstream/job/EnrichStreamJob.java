package cheetah.example.enrichstream.job;

import cheetah.example.enrichstream.function.EventEnricher;
import cheetah.example.enrichstream.model.EnrichEvent;
import cheetah.example.enrichstream.model.InputEvent;
import cheetah.example.enrichstream.model.OutputEvent;
import com.trifork.cheetah.processing.connector.kafka.CheetahKafkaSink;
import com.trifork.cheetah.processing.connector.kafka.CheetahKafkaSource;
import com.trifork.cheetah.processing.connector.kafka.config.CheetahKafkaSourceConfig;
import com.trifork.cheetah.processing.job.Job;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;

import java.io.Serializable;

/**
 * The EnrichStreamJob is meant to show how to enrich stream into one.
 * The two streams in this example are meant to both contain a deviceId, which will be used for the pairing
 * For each element in Stream B, we will output an element, if we have already seen the device in Stream A
 */
public class EnrichStreamJob extends Job implements Serializable {

    @SuppressWarnings("PMD.SignatureDeclareThrowsException") // Fix once lib-processing is fixed
    public static void main(final String[] args) throws Exception {
        new EnrichStreamJob().start(args);
    }

    @Override
    protected void setup() {
        // Setup reading from Stream A
        final KafkaSource<EnrichEvent> enrichingKafkaSource = CheetahKafkaSourceConfig.builder(this, "enriching")
                .toKafkaSourceBuilder(EnrichEvent.class)
                .build();

        final DataStream<EnrichEvent> enrichingStream  = CheetahKafkaSource.toDataStream(this, enrichingKafkaSource, "enriching-kafka-stream");

        // Setup reading from Stream B
        final KafkaSource<InputEvent> inputKafkaSource = CheetahKafkaSourceConfig.builder(this, "main")
                .toKafkaSourceBuilder(InputEvent.class)
                .build();

        final DataStream<InputEvent> inputStream = CheetahKafkaSource.toDataStream(this, inputKafkaSource, "input-kafka-stream");

        // Merge the two streams by connecting them, giving the KeyBy, which tells which fields to merge by.
        // Final processing is done by the Enricher
        final SingleOutputStreamOperator<OutputEvent> outputStream = enrichingStream
                .connect(inputStream)
                .keyBy(EnrichEvent::getDeviceId, InputEvent::getDeviceId)
                .process(new EventEnricher());

        // Output the result to a new Stream
       final KafkaSink<OutputEvent> kafkaSink = CheetahKafkaSink.builder(OutputEvent.class, this)
               .build();

        // Connect transformed stream to sink
        outputStream.sinkTo(kafkaSink).name(EnrichStreamJob.class.getSimpleName());
    }
}
