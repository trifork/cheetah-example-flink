package cheetah.example.flinkstates.job;

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
        //SQL

        //Get SQL Environment
        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(getStreamExecutionEnvironment());


        //Queries
        String createSQL = "CREATE TABLE IF NOT EXISTS FlinkStatesTable (deviceId STRING, `timestamp` BIGINT, `value` FLOAT) WITH ('connector'='kafka','topic'='FlinkStatesInputTopic','properties.bootstrap.servers' = 'kafka:19093','properties.group.id' = 'FlinkStates-group-id','format'='json', 'scan.startup.mode' = 'earliest-offset')";
        String insertSQL = "INSERT INTO FlinkStatesTable VALUES ('Jeff2', 5555, 12.34)";
        String selectSQL = "SELECT * FROM FlinkStatesTable";
        String showSQL = "SHOW PARTITIONS FlinkStatesTable";

        //Args example:
        //--sql "INSERT INTO OutputTopicSQL SELECT * FROM InputTopicSQL WHERE deviceId like %27Martin%27" --source "InputTopicSQL" --sink "OutputTopicSQL"

        //Create input topic / table
        //If topic already exists - table data is based upon that and
        //any data inserted is inserted into topic aswell.
        //If topic doesnt exist - new topic is created.
        String userSourceTopic = getParameters().get("source");
        String sourceTableSQL = "CREATE TABLE IF NOT EXISTS " + userSourceTopic + " (" +
                "deviceId STRING, " +
                "`timestamp` BIGINT, " +
                "`value` FLOAT" +
                ") WITH (" +
                "'connector'='kafka'," +
                "'topic'='" + userSourceTopic + "'," +
                "'properties.bootstrap.servers' = 'kafka:19092'," +
                "'properties.group.id' = 'FlinkStates-group-id'," +
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
        tableEnv.executeSql(sourceTableSQL);



        //Create output topic / table
        String userSinkTopic = getParameters().get("sink");
        String sinkTableSQL = "CREATE TABLE IF NOT EXISTS " + userSinkTopic + " (" +
                "deviceId STRING, " +
                "`timestamp` BIGINT, " +
                "`value` FLOAT) " +
                "WITH (" +
                "'connector'='kafka'," +
                "'topic'='" + userSinkTopic + "'," +
                "'properties.bootstrap.servers' = 'kafka:19092'," +
                "'properties.group.id' = 'FlinkStates-group-id'," +
                "'format'='json', " +
                "'scan.startup.mode' = 'earliest-offset', " +
                "'properties.sasl.mechanism' = '" + OAuthBearerLoginModule.OAUTHBEARER_MECHANISM + "', " +
                "'properties.security.protocol' = 'SASL_PLAINTEXT', " +
                "'properties.sasl.login.callback.handler.class' = '"+ JaasClientOauthLoginCallbackHandler.class.getName() + "', " +
                "'properties.sasl.jaas.config' = '"+OAuthBearerLoginModule.class.getName() + " required "
                + Config.OAUTH_CLIENT_ID + "=\"flink\" "
                + Config.OAUTH_CLIENT_SECRET + "=\"testsecret\" "
                + Config.OAUTH_SCOPE + "=\"flink\" "
                + ClientConfig.OAUTH_TOKEN_ENDPOINT_URI + "=\"http://cheetahoauthsimulator/oauth2/token\";'"+
                ")";
        tableEnv.executeSql(sinkTableSQL);

        //Execute user SQL
        String userSQL = getParameters().get("sql").replaceAll( "%27", "'");
        tableEnv.executeSql(userSQL);






        //Insert some data
//        String partition = "INSERT INTO InputTableSQL VALUES ('Jeff', " + System.currentTimeMillis() + ", 13.37)";
//        String partition1 = "INSERT INTO InputTableSQL VALUES ('Martin', " + System.currentTimeMillis() + ", 80.085)";
//        String partition2 = "INSERT INTO InputTableSQL VALUES ('Karsten', " + System.currentTimeMillis() + ", 77.7)";
//        String partition3 = "INSERT INTO InputTableSQL VALUES ('Steen', " + System.currentTimeMillis() + ", 10.10)";
//
//        StatementSet statementSet = tableEnv.createStatementSet();
//        statementSet.addInsertSql(partition);
//        statementSet.addInsertSql(partition1);
//        statementSet.addInsertSql(partition2);
//        statementSet.addInsertSql(partition3);
//        statementSet.execute();

        //Print
        //Convert table to datastream and print
        //DataStream<InputEvent> resultStream = tableEnv.toDataStream(resultTable, InputEvent.class);
        //resultStream.print();

    }
}
