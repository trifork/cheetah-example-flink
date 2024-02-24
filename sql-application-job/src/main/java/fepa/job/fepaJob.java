package fepa.job;

import fepa.model.InputEvent;
import fepa.model.OutputEvent;
import fepa.model.SQLevent;

import com.trifork.cheetah.processing.job.Job;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.source.KafkaSource;
import com.trifork.cheetah.processing.connector.kafka.CheetahKafkaSink;
import com.trifork.cheetah.processing.connector.kafka.CheetahKafkaSource;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;

import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.table.api.EnvironmentSettings;
import io.strimzi.kafka.oauth.client.ClientConfig;
import io.strimzi.kafka.oauth.client.JaasClientOauthLoginCallbackHandler;
import io.strimzi.kafka.oauth.common.Config;
import org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule;

import org.apache.flink.table.api.*;
import static org.apache.flink.table.api.Expressions.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.Serializable;


public class fepaJob extends Job implements Serializable {
//public class fepaJob {

    private static final Logger logger = LoggerFactory.getLogger(fepaJob.class);

    public static void main(final String[] args) throws Exception {

        // new fepaJob().start(args);

//    public static void main(String[] args) throws Exception {

        ParameterTool parameters = ParameterTool.fromArgs(args);

        EnvironmentSettings settings = EnvironmentSettings.newInstance().inStreamingMode().build();
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);

        /* TO DO */
        //Fix no operator defined in flink topology exception
        //Fix SaslCallbackHandler not castable exception
        //Investigate multiple sources query
        //Investigate running multiple jobs
        //Create example of api-call to run job with args

        /* ARGS EXAMPLE */
        //--sql "INSERT INTO MultiSourceOutput SELECT InputTopicSQL.deviceId, InputTopicSQL.`timestamp`, OutputTopicSQL.`value` FROM InputTopicSQL JOIN OutputTopicSQL ON InputTopicSQL.`timestamp` = OutputTopicSQL.`timestamp` WHERE InputTopicSQL.`timestamp` > 2222" --source "InputTopicSQL" --source1 "OutputTopicSQL" --sink "MultiSourceOutput" --sourceSql "deviceId STRING, `timestamp` BIGINT, `value` FLOAT" --sourceSql1 "deviceId STRING, `timestamp` BIGINT, `value` FLOAT" --sinkSql "deviceId STRING, `timestamp` BIGINT, `value` FLOAT" --groupId "Sql-group-id" --clientId "Sql-client-id"

        //Query desc: inserts deviceId, timestamp, value from InputTopicSQL where timestamp matches timestamp in OutputTopicSQL and timestamp is bigger than 2222 in InputTopicSQL
        // --sql "INSERT INTO MultiSourceOutput SELECT InputTopicSQL.deviceId, InputTopicSQL.`timestamp`, OutputTopicSQL.`value` FROM InputTopicSQL JOIN OutputTopicSQL ON InputTopicSQL.`timestamp` = OutputTopicSQL.`timestamp` WHERE InputTopicSQL.`timestamp` > 2222"
        // --source "InputTopicSQL"
        // --source1 "OutputTopicSQL"
        // --sink "MultiSourceOutput"
        // --sourceSql "deviceId STRING, `timestamp` BIGINT, `value` FLOAT"
        // --sourceSql1 "deviceId STRING, `timestamp` BIGINT, `value` FLOAT"
        // --sinkSql "deviceId STRING, `timestamp` BIGINT, `value` FLOAT"
        // --groupId "Sql-group-id"
        // --clientId "Sql-client-id"

        /* LIMITATIONS */
        //The SQL-Job can execute ONE query per job not including CREATE statements


//        //Create SQL Environment
//        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
//        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);

        //Extract groupId and clientId
        String groupId = parameters.get("groupId");
        String clientId = parameters.get("clientId");


        //Create source table / topic
        String userSourceTopic = parameters.get("source");
        String userSourceSql = parameters.get("sourceSql");
        createTable(userSourceTopic, tableEnv, userSourceSql, groupId, clientId);
        logger.info("First table!-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");

        //Create another source table / topic
        String userSourceTopic1 = parameters.get("source1");
        String userSourceSql1 = parameters.get("sourceSql1");
        createTable(userSourceTopic1, tableEnv, userSourceSql1, groupId, clientId);
        logger.info("Second table!-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");

        //Create sink table / topic
        String userSinkTopic = parameters.get("sink");
        String userSinkSql = parameters.get("sinkSql");
        createTable(userSinkTopic, tableEnv, userSinkSql, groupId, clientId);
        logger.info("Third table!-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");

        //Execute user SQL
        String userSQL = parameters.get("sql").replaceAll("%27", "'");
        tableEnv.executeSql(userSQL);
        logger.info("Result executed!-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        // To the sink
        DataStream<InputEvent> resultStream = tableEnv.toDataStream(tableEnv.from("fepaOutputTopic"), InputEvent.class);
        logger.info("Result!-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        resultStream.print();

    }

//        --Print
//        Convert table to datastream and print
//        DataStream<InputEvent> resultStream = tableEnv.toDataStream(tableEnv.from("fepaOutputTopic"), InputEvent.class);
//        resultStream.print();


    static public void createTable(String topicName, StreamTableEnvironment tableEnv, String tableSql, String groupId, String clientId) {
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
                "'properties.group.id' = '" + groupId + "'," +
                "'format'='json', " +
                "'scan.startup.mode' = 'earliest-offset', " +
                //        "'scan.bounded.mode' = 'latest-offset', " +
                "'properties.sasl.mechanism' = '" + OAuthBearerLoginModule.OAUTHBEARER_MECHANISM + "', " +
                "'properties.security.protocol' = 'SASL_PLAINTEXT', " +
                "'properties.sasl.login.callback.handler.class' = '" + JaasClientOauthLoginCallbackHandler.class.getName() + "', " +
                "'properties.sasl.jaas.config' = '" + OAuthBearerLoginModule.class.getName() + " required "
                + Config.OAUTH_CLIENT_ID + "= " + "default-access" + " "
                + Config.OAUTH_CLIENT_SECRET + "=\"default-access-secret\" "
                + Config.OAUTH_SCOPE + "=\"kafka\" "
                + ClientConfig.OAUTH_TOKEN_ENDPOINT_URI + "=\"http://keycloak:1852/realms/local-development/protocol/openid-connect/token\";'" +
                ")";

        tableEnv.executeSql(tableSQL);
    }

    @Override
    protected void setup() {
        // Input source
        final KafkaSource<InputEvent> kafkaSource = CheetahKafkaSource.builder(InputEvent.class, this)
                .build();

        final DataStream<InputEvent> inputStream = CheetahKafkaSource.toDataStream(this, kafkaSource, "fepa-source");

        // Transform stream
        final SingleOutputStreamOperator<OutputEvent> outputStream = inputStream
                .map(new fepaMapper("ExtraFieldValue"));

        // Output sink
        KafkaSink<OutputEvent> kafkaSink = CheetahKafkaSink.builder(OutputEvent.class, this)
                .build();

        // Connect transformed stream to sink
        outputStream.sinkTo(kafkaSink);
    }

//        for (int i = 0; i < args.length; i++) {
//            String stmt = sql_query.replaceAll("\\n", "").trim();
//            if (!stmt.isEmpty() && !stmt.startsWith("--")) {
//                logger.info("Executing statement #{}: {}", i, stmt);
//                try {
//                    tEnv.executeSql(stmt);
//                } catch (Exception e) {
//                    logger.error("Error executing SQL statement #{}: `{}`", i, stmt, e);
//                    throw e;
//                }
//            }
//        }
//    }
}


