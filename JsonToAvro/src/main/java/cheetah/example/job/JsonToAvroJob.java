package cheetah.example.job;

import cheetah.example.model.avrorecord.OutputEventAvro;
import cheetah.example.model.json.InputEvent;
import com.trifork.cheetah.processing.connector.kafka.config.CheetahKafkaSourceConfig;
import com.trifork.cheetah.processing.job.Job;
import com.trifork.cheetah.processing.connector.kafka.CheetahKafkaSink;
import com.trifork.cheetah.processing.connector.kafka.CheetahKafkaSource;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;

import java.io.Serializable;

/** jsonToAvroJob sets up the data processing job. */
public class JsonToAvroJob extends Job implements Serializable {

    @SuppressWarnings("PMD.SignatureDeclareThrowsException") // Fix once lib-processing is fixed
    public static void main(final String[] args) throws Exception {
        new JsonToAvroJob().start(args);
    }

    @Override
    protected void setup() {
        // Input source
        final KafkaSource<InputEvent> kafkaSource = CheetahKafkaSourceConfig.builder(this, "jsonToAvro")
                .toKafkaSourceBuilder(InputEvent.class)
                .build();

        final DataStream<InputEvent> inputStream = CheetahKafkaSource.toDataStream(this, kafkaSource, "jsonToAvro-source");

        // Transform stream
        final SingleOutputStreamOperator<OutputEventAvro> outputStream =
                inputStream.map(new JsonToAvroMapper())
                .name("JsonToAvroMapper")
                .uid("JsonToAvroMapper");

        // Output sink
        KafkaSink<OutputEventAvro> kafkaSink = CheetahKafkaSink.avroSpecificBuilder(OutputEventAvro.class, this)
                .build();

        // Connect transformed stream to sink
        outputStream.sinkTo(kafkaSink)
                .name("JsonToAvroKafkaSink")
                .uid("JsonToAvroKafkaSink");
    }
}
