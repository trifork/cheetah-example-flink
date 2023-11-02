package cheetah.example.mergetwostreams.job;

import cheetah.example.mergetwostreams.function.EventMerger;
import cheetah.example.mergetwostreams.model.InputEventA;
import cheetah.example.mergetwostreams.model.InputEventB;
import cheetah.example.mergetwostreams.model.OutputEvent;
import com.trifork.cheetah.processing.connector.kafka.CheetahKafkaSink;
import com.trifork.cheetah.processing.connector.kafka.CheetahKafkaSource;
import com.trifork.cheetah.processing.connector.kafka.config.CheetahKafkaSourceConfig;
import com.trifork.cheetah.processing.job.Job;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;

import java.io.Serializable;

/**
 * The MergeTwoStreamsJob is meant to show how to merge two streams into one.
 * The two streams in this example are meant to both contain a deviceId, which will be used for the pairing
 * For each element in Stream B, we will output an element, if we have already seen the device in Stream A
 */
public class MergeTwoStreamsJob extends Job implements Serializable {

    @SuppressWarnings("PMD.SignatureDeclareThrowsException") // Fix once lib-processing is fixed
    public static void main(final String[] args) throws Exception {
        new MergeTwoStreamsJob().start(args);
    }

    @Override
    protected void setup() {
        // Setup reading from Stream A
        final KafkaSource<InputEventA> kafkaSourceA = CheetahKafkaSourceConfig.builder(this,"a")
                .toKafkaSourceBuilder(InputEventA.class)
                .build();

        final DataStream<InputEventA> inputStreamA  = CheetahKafkaSource.toDataStream(this, kafkaSourceA, "my-source-name-a");

        // Setup reading from Stream B
        final KafkaSource<InputEventB> kafkaSourceB = CheetahKafkaSourceConfig.builder(this, "b")
                .toKafkaSourceBuilder(InputEventB.class)
                .build();

        final DataStream<InputEventB> inputStreamB = CheetahKafkaSource.toDataStream(this, kafkaSourceB, "my-source-name-b");

        // Merge the two streams by connecting them, giving the KeyBy, which tells which fields to merge by.
        // Final processing is done by the Enricher
        final SingleOutputStreamOperator<OutputEvent> outputStream = inputStreamA
                        .connect(inputStreamB)
                        .keyBy(InputEventA::getDeviceId, InputEventB::getDeviceId)
                        .process(new EventMerger());

        // Output the result to a new Stream
       final KafkaSink<OutputEvent> kafkaSink = CheetahKafkaSink.builder(OutputEvent.class, this)
               .build();

        // Connect transformed stream to sink
        outputStream.sinkTo(kafkaSink).name(MergeTwoStreamsJob.class.getSimpleName());
    }
}
