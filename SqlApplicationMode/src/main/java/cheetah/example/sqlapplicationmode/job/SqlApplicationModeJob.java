package cheetah.example.sqlapplicationmode.job;

import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.table.api.EnvironmentSettings;
import io.strimzi.kafka.oauth.client.ClientConfig;
import io.strimzi.kafka.oauth.client.JaasClientOauthLoginCallbackHandler;
import io.strimzi.kafka.oauth.common.Config;
import org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/** SqlApplicationModeJob sets up the data processing job. */
final public class SqlApplicationModeJob implements Serializable {

    private SqlApplicationModeJob() {
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(SqlApplicationModeJob.class);

    public static void main(final String[] args) throws Exception {

        SqlApplicationModeJob job = new SqlApplicationModeJob();

        ParameterTool parameters = ParameterTool.fromArgs(args);

        EnvironmentSettings settings = EnvironmentSettings.newInstance().inStreamingMode().build();
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env, settings);

        // Create a map parameters for provide information for create the tables
        Map<String, String> tableParams = new HashMap<>();
        tableParams.put("groupId", parameters.get("groupId"));
        tableParams.put("clientId", parameters.get("clientId"));

        //Create source table / topic
        tableParams.put("userTopic", parameters.get("source"));
        tableParams.put("tableSql", parameters.get("sourceSql"));
        job.createTable(tableParams, tableEnv);
        LOGGER.info("First table!--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");

        //Create another source table / topic
        tableParams.put("userTopic", parameters.get("source1"));
        tableParams.put("tableSql", parameters.get("sourceSql1"));
        job.createTable(tableParams, tableEnv);
        LOGGER.info("Second table!------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");

        //Create sink table / topic
        tableParams.put("userTopic", parameters.get("sink"));
        tableParams.put("tableSql", parameters.get("sinkSql"));
        job.createTable(tableParams, tableEnv);
        LOGGER.info("Sink table!--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");

        //Execute user SQL and put into the sink
        String userSQL = parameters.get("sql").replaceAll("%27", "'");
        tableEnv.executeSql(userSQL);
        LOGGER.info("Result executed!---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
    }

    public void createTable(Map<String, String> tableParams, StreamTableEnvironment tableEnv) {
        //Create input topic / table
        //If topic already exists - table data is based upon that and
        //any data inserted is inserted into topic aswell.
        //If topic doesnt exist - new topic is created.
        String tableSQL = "CREATE TABLE IF NOT EXISTS " + tableParams.get("userTopic") + " (" +
                tableParams.get("tableSql") +
                ") WITH (" +
                "'connector'='kafka'," +
                "'topic'='" + tableParams.get("userTopic") + "'," +
                "'properties.bootstrap.servers' = 'kafka:19092'," +
                "'properties.group.id' = '" + tableParams.get("groupId") + "'," +
                "'format'='json', " +
                "'scan.startup.mode' = 'earliest-offset', " +
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
}
