package tumblingwindow.job;

import com.trifork.cheetah.processing.connector.kafka.CheetahKafkaSink;
import com.trifork.cheetah.processing.connector.kafka.CheetahKafkaSource;
import com.trifork.cheetah.processing.connector.kafka.config.CheetahKafkaSinkConfig;
import com.trifork.cheetah.processing.connector.kafka.config.CheetahKafkaSourceConfig;
import com.trifork.cheetah.processing.job.Job;
import com.trifork.cheetah.processing.util.WatermarkStrategyBuilder;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import tumblingwindow.model.EventWindow;
import tumblingwindow.model.InputEvent;

import java.io.Serializable;
import java.time.Instant;

/** TumblingWindowJob sets up the data processing job. */
public class TumblingWindowJob extends Job implements Serializable {

    @SuppressWarnings("PMD.SignatureDeclareThrowsException") // Fix once lib-processing is fixed
    public static void main(final String[] args) throws Exception {
        new TumblingWindowJob().start(args);
    }

    @Override
    protected void setup() {

        // Transform stream
        WatermarkStrategy<InputEvent> watermarkStrategy = WatermarkStrategyBuilder
                .builder(InputEvent.class)
                .eventTimestampSupplier(input -> Instant.ofEpochMilli(input.getTimestamp()))
                .build();

        CheetahKafkaSourceConfig config = CheetahKafkaSourceConfig.defaultConfig(this);
        final KafkaSource<InputEvent> kafkaSource = CheetahKafkaSource.builder(InputEvent.class, config).setStartingOffsets(OffsetsInitializer.earliest()).build();
        final DataStream<InputEvent> inputStream = CheetahKafkaSource.toDataStream(this, kafkaSource, watermarkStrategy,"Input Event Source");


        SingleOutputStreamOperator<EventWindow> outputStream = inputStream
                .assignTimestampsAndWatermarks(watermarkStrategy)
                .map(message -> {
                    System.out.println(message);
                    return message;
                })
                .keyBy(InputEvent::getDeviceId)
                .window(TumblingEventTimeWindows.of(Time.minutes(5)))
                .aggregate(new TumblingWindowAggregate(), new TumblingWindowFunction());

        // Output sink
        final KafkaSink<EventWindow> kafkaSink =
                CheetahKafkaSink.builder(EventWindow.class, CheetahKafkaSinkConfig.defaultConfig(this))
                        .build();

        // Connect transformed stream to sink
        outputStream.sinkTo(kafkaSink).name(TumblingWindowJob.class.getSimpleName());
    }
}
