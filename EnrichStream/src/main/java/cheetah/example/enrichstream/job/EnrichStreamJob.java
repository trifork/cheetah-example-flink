package cheetah.example.enrichstream.job;

import cheetah.example.enrichstream.function.Enricher;
import cheetah.example.enrichstream.model.EnrichingEvent;
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
 * The MergeTwoStreamsJob is meant to show how to merge two streams into one.
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
        final KafkaSource<EnrichingEvent> enrichingSource = CheetahKafkaSourceConfig.builder(this, "enriching")
                .toKafkaSourceBuilder(EnrichingEvent.class)
                .build();

        final DataStream<EnrichingEvent> enrichingInputStream  = CheetahKafkaSource.toDataStream(this, enrichingSource, "enrichingSource");

        // Setup reading from Stream B
        final KafkaSource<InputEvent> mainSource = CheetahKafkaSourceConfig.builder(this, "main")
                .toKafkaSourceBuilder(InputEvent.class)
                .build();

        final DataStream<InputEvent> mainInputStream = CheetahKafkaSource.toDataStream(this, mainSource, "mainSource");

        // Merge the two streams by connecting them, giving the KeyBy, which tells which fields to merge by.
        // Final processing is done by the Enricher
        final SingleOutputStreamOperator<OutputEvent> outputStream = enrichingInputStream
                        .connect(mainInputStream)
                        .keyBy(EnrichingEvent::getDeviceId, InputEvent::getDeviceId)
                        .process(new Enricher());

        // Output the result to a new Stream
       final KafkaSink<OutputEvent> kafkaSink = CheetahKafkaSink.builder(OutputEvent.class, this)
               .build();

        // Connect transformed stream to sink
        outputStream.sinkTo(kafkaSink).name(getClass().getSimpleName());
    }
}
