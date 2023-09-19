package tumblingWindow.job;

import tumblingWindow.model.TumblingWindowInputEvent;
import tumblingWindow.model.TumblingWindowOutputEvent;
import com.trifork.cheetah.processing.connector.kafka.KafkaDataStreamBuilder;
import com.trifork.cheetah.processing.connector.kafka.KafkaSinkBuilder;
import com.trifork.cheetah.processing.connector.serialization.SimpleKeySerializationSchema;
import com.trifork.cheetah.processing.job.Job;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;

import java.io.Serializable;

/** TumblingWindowJob sets up the data processing job. */
public class TumblingWindowJob extends Job implements Serializable {

    @SuppressWarnings("PMD.SignatureDeclareThrowsException") // Fix once lib-processing is fixed
    public static void main(final String[] args) throws Exception {
        new TumblingWindowJob().start(args);
    }

    @Override
    protected void setup() {
        // Input source
        final DataStream<TumblingWindowInputEvent> inputStream =
                KafkaDataStreamBuilder.forSource(this, TumblingWindowInputEvent.class)
                        .offsetsInitializer(OffsetsInitializer.earliest())
                        .build();

        // Transform stream
        final SingleOutputStreamOperator<TumblingWindowOutputEvent> outputStream =
                inputStream.map(new TumblingWindowMapper("ExtraFieldValue"));

        // Output sink
        final KafkaSink<TumblingWindowOutputEvent> kafkaSink =
                KafkaSinkBuilder.defaultKafkaConfig(this, TumblingWindowOutputEvent.class)
                        .keySerializationSchema(
                                new SimpleKeySerializationSchema<>() {

                                    @Override
                                    public Object getKey(final TumblingWindowOutputEvent outputEvent) {
                                        return outputEvent.getDeviceId();
                                    }
                                })
                        .build();

        // Connect transformed stream to sink
        outputStream.sinkTo(kafkaSink).name(TumblingWindowJob.class.getSimpleName());
    }
}
