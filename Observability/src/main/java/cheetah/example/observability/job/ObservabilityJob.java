package cheetah.example.observability.job;

import cheetah.example.observability.model.ObservabilityInputEvent;
import com.trifork.cheetah.processing.connector.kafka.KafkaDataStreamBuilder;
import com.trifork.cheetah.processing.connector.kafka.KafkaSinkBuilder;
import com.trifork.cheetah.processing.connector.serialization.SimpleKeySerializationSchema;
import com.trifork.cheetah.processing.job.Job;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;

import java.io.Serializable;

/** ObservabilityJob sets up the data processing job. */
public class ObservabilityJob extends Job implements Serializable {

    @SuppressWarnings("PMD.SignatureDeclareThrowsException") // Fix once lib-processing is fixed
    public static void main(final String[] args) throws Exception {
        new ObservabilityJob().start(args);
    }

    @Override
    protected void setup() {
        // Input source
        final DataStream<ObservabilityInputEvent> inputStream =
                KafkaDataStreamBuilder.forSource(this, ObservabilityInputEvent.class)
                        .build();

        // Transform stream
        final SingleOutputStreamOperator<ObservabilityInputEvent> countedStream =
                inputStream.map(new CounterMapper());
        final SingleOutputStreamOperator<ObservabilityInputEvent> gaugedStream =
                countedStream.map(new GaugeMapper());
        final SingleOutputStreamOperator<ObservabilityInputEvent> histogramStream =
                gaugedStream.map(new HistogramMapper());


        // Connect transformed stream to sink
        histogramStream.name(ObservabilityJob.class.getSimpleName());
    }
}
