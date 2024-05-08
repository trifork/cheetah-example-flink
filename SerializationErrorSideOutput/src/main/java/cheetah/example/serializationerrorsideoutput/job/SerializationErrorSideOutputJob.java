package cheetah.example.serializationerrorsideoutput.job;

import cheetah.example.serializationerrorsideoutput.function.SerializationErrorSideOutputMapper;
import cheetah.example.serializationerrorsideoutput.model.InputEvent;
import cheetah.example.serializationerrorsideoutput.model.OutputEvent;
import com.trifork.cheetah.processing.job.Job;
import com.trifork.cheetah.processing.util.deserialization.*;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.source.KafkaSource;
import com.trifork.cheetah.processing.connector.kafka.CheetahKafkaSink;
import com.trifork.cheetah.processing.connector.kafka.CheetahKafkaSource;
import org.apache.flink.formats.json.JsonDeserializationSchema;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;

import java.io.Serializable;

/**
 * The SerializationErrorSideOutputJob exemplifies the using the MaybeUnParsableKafkaValueOnlyDeserializationSchema and
 * UnParsableHelper.filterToSideTopic to filter un-parsable elements to a separate kafka topic
 * A warning log is produces for every un-parsable message accompanied by the incrementation of a metric counter.
 */
public class SerializationErrorSideOutputJob extends Job implements Serializable {

    @SuppressWarnings("PMD.SignatureDeclareThrowsException") // Fix once lib-processing is fixed
    public static void main(final String[] args) throws Exception {
        new SerializationErrorSideOutputJob().start(args);
    }

    @Override
    protected void setup() {
        // Input source
        final KafkaSource<MaybeParsable> kafkaSource = CheetahKafkaSource.builder(MaybeParsable.class, this, "main-source")
                .setDeserializer(new MaybeUnParsableKafkaValueOnlyDeserializationSchema<>(new JsonDeserializationSchema<>(InputEvent.class)))
                .build();

        final DataStream<MaybeParsable> unFilteredInputStream = CheetahKafkaSource.toDataStream(this, kafkaSource, "SerializationErrorSideOutput-source", "SerializationErrorSideOutput-source");

        final SingleOutputStreamOperator<InputEvent> inputStream = UnParsableHelper.filterToSideTopic(this,
                unFilteredInputStream,
                InputEvent.class,
                "unParsedProcessor",
                "unParsedProcessor",
                "unParsedSink",
                "unParsedSink",
                "un-parsed");

        // Transform stream
        final SingleOutputStreamOperator<OutputEvent> outputStream = inputStream
                .map(new SerializationErrorSideOutputMapper("ExtraFieldValue"))
                .name("SerializationErrorSideOutputMapper")
                .uid("SerializationErrorSideOutputMapper");

        // Output sink
        KafkaSink<OutputEvent> kafkaSink = CheetahKafkaSink.builder(OutputEvent.class, this, "main-sink")
                .build();

        // Connect transformed stream to sink
        outputStream.sinkTo(kafkaSink)
                .name("SerializationErrorSideOutputKafkaSink")
                .uid("SerializationErrorSideOutputKafkaSink");
    }
}
