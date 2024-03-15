package cheetah.example.flinkstates.job;

import cheetah.example.flinkstates.function.*;
import cheetah.example.flinkstates.model.InputEvent;
import com.trifork.cheetah.processing.connector.kafka.CheetahKafkaSource;
import com.trifork.cheetah.processing.connector.kafka.config.CheetahKafkaSinkConfig;
import com.trifork.cheetah.processing.connector.kafka.config.CheetahKafkaSourceConfig;
import com.trifork.cheetah.processing.job.Job;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.source.KafkaSource;
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
        final KafkaSource<InputEvent> kafkaSource = CheetahKafkaSourceConfig.builder(this).toKafkaSourceBuilder(InputEvent.class).build();
        final KeyedStream<InputEvent, String> keyedByStream = CheetahKafkaSource.toDataStream(this, kafkaSource, "Event Input Source")
                .keyBy(InputEvent::getDeviceId);

        // Setup each mapper and corresponding sink
        mapAndSink(keyedByStream, new FlinkValueStatesMapper(), Double.class, "value");
        mapAndSink(keyedByStream, new FlinkReducingStatesMapper(), Double.class, "reducing");
        mapAndSink(keyedByStream, new FlinkAggregatingStatesMapper(), Double.class, "aggregating");
        mapAndSink(keyedByStream, new FlinkMapStatesMapper(), Double.class, "map");
        mapAndSink(keyedByStream, new FlinkListStatesMapper(), Double[].class, "list");
    }

    public <T> void mapAndSink(KeyedStream<InputEvent, String> keyedStream, RichFlatMapFunction<InputEvent, T> function, Class<T> outputType, String kafkaPostFix){

        final SingleOutputStreamOperator<T> outputStream = keyedStream.flatMap(function);
        final KafkaSink<T> sink = CheetahKafkaSinkConfig.builder(this, kafkaPostFix).toKafkaSinkBuilder(outputType).build();

        outputStream.sinkTo(sink);
    }
}
