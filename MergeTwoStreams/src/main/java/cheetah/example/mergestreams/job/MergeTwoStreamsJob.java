package cheetah.example.mergestreams.job;

import cheetah.example.mergestreams.enricher.MergeTwoStreamsEnricher;
import cheetah.example.mergestreams.model.MergeTwoStreamsInputEventA;
import cheetah.example.mergestreams.model.MergeTwoStreamsInputEventB;
import cheetah.example.mergestreams.model.MergeTwoStreamsOutputEvent;
import com.trifork.cheetah.processing.connector.kafka.CheetahKafkaSink;
import com.trifork.cheetah.processing.connector.kafka.CheetahKafkaSource;
import com.trifork.cheetah.processing.connector.kafka.config.CheetahKafkaSinkConfig;
import com.trifork.cheetah.processing.connector.kafka.config.CheetahKafkaSourceConfig;
import com.trifork.cheetah.processing.connector.serde.CheetahSerdeSchemas;
import com.trifork.cheetah.processing.job.Job;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.formats.json.JsonSerializationSchema;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;

import java.io.Serializable;

/**
 * The MergeTwoStreamsJob is meant to show how to merge two streams into one.
 * The two streams in this example are meant to both contain a deviceId, which will be used for the pairing
 * For each element in Stream B, we will output an element, if we have already seen the device in Stream A
 */
public class MergeTwoStreamsJob extends Job implements Serializable {

    @SuppressWarnings("PMD.SignatureDeclareThrowsException") // Fix once lib-processing is fixed
    public static void main(final String[] args) throws Exception {
        new MergeTwoStreamsJob().start(args);
    }

    @Override
    protected void setup() {
        // Setup reading from Stream A
        CheetahKafkaSourceConfig configA = CheetahKafkaSourceConfig.defaultConfig(this, "a");

        final KafkaSource<MergeTwoStreamsInputEventA> kafkaSourceA = CheetahKafkaSource.builder(MergeTwoStreamsInputEventA.class, configA).build();

        final DataStream<MergeTwoStreamsInputEventA> dataStreamA = CheetahKafkaSource.toDataStream(this, kafkaSourceA, "my-source-name-a");

        // Setup reading from Stream B
        CheetahKafkaSourceConfig configB = CheetahKafkaSourceConfig.defaultConfig(this, "b");

        final KafkaSource<MergeTwoStreamsInputEventB> kafkaSourceB = CheetahKafkaSource.builder(MergeTwoStreamsInputEventB.class,configB).build();

        final DataStream<MergeTwoStreamsInputEventB> dataStreamB = CheetahKafkaSource.toDataStream(this, kafkaSourceB, "my-source-name-b");

        // Merge the two streams by connecting them, giving the KeyBy, which tells which fields to merge by.
        // Final processing is done by the Enricher
        final SingleOutputStreamOperator<MergeTwoStreamsOutputEvent> outputStream =
                dataStreamA
                        .connect(dataStreamB)
                        .keyBy((KeySelector<MergeTwoStreamsInputEventA, String>) MergeTwoStreamsInputEventA::getDeviceId,
                                (KeySelector<MergeTwoStreamsInputEventB, String>) MergeTwoStreamsInputEventB::getDeviceId)
                        .process(new MergeTwoStreamsEnricher());

        // Output the result to a new Stream
       final KafkaSink<MergeTwoStreamsOutputEvent> kafkaSink = CheetahKafkaSink.builder(MergeTwoStreamsOutputEvent.class, this)
               .setRecordSerializer(CheetahSerdeSchemas.kafkaRecordSerializationSchema(
                       CheetahKafkaSinkConfig.defaultConfig(this),
                       message -> message.getDeviceId().getBytes(),
                       new JsonSerializationSchema<>()
               ))
               .build();

        // Connect transformed stream to sink
        outputStream.sinkTo(kafkaSink).name(MergeTwoStreamsJob.class.getSimpleName());
    }
}
