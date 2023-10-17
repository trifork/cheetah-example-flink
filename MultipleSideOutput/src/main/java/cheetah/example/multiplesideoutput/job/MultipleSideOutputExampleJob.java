package cheetah.example.multiplesideoutput.job;

import cheetah.example.multiplesideoutput.function.MultipleSideOutputExampleProcess;
import cheetah.example.multiplesideoutput.model.InputEvent;
import cheetah.example.multiplesideoutput.model.OutputEvent2;
import com.trifork.cheetah.processing.connector.kafka.CheetahKafkaSink;
import com.trifork.cheetah.processing.connector.kafka.CheetahKafkaSource;
import com.trifork.cheetah.processing.connector.kafka.config.CheetahKafkaSinkConfig;
import com.trifork.cheetah.processing.connector.kafka.config.CheetahKafkaSourceConfig;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.util.OutputTag;
import com.trifork.cheetah.processing.job.Job;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.streaming.api.datastream.DataStream;
import cheetah.example.multiplesideoutput.model.OutputEvent;

import java.io.Serializable;

/** MultipleSideOutputExampleJob sets up the data processing job.
 *  This job does not contain a main output, as each of the 3 outputs is only
 *  used if specific conditions is met.
 *  This doest not mean the main output can't be used.
 *  And it can be done just like any other main output from a function
 *  */
public class MultipleSideOutputExampleJob extends Job implements Serializable {

    @SuppressWarnings("PMD.SignatureDeclareThrowsException") // Fix once lib-processing is fixed
    public static void main(final String[] args) throws Exception {
        new MultipleSideOutputExampleJob().start(args);
    }

    // The 3 different kind of output produces by this job
    public static OutputTag<OutputEvent> outputA = new OutputTag<>("output-a"){};
    public static OutputTag<OutputEvent> outputB = new OutputTag<>("output-b"){};
    public static OutputTag<OutputEvent2> outputCD = new OutputTag<>("output-cd"){};

    @Override
    protected void setup() {
        // Input source
        final KafkaSource<InputEvent> kafkaSource = CheetahKafkaSourceConfig.builder(this).toKafkaSourceBuilder(InputEvent.class).build();
        final DataStream<InputEvent> inputStream = CheetahKafkaSource.toDataStream(this, kafkaSource, "my-source-name");

        // Process element
        final SingleOutputStreamOperator<InputEvent> dataStream = inputStream.keyBy(InputEvent::getDeviceId)
                .process(new MultipleSideOutputExampleProcess());

        //
        // Output sink for output A
        final KafkaSink<OutputEvent> kafkaSinkA = CheetahKafkaSink.builder(OutputEvent.class, CheetahKafkaSinkConfig.defaultConfig(this, "a"))
                .build();

        // Taking the side output from the data stream that has be put on to side output A
        dataStream.getSideOutput(outputA).sinkTo(kafkaSinkA);

        // Output sink for output B
        final KafkaSink<OutputEvent> kafkaSinkB = CheetahKafkaSink.builder(OutputEvent.class, CheetahKafkaSinkConfig.defaultConfig(this, "b"))
                .build();

        // Taking the side output from the data stream that has be put on to side output B
        dataStream.getSideOutput(outputB).sinkTo(kafkaSinkB);

        // Output sink for output CD
        final KafkaSink<OutputEvent2> kafkaSinkCD = CheetahKafkaSink.builder(OutputEvent2.class, CheetahKafkaSinkConfig.defaultConfig(this, "cd"))
                .build();

        // Taking the side output from the data stream that has be put on to side output CD
        dataStream.getSideOutput(outputCD).sinkTo(kafkaSinkCD);
    }
}
