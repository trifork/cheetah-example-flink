package cheetah.example.job;

import cheetah.example.model.FlinkStatesInputEvent;
import com.trifork.cheetah.processing.connector.kafka.KafkaDataStreamBuilder;
import com.trifork.cheetah.processing.connector.kafka.KafkaSinkBuilder;
import com.trifork.cheetah.processing.job.Job;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;

import java.io.Serializable;

/**
 * FlinkStatesJob sets up the data processing job. It contains examples of using the different types of state
 */
public class FlinkStatesJob extends Job implements Serializable {

    @SuppressWarnings("PMD.SignatureDeclareThrowsException") // Fix once lib-processing is fixed
    public static void main(final String[] args) throws Exception {
        new FlinkStatesJob().start(args);
    }

    @Override
    protected void setup() {
        // Input source
        final DataStream<FlinkStatesInputEvent> inputStream =
                KafkaDataStreamBuilder.forSource(this, FlinkStatesInputEvent.class)
                        .build();

        final KeyedStream<FlinkStatesInputEvent, String> keyedByStream = inputStream.keyBy(FlinkStatesInputEvent::getDeviceId);

        // Transform stream
        final SingleOutputStreamOperator<Double> valueOutputStream =
                keyedByStream.flatMap(new FlinkValueStatesMapper());
        // Output sink
        final KafkaSink<Double> valueSink =
                KafkaSinkBuilder.defaultKafkaConfig(this, Double.class)
                        .kafkaPostfix("-value")
                        .build();

        // Transform stream
        final SingleOutputStreamOperator<Double> reducingOutputStream =
                keyedByStream.flatMap(new FlinkReducingStatesMapper());

        // Output sink
        final KafkaSink<Double> reducingSink =
                KafkaSinkBuilder.defaultKafkaConfig(this, Double.class)
                        .kafkaPostfix("-reducing")
                        .build();

        // Transform stream
        final SingleOutputStreamOperator<Double> aggregatingOutputStream =
                keyedByStream.flatMap(new FlinkAggregatingStatesMapper());

        // Output sink
        final KafkaSink<Double> aggregatingSink =
                KafkaSinkBuilder.defaultKafkaConfig(this, Double.class)
                        .kafkaPostfix("-aggregating")
                        .build();

        // Transform stream
        final SingleOutputStreamOperator<Double> mapOutputStream =
                keyedByStream.flatMap(new FlinkMapStatesMapper());

        // Output sink
        final KafkaSink<Double> mapSink =
                KafkaSinkBuilder.defaultKafkaConfig(this, Double.class)
                        .kafkaPostfix("-map")
                        .build();

        // Transform stream
        final SingleOutputStreamOperator<Double[]> listOutputStream =
                keyedByStream.flatMap(new FlinkListStatesMapper());
        // Output sink
        final KafkaSink<Double[]> listSink =
                KafkaSinkBuilder.defaultKafkaConfig(this, Double[].class)
                        .kafkaPostfix("-list")
                        .build();

        valueOutputStream.sinkTo(valueSink);
        reducingOutputStream.sinkTo(reducingSink);
        aggregatingOutputStream.sinkTo(aggregatingSink);
        mapOutputStream.sinkTo(mapSink);
        listOutputStream.sinkTo(listSink);
    }
}
