package cheetah.example.serializationerrorcatch.job;

import cheetah.example.serializationerrorcatch.function.FilterAndCountFailedSerializations;
import cheetah.example.serializationerrorcatch.function.SerializationErrorCatchMapper;
import cheetah.example.serializationerrorcatch.model.InputEvent;
import cheetah.example.serializationerrorcatch.model.OutputEvent;
import cheetah.example.serializationerrorcatch.serde.DeserializationSchema;
import com.trifork.cheetah.processing.job.Job;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.source.KafkaSource;
import com.trifork.cheetah.processing.connector.kafka.CheetahKafkaSink;
import com.trifork.cheetah.processing.connector.kafka.CheetahKafkaSource;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;

import java.io.Serializable;

/** The SerializationErrorCatchJob exemplifies the creation of a custom deserialization method,
 * its integration with the KafkaSource, and the handling of deserialization errors in a tailored manner.
 * In this demonstration, an error log message is displayed also accompanied by the incrementation of a metric counter. */
public class SerializationErrorCatchJob extends Job implements Serializable {

    @SuppressWarnings("PMD.SignatureDeclareThrowsException") // Fix once lib-processing is fixed
    public static void main(final String[] args) throws Exception {
        new SerializationErrorCatchJob().start(args);
    }

    @Override
    protected void setup() {
        // Input source
        final KafkaSource<InputEvent> kafkaSource = CheetahKafkaSource.builder(InputEvent.class, this)
                .setValueOnlyDeserializer(new DeserializationSchema<>(InputEvent.class))
                .build();

        final DataStream<InputEvent> inputStream = CheetahKafkaSource.toDataStream(this, kafkaSource, "SerializationErrorCatch-source");

        // Transform stream
        final SingleOutputStreamOperator<OutputEvent> outputStream = inputStream
                .filter(new FilterAndCountFailedSerializations())
                .name("SerializationErrorCatchFilter")
                .uid("SerializationErrorCatchFilter")
                .map(new SerializationErrorCatchMapper("ExtraFieldValue"))
                .name("SerializationErrorCatchMapper")
                .uid("SerializationErrorCatchMapper");

        // Output sink
        KafkaSink<OutputEvent> kafkaSink = CheetahKafkaSink.builder(OutputEvent.class, this)
                .build();

        // Connect transformed stream to sink
        outputStream.sinkTo(kafkaSink)
                .name("SerializationErrorCatchKafkaSink")
                .uid("SerializationErrorCatchKafkaSink");
    }
}
