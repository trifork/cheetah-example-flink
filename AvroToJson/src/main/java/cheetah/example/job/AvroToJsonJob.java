package cheetah.example.job;

import cheetah.example.model.avrorecord.InputEventAvro;
import cheetah.example.model.json.OutputEventJson;
import com.trifork.cheetah.processing.job.Job;
import com.trifork.cheetah.processing.connector.kafka.CheetahKafkaSink;
import com.trifork.cheetah.processing.connector.kafka.CheetahKafkaSource;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;

import java.io.Serializable;

/** AvroToJsonJob sets up the data processing job. */
public class AvroToJsonJob extends Job implements Serializable {

    public static void main(final String[] args) throws Exception {
        new AvroToJsonJob().start(args);
    }

    @Override
    protected void setup() {
        // Input source
        final KafkaSource<InputEventAvro> kafkaSource = CheetahKafkaSource.avroSpecificBuilder(InputEventAvro.class, this)
                .build();

        final DataStream<InputEventAvro> inputStream = CheetahKafkaSource.toDataStream(this, kafkaSource, "AvroToJson-source");

        // Transform stream
        final SingleOutputStreamOperator<OutputEventJson> outputStream = inputStream
                .map(new AvroToJsonMapper())
                .name("AvroToJsonMapper")
                .uid("AvroToJsonMapper");

        // Output sink
        final KafkaSink<OutputEventJson> kafkaSink = CheetahKafkaSink.builder(OutputEventJson.class, this)
                .build();

        // Connect transformed stream to sink
        outputStream.sinkTo(kafkaSink)
                .name("AvroToJsonKafkaSink")
                .uid("AvroToJsonKafkaSink");
    }
}
