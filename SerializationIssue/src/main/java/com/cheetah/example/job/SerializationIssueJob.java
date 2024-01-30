package com.cheetah.example.job;

import com.cheetah.example.function.FilterMessage;
import com.cheetah.example.model.InputEvent;
import com.cheetah.example.model.OutputEvent;
import com.trifork.cheetah.processing.job.Job;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.source.KafkaSource;
import com.trifork.cheetah.processing.connector.kafka.CheetahKafkaSink;
import com.trifork.cheetah.processing.connector.kafka.CheetahKafkaSource;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;

import java.io.Serializable;

/** SerializationIssueJob sets up the data processing job. */
public class SerializationIssueJob extends Job implements Serializable {

    public static void main(final String[] args) throws Exception {
        new SerializationIssueJob().start(args);
    }

    @Override
    protected void setup() {
        // Input source
        final KafkaSource<InputEvent> kafkaSource = CheetahKafkaSource.builder(InputEvent.class, this)
                .build();

        final DataStream<InputEvent> inputStream = CheetahKafkaSource.toDataStream(this, kafkaSource, "SerializationIssue-source");

        // Transform stream
        final SingleOutputStreamOperator<OutputEvent> outputStream = inputStream
                .filter(new FilterMessage())
                .map(new SerializationIssueMapper("ExtraFieldValue"));

        // Output sink
        KafkaSink<OutputEvent> kafkaSink = CheetahKafkaSink.builder(OutputEvent.class, this)
                .build();

        // Connect transformed stream to sink
        outputStream.sinkTo(kafkaSink);
    }
}
