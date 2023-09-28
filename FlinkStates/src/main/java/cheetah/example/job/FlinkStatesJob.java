package cheetah.example.job;

import cheetah.example.model.FlinkStatesInputEvent;
import com.trifork.cheetah.processing.connector.kafka.KafkaDataStreamBuilder;
import com.trifork.cheetah.processing.job.Job;
import org.apache.flink.streaming.api.datastream.DataStream;
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

        // Transform stream
        final SingleOutputStreamOperator<Double> valueOutputStream =
                inputStream.flatMap(new FlinkValueStatesMapper());

        // Transform stream
        final SingleOutputStreamOperator<Double> sumOutputStream =
                inputStream.flatMap(new FlinkReducingStatesMapper());

        // Transform stream
        final SingleOutputStreamOperator<Double[]> listOutputStream =
                inputStream.flatMap(new FlinkListStatesMapper());


    }
}
