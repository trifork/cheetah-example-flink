package cheetah.example.job;

import cheetah.example.function.*;
import cheetah.example.model.InputEvent;
import com.trifork.cheetah.processing.connector.kafka.KafkaDataStreamBuilder;
import com.trifork.cheetah.processing.connector.kafka.KafkaSinkBuilder;
import com.trifork.cheetah.processing.job.Job;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
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
        final DataStream<InputEvent> inputStream =
                KafkaDataStreamBuilder.forSource(this, InputEvent.class)
                        .build();

        final KeyedStream<InputEvent, String> keyedByStream = inputStream.keyBy(InputEvent::getDeviceId);

        mapAndSink(keyedByStream, new FlinkValueStatesMapper(), Double.class, "-value");
        mapAndSink(keyedByStream, new FlinkReducingStatesMapper(), Double.class, "-reducing");
        mapAndSink(keyedByStream, new FlinkAggregatingStatesMapper(), Double.class, "-aggregating");
        mapAndSink(keyedByStream, new FlinkMapStatesMapper(), Double.class, "-map");
        mapAndSink(keyedByStream, new FlinkListStatesMapper(), Double[].class, "-list");
    }

    public <T> void mapAndSink(KeyedStream<InputEvent, String> keyedStream, RichFlatMapFunction<InputEvent, T> function, Class<T> outputType, String kafkaPostFix){

        final SingleOutputStreamOperator<T> listOutputStream =
                keyedStream.flatMap(function);

        final KafkaSink<T> listSink =
                KafkaSinkBuilder.defaultKafkaConfig(this, outputType)
                        .kafkaPostfix(kafkaPostFix)
                        .build();

        listOutputStream.sinkTo(listSink);
    }
}
