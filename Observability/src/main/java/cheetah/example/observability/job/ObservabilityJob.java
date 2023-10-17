package cheetah.example.observability.job;

import cheetah.example.observability.model.InputEvent;
import com.trifork.cheetah.processing.connector.kafka.CheetahKafkaSource;
import com.trifork.cheetah.processing.connector.kafka.config.CheetahKafkaSourceConfig;
import com.trifork.cheetah.processing.job.Job;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;

import java.io.Serializable;

/** ObservabilityJob sets up the data processing job, whose only purpose is to show how to set up custom metrics. */
public class ObservabilityJob extends Job implements Serializable {

    @SuppressWarnings("PMD.SignatureDeclareThrowsException") // Fix once lib-processing is fixed
    public static void main(final String[] args) throws Exception {
        new ObservabilityJob().start(args);
    }

    @Override
    protected void setup() {
        // Input source
        final KafkaSource<InputEvent> kafkaSource = CheetahKafkaSourceConfig.builder(this).toKafkaSourceBuilder(InputEvent.class).build();

        final DataStream<InputEvent> inputStream = CheetahKafkaSource.toDataStream(this, kafkaSource,"my-source-name");

        //Use three distinct mappers to add the different types of metrics, that are available
        final SingleOutputStreamOperator<InputEvent> countedStream =
                inputStream.map(new CounterMapper());
        final SingleOutputStreamOperator<InputEvent> gaugedStream =
                countedStream.map(new GaugeMapper());
        final SingleOutputStreamOperator<InputEvent> histogramStream =
                gaugedStream.map(new HistogramMapper());

        histogramStream.name(ObservabilityJob.class.getSimpleName());
    }
}
