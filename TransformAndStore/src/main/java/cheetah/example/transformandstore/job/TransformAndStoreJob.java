package cheetah.example.transformandstore.job;

import cheetah.example.transformandstore.model.InputEvent;
import cheetah.example.transformandstore.model.OutputEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trifork.cheetah.processing.job.Job;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.source.KafkaSource;
import com.trifork.cheetah.processing.connector.kafka.CheetahKafkaSink;
import com.trifork.cheetah.processing.connector.kafka.CheetahKafkaSource;
import org.apache.flink.connector.opensearch.sink.OpensearchSink;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.opensearch.action.index.IndexRequest;

import com.trifork.cheetah.processing.connector.opensearch.serde.OpensearchRequests;
import com.trifork.cheetah.processing.connector.opensearch.CheetahOpensearchSink;
import org.apache.flink.connector.opensearch.sink.OpensearchSink;
import org.opensearch.action.index.IndexRequest;

import java.io.Serializable;
import java.util.Objects;

/** TransformAndStoreJob sets up the data processing job. */
public class TransformAndStoreJob extends Job implements Serializable {

    @SuppressWarnings("PMD.SignatureDeclareThrowsException") // Fix once lib-processing is fixed
    public static void main(final String[] args) throws Exception {
        new TransformAndStoreJob().start(args);
    }

    @Override
    protected void setup() {
        // get parameters
        ParameterTool parameters = getParameters();

        // Input source
        final KafkaSource<InputEvent> kafkaSource = CheetahKafkaSource.builder(InputEvent.class, this)
                .build();

        final DataStream<InputEvent> inputStream = CheetahKafkaSource.toDataStream(this, kafkaSource, "transform-and-store-source");

        // Transform stream
        final SingleOutputStreamOperator<OutputEvent> outputStream =
                inputStream.map(new TransformAndStoreMapper());

        // Output sink
        String indexBaseName = Objects.requireNonNull(parameters.get("index-base-name"), "--index-base-name is required");

        final OpensearchSink<OutputEvent> kafkaSink = CheetahOpensearchSink.builder(OutputEvent.class,this)
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

        // Connect transformed stream to sink
        outputStream.sinkTo(kafkaSink);
    }
}
