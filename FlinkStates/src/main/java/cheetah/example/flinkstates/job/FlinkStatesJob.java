package cheetah.example.flinkstates.job;

import com.trifork.cheetah.processing.connector.kafka.config.CheetahKafkaSinkConfig;
import com.trifork.cheetah.processing.connector.kafka.config.CheetahKafkaSinkConfigBuilder;
import com.trifork.cheetah.processing.connector.kafka.config.CheetahKafkaSourceConfig;
import com.trifork.cheetah.processing.job.Job;
import io.strimzi.kafka.oauth.client.ClientConfig;
import io.strimzi.kafka.oauth.client.JaasClientOauthLoginCallbackHandler;
import io.strimzi.kafka.oauth.common.Config;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule;

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
        //Create SQL Environment
        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(getStreamExecutionEnvironment());

        //Args example:
        //--sql "INSERT INTO OutputTopicSQL SELECT * FROM InputTopicSQL WHERE deviceId like %27Mort%27" --source "InputTopicSQL" --sink "OutputTopicSQL" --sourceSql "deviceId STRING, `timestamp` BIGINT, `value` FLOAT" --sinkSql "deviceId STRING, `timestamp` BIGINT, `value` FLOAT"


        //Create source table / topic
        String userSourceTopic = getParameters().get("source");
        String userSourceSql = getParameters().get("sourceSql");
        createTable(userSourceTopic, tableEnv, userSourceSql);

        //Create sink table / topic
        String userSinkTopic = getParameters().get("sink");
        String userSinkSql = getParameters().get("sinkSql");
        createTable(userSinkTopic, tableEnv, userSinkSql);

        //Execute user SQL
        String userSQL = getParameters().get("sql").replaceAll( "%27", "'");
        tableEnv.executeSql(userSQL);


//        --Insert data
//        String partition = "INSERT INTO InputTopicSQL VALUES ('Jeff', 1111, 13.37)";
//        String partition1 = "INSERT INTO InputTopicSQL VALUES ('Martin', 2222, 88.88)";
//        String partition2 = "INSERT INTO InputTopicSQL VALUES ('Karsten', 3333, 77.7)";
//        String partition3 = "INSERT INTO InputTopicSQL VALUES ('Steen', 4444, 10.10)";
//
//        StatementSet statementSet = tableEnv.createStatementSet();
//        statementSet.addInsertSql(partition);
//        statementSet.addInsertSql(partition1);
//        statementSet.addInsertSql(partition2);
//        statementSet.addInsertSql(partition3);
//        statementSet.execute();
//
//        --Print
//        Convert table to datastream and print
//        DataStream<InputEvent> resultStream = tableEnv.toDataStream(resultTable, InputEvent.class);
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
                "'scan.bounded.mode' = 'latest-offset', " +
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
