package cheetah.example.odataimporter.job;

import com.trifork.cheetah.processing.connector.kafka.config.CheetahKafkaSinkConfig;
import com.trifork.cheetah.processing.job.Job;
import com.trifork.cheetah.processing.util.Strings;

import cheetah.example.odataimporter.function.ODataV2SourceFunction;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.io.Serializable;

/**
 * ExternalLookupJob sets up the data processing job.
 */
public class ODataImporterJob extends Job implements Serializable {

        @SuppressWarnings("PMD.SignatureDeclareThrowsException") // Fix once lib-processing is fixed
        public static void main(final String[] args) throws Exception {
                new ODataImporterJob().start(args);
        }

        @SuppressWarnings("deprecation")
        @Override
        protected void setup() {
                final StreamExecutionEnvironment env = this.getStreamExecutionEnvironment();

                final String sourceName = "odata_source";
                // Get configuration from ENV
                final String odata_url = System.getenv("ODATA_URL");
                final String odata_username = System.getenv("ODATA_USERNAME");
                final String odata_password = System.getenv("ODATA_PASSWORD");
                final String odata_entityset = System.getenv("ODATA_ENTIYSET_NAME");

                // Source
                var source = env
                                .addSource(new ODataV2SourceFunction(odata_url, odata_entityset, odata_username,
                                                odata_password),
                                                sourceName)
                                // .fromSource(new ODataV4SourceFunction(odata_url, odata_entityset,
                                // odata_username, odata_password),
                                // WatermarkStrategy.noWatermarks(), sourceName)
                                .uid(Strings.toKebabCase(sourceName)).rebalance();

                // todo: output stream for errors
                var datastream = source.map(new ODataTypesMapper(true, false, true))
                                .name("odatatypes_mapper")
                                .uid("odatatypes_mapper");

                // Kafka sink config
                CheetahKafkaSinkConfig kafkaSinkConfig = CheetahKafkaSinkConfig.defaultConfig(this);

                // Output sink
                final KafkaSink<org.json.JSONObject> kafkaSink = CheetahKafkaSinkConfig.builder(this)
                                .toKafkaSinkBuilder(org.json.JSONObject.class)
                                .setRecordSerializer((element, context, timestamp) -> {
                                        var producerRecord = new ProducerRecord<>(
                                                        element.getJSONObject("__metadata").getString("type"), // topic
                                                        element.getJSONObject("__metadata").getString("id").getBytes(), // key
                                                        element.toString().getBytes()); // value
                                        return producerRecord;
                                })
                                // .setRecordSerializer(CheetahSerdeSchemas.kafkaRecordSerializationSchema(
                                // // consider a
                                // // key from
                                // // _metadata
                                // kafkaSinkConfig,
                                // new JSONObjectSerializationSchema()))
                                .build();

                // Connect transformed stream to sink
                datastream.sinkTo(kafkaSink).name(ODataImporterJob.class.getSimpleName());
        }
}
