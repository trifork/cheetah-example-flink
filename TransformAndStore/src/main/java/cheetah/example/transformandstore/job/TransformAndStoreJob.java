package cheetah.example.transformandstore.job;

import cheetah.example.transformandstore.model.InputEvent;
import cheetah.example.transformandstore.model.OutputEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trifork.cheetah.processing.job.Job;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.connector.kafka.source.KafkaSource;
import com.trifork.cheetah.processing.connector.kafka.CheetahKafkaSource;
import org.apache.flink.connector.opensearch.sink.OpensearchSink;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.opensearch.action.index.IndexRequest;

import com.trifork.cheetah.processing.connector.opensearch.serde.OpensearchRequests;
import com.trifork.cheetah.processing.connector.opensearch.CheetahOpensearchSink;
import java.io.Serializable;
import java.util.Objects;

/**
 * The TransformAndStoreJob serves as an example for simple transformation and storing data in OpenSearch.
 * The Job subscribes to a Kafka topic and performs a straightforward transformation on the incoming data,
 * appending a status based on the object's value field. After this transformation, the data is stored in
 * OpenSearch using an OpenSearch Connector. */
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
        final KafkaSource<InputEvent> kafkaSource = CheetahKafkaSource.builder(InputEvent.class, this).build();
        final DataStream<InputEvent> inputStream = CheetahKafkaSource.toDataStream(this, kafkaSource, "transform-and-store-source");

        // Transform stream
        final SingleOutputStreamOperator<OutputEvent> outputStream =
                inputStream.map(new TransformAndStoreMapper());

        // Store the transformed object in OpenSearch using the OpenSearchSink.
        // In this process, the output is serialized to JSON. The index name is constructed using the indexBaseName
        // from the parameters, combined with the timestamp from the transformed object.
        // Meanwhile, the deviceId field of the transformed object serves as the index ID.
        final OpensearchSink<OutputEvent> openSearchSink = CheetahOpensearchSink.builder(OutputEvent.class,this)
                .setEmitter((element, context, indexer) -> {
                    IndexRequest indexRequest = null;
                    try {
                        indexRequest = OpensearchRequests.createIndexRequest(new ObjectMapper().writeValueAsString(element), indexBaseName +  element.parseTimestampToString(), element.getDeviceId());
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                    indexer.add(indexRequest);
                })
                .build();

        // Connect transformed stream to openSearchSink
        outputStream.sinkTo(openSearchSink).name(TransformAndStoreJob.class.getSimpleName());
    }
}