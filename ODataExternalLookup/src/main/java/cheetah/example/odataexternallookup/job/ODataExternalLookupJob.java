package cheetah.example.odataexternallookup.job;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.streaming.api.datastream.AsyncDataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.kafka.clients.producer.ProducerRecord;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trifork.cheetah.processing.connector.kafka.config.CheetahKafkaSinkConfig;
import com.trifork.cheetah.processing.job.Job;
import com.trifork.cheetah.processing.util.Strings;

import cheetah.example.odataexternallookup.function.ODataLookupMapper;
import cheetah.example.odataexternallookup.function.JmsQueueSource;

/**
 * ExternalLookupJob sets up the data processing job.
 */
public class ODataExternalLookupJob extends Job implements Serializable {

        @SuppressWarnings("PMD.SignatureDeclareThrowsException") // Fix once lib-processing is fixed
        public static void main(final String[] args) throws Exception {
                new ODataExternalLookupJob().start(args);
        }

        @Override
        protected void setup() throws IOException {

                final StreamExecutionEnvironment env = this.getStreamExecutionEnvironment();

                final String sourceName = "odata_eventmesh_source";

                // Get configuration from ENV
                final String odata_url = System.getenv("ODATA_ENTITYURL");
                if (odata_url == null) {
                        throw new IllegalArgumentException("ODATA_ENTITYURL is not set");
                }
                final String odata_username = System.getenv("ODATA_USERNAME");
                if (odata_username == null) {
                        throw new IllegalArgumentException("ODATA_USERNAME is not set");
                }
                final String odata_password = System.getenv("ODATA_PASSWORD");
                if (odata_password == null) {
                        throw new IllegalArgumentException("ODATA_PASSWORD is not set");
                }
                final String entity_queueName = System.getenv("ODATA_QUEUENAME");
                if (entity_queueName == null) {
                        throw new IllegalArgumentException("ODATA_QUEUENAME is not set");
                }
                final String entityIdPath = System.getenv("ODATA_ENTITYID_PATH");
                if (entityIdPath == null) {
                        throw new IllegalArgumentException("ODATA_ENTITYID_PATH is not set");
                }

                var credentials = jsonFileToMap("/etc/odata-external-lookup-job-config.json");

                // Source
                var inputStream = env
                                .addSource(new JmsQueueSource(credentials, entity_queueName))
                                .uid(Strings.toKebabCase(sourceName)).rebalance();

                // Transform stream
                final SingleOutputStreamOperator<org.json.JSONObject> outputStream = AsyncDataStream.orderedWait(
                                inputStream,
                                new ODataLookupMapper(odata_url, odata_username,
                                                odata_password, entityIdPath),
                                60,
                                TimeUnit.SECONDS, 20);

                // TODO: Sideoutput? with resultFuture.complete #Exceptionally(e) in
                // ODataLookupMapper and perhaps add some metsdata somehow

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
                                .build();

                // Connect transformed stream to sink
                outputStream.sinkTo(kafkaSink).name(ODataExternalLookupJob.class.getSimpleName());
        }

        public static Map<String, Object> jsonFileToMap(String path) throws IOException {
                ObjectMapper mapper = new ObjectMapper();
                File src = new File(path);
                if (!src.exists()) {
                        throw new IOException("Configuration File not found: " + path);
                }
                var valueTypeRef = new TypeReference<Map<String, Object>>() {
                };
                return mapper.readValue(src, valueTypeRef);
        }
}