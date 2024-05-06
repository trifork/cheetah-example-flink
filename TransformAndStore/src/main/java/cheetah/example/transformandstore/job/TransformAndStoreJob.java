package cheetah.example.transformandstore.job;

import cheetah.example.transformandstore.model.InputEvent;
import cheetah.example.transformandstore.model.OutputEvent;
import com.trifork.cheetah.processing.connector.opensearch.serde.SimpleEmitter;
import com.trifork.cheetah.processing.job.Job;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.connector.kafka.source.KafkaSource;
import com.trifork.cheetah.processing.connector.kafka.CheetahKafkaSource;
import org.apache.flink.connector.opensearch.sink.OpensearchSink;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;

import com.trifork.cheetah.processing.connector.opensearch.CheetahOpensearchSink;

import java.io.Serializable;
import java.util.Objects;

/**
 * The TransformAndStoreJob serves as an example for simple transformation and storing data in OpenSearch.
 * The Job subscribes to a Kafka topic and performs a straightforward transformation on the incoming data,
 * appending a status based on the object's value field. After this transformation, the data is stored in
 * OpenSearch using an OpenSearch Connector.
 */
public class TransformAndStoreJob extends Job implements Serializable {

    @SuppressWarnings("PMD.SignatureDeclareThrowsException") // Fix once lib-processing is fixed
    public static void main(final String[] args) throws Exception {
        new TransformAndStoreJob().start(args);
    }

    @Override
    protected void setup() {
        // Get index-base-name from parameters
        ParameterTool parameters = getParameters();
        String indexBaseName = Objects.requireNonNull(parameters.get("index-base-name"), "--index-base-name is required");

        // Setup reading from input stream
        final KafkaSource<InputEvent> kafkaSource = CheetahKafkaSource.builder(InputEvent.class, this, "main-source")
                .build();
        final DataStream<InputEvent> inputStream = CheetahKafkaSource.toDataStream(this, kafkaSource, "transform-and-store-source", "transform-and-store-source");

        // Transform stream
        final SingleOutputStreamOperator<OutputEvent> outputStream =
                inputStream.map(new TransformAndStoreMapper())
                        .name("TransformAndStoreMapper")
                        .uid("TransformAndStoreMapper");

        // Store the transformed object in OpenSearch using the OpenSearchSink.
        // In this process, the output is serialized to JSON. The index name is constructed using the indexBaseName
        // from the parameters, combined with the timestamp from the transformed object.
        // Additionally, the deviceId field is intentionally set to null through the SimpleEmitter Class, which prompts
        // OpenSearch to assign a unique identifier to the stored element.
        // Lastly the OpenSearch database is set to flush every 200ms.
        final OpensearchSink<OutputEvent> openSearchSink = CheetahOpensearchSink.builder(OutputEvent.class, this)
                .setEmitter((SimpleEmitter<OutputEvent>) element -> indexBaseName + element.getYearStringFromTimestamp())
                .setBulkFlushInterval(200)
                .build();

        // Connect transformed stream to openSearchSink
        outputStream.sinkTo(openSearchSink).
                name("TransformAndStoreKafkaSink")
                .uid("TransformAndStoreKafkaSink");
    }
}