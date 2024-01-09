package cheetah.example.flinkstates.job;

import cheetah.example.flinkstates.model.InputEvent;
import com.trifork.cheetah.processing.connector.kafka.CheetahKafkaSink;
import com.trifork.cheetah.processing.connector.kafka.CheetahKafkaSource;
import com.trifork.cheetah.processing.connector.kafka.config.CheetahKafkaSinkConfig;
import com.trifork.cheetah.processing.connector.kafka.config.CheetahKafkaSourceConfig;
import com.trifork.cheetah.processing.job.Job;
import io.strimzi.kafka.oauth.client.ClientConfig;
import io.strimzi.kafka.oauth.client.JaasClientOauthLoginCallbackHandler;
import io.strimzi.kafka.oauth.common.Config;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule;

import java.io.Serializable;
import java.util.concurrent.ExecutionException;

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
        /* TO DO */
        //Fix no operator defined in flink topology exception
        //Fix SaslCallbackHandler not castable exception
        //Investigate multiple sources query
        //Investigate running multiple jobs
        //Create example of api-call to run job with args

        /* ARGS EXAMPLE */
        //--sql "INSERT INTO MultiSourceOutput SELECT InputTopicSQL.deviceId, InputTopicSQL.`timestamp`, OutputTopicSQL.`value` FROM InputTopicSQL JOIN OutputTopicSQL ON InputTopicSQL.`timestamp` = OutputTopicSQL.`timestamp` WHERE InputTopicSQL.`timestamp` > 2222" --source "InputTopicSQL" --source1 "OutputTopicSQL" --sink "MultiSourceOutput" --sourceSql "deviceId STRING, `timestamp` BIGINT, `value` FLOAT" --sourceSql1 "deviceId STRING, `timestamp` BIGINT, `value` FLOAT" --sinkSql "deviceId STRING, `timestamp` BIGINT, `value` FLOAT"

        //Query desc: inserts deviceId, timestamp, value from InputTopicSQL where timestamp matches timestamp in OutputTopicSQL and timestamp is bigger than 2222 in InputTopicSQL
        //--sql "INSERT INTO MultiSourceOutput SELECT InputTopicSQL.deviceId, InputTopicSQL.`timestamp`, OutputTopicSQL.`value` FROM InputTopicSQL JOIN OutputTopicSQL ON InputTopicSQL.`timestamp` = OutputTopicSQL.`timestamp` WHERE InputTopicSQL.`timestamp` > 2222"
        // --source "InputTopicSQL"
        // --source1 "OutputTopicSQL"
        // --sink "MultiSourceOutput"
        // --sourceSql "deviceId STRING, `timestamp` BIGINT, `value` FLOAT"
        // --sourceSql1 "deviceId STRING, `timestamp` BIGINT, `value` FLOAT"
        // --sinkSql "deviceId STRING, `timestamp` BIGINT, `value` FLOAT"

        /* LIMITATIONS */
        //The SQL-Job can execute ONE query per job not including CREATE statements


        //Create SQL Environment
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);

        //Create source table / topic
        String userSourceTopic = getParameters().get("source");
        String userSourceSql = getParameters().get("sourceSql");
        createTable(userSourceTopic, tableEnv, userSourceSql);

        //Create another source table / topic
        String userSourceTopic1 = getParameters().get("source1");
        String userSourceSql1 = getParameters().get("sourceSql1");
        createTable(userSourceTopic1, tableEnv, userSourceSql1);

        //Create sink table / topic
        String userSinkTopic = getParameters().get("sink");
        String userSinkSql = getParameters().get("sinkSql");
        createTable(userSinkTopic, tableEnv, userSinkSql);

        //Execute user SQL
        String userSQL = getParameters().get("sql").replaceAll( "%27", "'");
        tableEnv.executeSql(userSQL);



//        --Print
//        Convert table to datastream and print
//        DataStream<InputEvent> resultStream = tableEnv.toDataStream(tableEnv.from("OutputTopicSQL"), InputEvent.class);
//        resultStream.print();

    }

    public void createTable(String topicName, StreamTableEnvironment tableEnv, String tableSql) {
        //Create input topic / table
        //If topic already exists - table data is based upon that and
        //any data inserted is inserted into topic aswell.
        //If topic doesnt exist - new topic is created.
        String tableSQL = "CREATE TABLE IF NOT EXISTS " + topicName + " (" +
                tableSql +
                ") WITH (" +
                "'connector'='kafka'," +
                "'topic'='" + topicName + "'," +
                "'properties.bootstrap.servers' = 'kafka:19092'," +
                "'properties.group.id' = 'Sql-group-id'," +
                "'format'='json', " +
                "'scan.startup.mode' = 'earliest-offset', " +
        //        "'scan.bounded.mode' = 'latest-offset', " +
                "'properties.sasl.mechanism' = '" + OAuthBearerLoginModule.OAUTHBEARER_MECHANISM + "', " +
                "'properties.security.protocol' = 'SASL_PLAINTEXT', " +
                "'properties.sasl.login.callback.handler.class' = '"+ JaasClientOauthLoginCallbackHandler.class.getName() + "', " +
                "'properties.sasl.jaas.config' = '"+OAuthBearerLoginModule.class.getName() + " required "
                + Config.OAUTH_CLIENT_ID + "=\"flink\" "
                + Config.OAUTH_CLIENT_SECRET + "=\"testsecret\" "
                + Config.OAUTH_SCOPE + "=\"flink\" "
                + ClientConfig.OAUTH_TOKEN_ENDPOINT_URI + "=\"http://cheetahoauthsimulator/oauth2/token\";'"+
                ")";
        tableEnv.executeSql(tableSQL);
    }


}
