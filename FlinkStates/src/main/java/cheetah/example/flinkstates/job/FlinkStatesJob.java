package cheetah.example.flinkstates.job;

import cheetah.example.flinkstates.function.*;
import cheetah.example.flinkstates.model.InputEvent;
import com.trifork.cheetah.processing.connector.kafka.CheetahKafkaSource;
import com.trifork.cheetah.processing.connector.kafka.config.CheetahKafkaSinkConfig;
import com.trifork.cheetah.processing.connector.kafka.config.CheetahKafkaSourceConfig;
import com.trifork.cheetah.processing.job.Job;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.TableResult;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.types.Row;
import org.apache.flink.util.CloseableIterator;

import java.io.Serializable;

/**
 * FlinkStatesJob sets up the data processing job. It contains examples of using the different types of state
 */
public class FlinkStatesJob extends Job implements Serializable {

    @SuppressWarnings("PMD.SignatureDeclareThrowsException") // Fix once lib-processing is fixed
    public static void main(final String[] args) throws Exception {
        int index;

        for (index = 0; index < args.length; ++index)
        {
            System.out.println("args[" + index + "]: " + args[index]);
        }
        new FlinkStatesJob().start(args);
    }

    @Override
    protected void setup() throws Exception {

        // Input source
        final KafkaSource<InputEvent> kafkaSource = CheetahKafkaSourceConfig.builder(this).toKafkaSourceBuilder(InputEvent.class).build();
        final KeyedStream<InputEvent, String> keyedByStream = CheetahKafkaSource.toDataStream(this, kafkaSource, "Event Input Source")
                .keyBy(InputEvent::getDeviceId);

        // Setup each mapper and corresponding sink
        mapAndSink(keyedByStream, new FlinkValueStatesMapper(), Double.class, "value");
        mapAndSink(keyedByStream, new FlinkReducingStatesMapper(), Double.class, "reducing");
        mapAndSink(keyedByStream, new FlinkAggregatingStatesMapper(), Double.class, "aggregating");
        mapAndSink(keyedByStream, new FlinkMapStatesMapper(), Double.class, "map");
        mapAndSink(keyedByStream, new FlinkListStatesMapper(), Double[].class, "list");


        //SQL

        //Get SQL Environment
        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(keyedByStream.getExecutionEnvironment());

        //Connect to topics seen on RedPanda
        String x = "FlinkStatesInputTopic";
        tableEnv.executeSql("CREATE TABLE FlinkStatesTable (`user` BIGINT, product STRING, amount INT) WITH ('connector'='kafka','topic'='" + x + "','properties.bootstrap.servers' = 'localhost:9093','properties.group.id' = 'FlinkStates-group-id','format'='json', 'scan.startup.mode' = 'earliest-offset', 'properties.auto.offset.reset' = 'earliest')");

        //Insert statement
        //tableEnv.executeSql("INSERT INTO Orders VALUES (1, 'Jeff', 2)");

        //Show databases, it's possible to create new ones
        tableEnv.executeSql("SHOW tables FROM default_database").print();

        //Select statement
        tableEnv.executeSql("SELECT * FROM FlinkStatesTable").print();

        //TableResult tableResult2 = tableEnv.sqlQuery("SELECT * FROM FlinkStatesTable").execute();
        //tableResult2.print();
    }

    public <T> void mapAndSink(KeyedStream<InputEvent, String> keyedStream, RichFlatMapFunction<InputEvent, T> function, Class<T> outputType, String kafkaPostFix){

        final SingleOutputStreamOperator<T> outputStream = keyedStream.flatMap(function);
        final KafkaSink<T> sink = CheetahKafkaSinkConfig.builder(this, kafkaPostFix).toSinkBuilder(outputType).build();

        outputStream.sinkTo(sink);
    }
}
