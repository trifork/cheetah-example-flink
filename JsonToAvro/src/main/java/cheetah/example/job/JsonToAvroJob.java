package cheetah.example.job;

import cheetah.example.model.avrorecord.EventAvro;
import cheetah.example.model.json.InputEvent;
import com.trifork.cheetah.processing.connector.kafka.config.CheetahKafkaSourceConfig;
import com.trifork.cheetah.processing.job.Job;
import com.trifork.cheetah.processing.connector.kafka.CheetahKafkaSink;
import com.trifork.cheetah.processing.connector.kafka.CheetahKafkaSource;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import com.trifork.cheetah.processing.connector.serialization.SimpleKeySerializationSchema;
import com.trifork.cheetah.processing.connector.kafka.config.CheetahKafkaSinkConfig;
import com.trifork.cheetah.processing.connector.kafka.serde.AvroSerdeSchemas;
import com.trifork.cheetah.processing.connector.kafka.serde.CheetahSerdeSchemas;
import cheetah.example.serialization.AvroKeySerializationSchema;
import org.apache.flink.api.common.serialization.SerializationSchema;

import java.io.Serializable;
import java.util.Properties;

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
        final SingleOutputStreamOperator<EventAvro> outputStream =
                inputStream.map(new JsonToAvroMapper());

        final String compressionType = System.getenv("KAFKA_COMPRESSION_TYPE") != null ? System.getenv("KAFKA_COMPRESSION_TYPE") : "none";

        SerializationSchema<EventAvro> avroKeySerializer = new AvroKeySerializationSchema();
        SerializationSchema<EventAvro> staticKeySerializer = new SimpleKeySerializationSchema<>() {
                            @Override
                            public Object getKey(EventAvro object) {
                                    return "/tmp/data/ready/measuredata/2022/03/06/TelemetryData_20220306230857077.ktd#4869"; // fake key
                                }
        };

        // Output sink
        CheetahKafkaSinkConfig kafkaConfig =  CheetahKafkaSinkConfig.defaultConfig(this);
        Properties producerProperties = kafkaConfig.getProducerProperties();
        producerProperties.setProperty("compression.type", compressionType);
        KafkaSink<EventAvro> kafkaSink = CheetahKafkaSink.avroSpecificBuilder(EventAvro.class, kafkaConfig)
            .setKafkaProducerConfig(producerProperties)
            .setProperty("compression.type", compressionType)
            .setRecordSerializer(
                CheetahSerdeSchemas.kafkaRecordSerializationSchema(kafkaConfig, 
                        staticKeySerializer,
                        AvroSerdeSchemas.avroSerializationSchema(EventAvro.class, kafkaConfig.topic + "-value", kafkaConfig)
                        )
                    )
                .build();

        // Connect transformed stream to sink
        outputStream.sinkTo(kafkaSink);
    }
}


