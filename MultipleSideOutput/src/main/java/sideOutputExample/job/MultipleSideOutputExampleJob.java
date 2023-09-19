package sideOutputExample.job;

import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.util.OutputTag;
import sideOutputExample.model.InputEvent;
import com.trifork.cheetah.processing.connector.kafka.KafkaDataStreamBuilder;
import com.trifork.cheetah.processing.connector.kafka.KafkaSinkBuilder;
import com.trifork.cheetah.processing.connector.serialization.SimpleKeySerializationSchema;
import com.trifork.cheetah.processing.job.Job;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.streaming.api.datastream.DataStream;
import sideOutputExample.model.OutputEvent;
import sideOutputExample.model.OutputEvent2;

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
        final DataStream<InputEvent> inputStream =
                KafkaDataStreamBuilder.forSource(this, InputEvent.class)
                        .build();

        // Process element
        final SingleOutputStreamOperator<InputEvent> dataStream = inputStream.keyBy(InputEvent::getDeviceId)
                .process(new MultipleSideOutputExampleProcess());

        //
        // Output sink for output A
        final KafkaSink<OutputEvent> kafkaSinkA =
                KafkaSinkBuilder.defaultKafkaConfig(this, OutputEvent.class)
                        .topic("OutputA-events")
                        .keySerializationSchema(
                                new SimpleKeySerializationSchema<>() {

                                    @Override
                                    public Object getKey(final OutputEvent outputEvent) {
                                        return outputEvent.getDeviceId();
                                    }
                                })
                        .build();
        // Taking the side output from the data stream that has be put on to side output A
        dataStream.getSideOutput(outputA).sinkTo(kafkaSinkA);

        // Output sink for output B
        final KafkaSink<OutputEvent> kafkaSinkB =
                KafkaSinkBuilder.defaultKafkaConfig(this, OutputEvent.class)
                        .topic("OutputB-events")
                        .keySerializationSchema(
                                new SimpleKeySerializationSchema<>() {

                                    @Override
                                    public Object getKey(final OutputEvent outputEvent) {
                                        return outputEvent.getDeviceId();
                                    }
                                })
                        .build();
        // Taking the side output from the data stream that has be put on to side output B
        dataStream.getSideOutput(outputB).sinkTo(kafkaSinkB);

        // Output sink for output CD
        final KafkaSink<OutputEvent2> kafkaSinkCD =
                KafkaSinkBuilder.defaultKafkaConfig(this, OutputEvent2.class)
                        .topic("OutputCD-events")
                        .keySerializationSchema(
                                new SimpleKeySerializationSchema<>() {

                                    @Override
                                    public Object getKey(final OutputEvent2 outputEvent) {
                                        return outputEvent.getDeviceId();
                                    }
                                })
                        .build();
        // Taking the side output from the data stream that has be put on to side output CD
        dataStream.getSideOutput(outputCD).sinkTo(kafkaSinkCD);
    }
}
