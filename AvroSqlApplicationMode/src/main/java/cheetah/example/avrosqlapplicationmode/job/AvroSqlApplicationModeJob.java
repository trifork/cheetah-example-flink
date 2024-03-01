package cheetah.example.avrosqlapplicationmode.job;

import cheetah.example.avrosqlapplicationmode.model.InputEvent;
import cheetah.example.avrosqlapplicationmode.model.OutputEvent;
import cheetah.example.avrosqlapplicationmode.job.SchemaConverter;
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

import io.apicurio.registry.serde.SerdeConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import io.apicurio.registry.serde.avro.AvroKafkaSerdeConfig;
import io.apicurio.rest.client.config.ApicurioClientConfig;
import io.apicurio.registry.rest.client.RegistryClient;
import io.apicurio.registry.rest.client.RegistryClientFactory;
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.calcite.sql.type.SqlTypeFactoryImpl;
import org.apache.calcite.rel.type.RelDataTypeSystem;
import org.apache.calcite.rel.type.RelDataTypeFactory;

import org.apache.flink.table.api.*;
import static org.apache.flink.table.api.Expressions.*;

import java.util.Map;
import java.io.File;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.Serializable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/** AvroSqlApplicationModeJob sets up the data processing job. */
public class AvroSqlApplicationModeJob  implements Serializable {   //, KeyedDeserializationSchema<GenericRecord>

    private static final Logger logger = LoggerFactory.getLogger(AvroSqlApplicationModeJob.class);

    private SchemaConverter schemaConverter;

    public static void main(final String[] args) throws Exception {

        ParameterTool parameters = ParameterTool.fromArgs(args);

        EnvironmentSettings settings = EnvironmentSettings.newInstance().inStreamingMode().build();
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        // StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env, settings);

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

        //Extract groupId and clientId
        String groupId = parameters.get("groupId");
        String clientId = parameters.get("clientId");

        //Create source table / topic
        String userSourceTopic = parameters.get("source");
        //String userSourceSql = parameters.get("sourceSql");                                  To be modified with the schema registry
        //createTable(userSourceTopic, tableEnv, userSourceSql, groupId, clientId);            Will be created with the schema registry
        logger.info("First table!--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");

//        //Create another source table / topic
//        String userSourceTopic1 = parameters.get("source1");
//        String userSourceSql1 = parameters.get("sourceSql1");
//        createTable(userSourceTopic1, tableEnv, userSourceSql1, groupId, clientId);
//        logger.info("Second table!------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
//
//        //Create sink table / topic
//        String userSinkTopic = parameters.get("sink");
//        String userSinkSql = parameters.get("sinkSql");
//        createTable(userSinkTopic, tableEnv, userSinkSql, groupId, clientId);
//        logger.info("Sink table!--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
//
//        //Execute user SQL and put into the sink
//        String userSQL = parameters.get("sql").replaceAll("%27", "'");
//        tableEnv.executeSql(userSQL);
//        logger.info("Result executed!---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");


        //---------------------------------------------------------------------------------------------------------------------------- Work in progress new Avro features

        Properties props = new Properties();

        String registryUrl = parameters.get("sr-url");
        String bootstrapServer = parameters.get("kafka-bootstrap-servers");
        String tokenUrl = parameters.get("token-url");
        String apicurioClientId = parameters.get("apicurio-client-id");
        String clientSecret = parameters.get("client-secret");
        String scope = parameters.get("scope");

        //-------------------------------Configure Kafka settings from:         https://www.apicur.io/registry/docs/apicurio-registry/2.5.x/getting-started/assembly-using-kafka-client-serdes.html#registry-serdes-config-producer_registry

        props.putIfAbsent(SerdeConfig.REGISTRY_URL, registryUrl);
        props.putIfAbsent(SerdeConfig.AUTO_REGISTER_ARTIFACT, true);
        props.putIfAbsent(SerdeConfig.ARTIFACT_RESOLVER_STRATEGY, "io.apicurio.registry.serde.avro.strategy.RecordIdStrategy");
        props.putIfAbsent(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
        props.putIfAbsent(ConsumerConfig.GROUP_ID_CONFIG, "Consumer-" + userSourceTopic);
        props.putIfAbsent(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        props.putIfAbsent(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
        props.putIfAbsent(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        //-------------------------------------------------------------------------------------------

        // Configure autentication from:          https://github.com/trifork/cheetah-lib-processing/blob/main/src/main/java/com/trifork/cheetah/processing/connector/kafka/serde/AvroSerdeSchemas.java
        // To be implented from:              https://github.com/trifork/cheetah-lib-processing/blob/main/src/main/java/com/trifork/cheetah/processing/auth/OAuthConfig.java

        props.putIfAbsent(AbstractKafkaSchemaSerDeConfig.BEARER_AUTH_CREDENTIALS_SOURCE, "OAUTHBEARER");
        props.putIfAbsent(AbstractKafkaSchemaSerDeConfig.BEARER_AUTH_ISSUER_ENDPOINT_URL, tokenUrl);
        props.putIfAbsent(AbstractKafkaSchemaSerDeConfig.BEARER_AUTH_CLIENT_ID, apicurioClientId);
        props.putIfAbsent(AbstractKafkaSchemaSerDeConfig.BEARER_AUTH_CLIENT_SECRET, clientSecret);
        props.putIfAbsent(AbstractKafkaSchemaSerDeConfig.BEARER_AUTH_SCOPE, scope);  //"schema-registry" or "kafka"???????
        props.putIfAbsent(AbstractKafkaSchemaSerDeConfig.BEARER_AUTH_LOGICAL_CLUSTER, ".");
        props.putIfAbsent(AbstractKafkaSchemaSerDeConfig.BEARER_AUTH_IDENTITY_POOL_ID, ".");
        //-------------------------------------------------------------------------------------------------


        // Configure deserializer settings from:             https://www.apicur.io/registry/docs/apicurio-registry/2.5.x/getting-started/assembly-using-kafka-client-serdes.html#registry-serdes-config-producer_registry
        props.putIfAbsent(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class.getName());
        //------------------------------------------------------------------------------------------------------

        logger.info("Setup parameter and config done!--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");

        //-----------------------------------------Convert kafka connection properties to a Map from:          https://ibm.github.io/event-automation/es/schemas/setting-java-apps-apicurio-serdes/
        try {
            // Example GET request
            String getRequestUrl = "http://localhost:8081/apis/ccompat/v7/schemas/ids/1";
            String getResponse = sendGetRequest(getRequestUrl);
            System.out.println("GET Response:");
            System.out.println(getResponse);
            Schema schema = new Schema.Parser().parse(getResponse);

//            // Example POST request
//            String postRequestUrl = "https://jsonplaceholder.typicode.com/posts";
//            String postData = "{\"title\": \"foo\",\"body\": \"bar\",\"userId\": 1}";
//            String postResponse = sendPostRequest(postRequestUrl, postData);
//            System.out.println("POST Response:");
//            System.out.println(postResponse);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Map<String, Object> config = (Map) props;
        // Create the client
        // RegistryClient client = RegistryClientFactory.create(registryUrl, config);


        // Get the schema from apicurio and convert to an avro schema
        // Schema schema = new Schema.Parser().parse(client.getLatestArtifact(null, "jsonToAvroOutputTopic-value"));   //artifactId
        //---------------------------------------------------------------------
        // To be implented refer to a class from:              https://github.com/milinda/avro-to-calcite/blob/master/src/main/java/org/pathirage/a2c/AvroSchemaConverter.java

//        RelDataTypeFactory relDataTypeFactory = new RelDataTypeFactory()  //this is an interface!
//        SchemaConverter schemaConverter = new SchemaConverter(relDataTypeFactory, schema);

//        Object sqlSchema = schemaConverter.convert();

//        createAvroTable(userSourceTopic, tableEnv, sqlSchema, groupId, clientId);
        logger.info("First table!--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
    }

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

    static public void createAvroTable(String topicName, StreamTableEnvironment tableEnv, Object tableSql, String groupId, String clientId) {
        //Create input topic / table
        //If topic already exists - table data is based upon that and
        //any data inserted is inserted into topic aswell.
        //If topic doesnt exist - new topic is created.
        String tableSQL = "CREATE TABLE IF NOT EXISTS " + topicName + " (" +
                tableSql +
                ") WITH (" +
                "'connector'='kafka'," +
                "'topic'='" + topicName + "'," +
                "'connector' = 'upsert-kafka'," +
                "'properties.bootstrap.servers' = 'kafka:19092'," +
                "'properties.group.id' = '" + groupId + "'," +
                "'format' = 'avro-confluent'," +
                "'avro-confluent.url' = 'http://localhost:8082'," +
                "'fields-include' = 'EXCEPT_KEY'," +
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

    static public String sendGetRequest(String url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("GET");
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();
        return response.toString();
    }

    static public String sendPostRequest(String url, String data) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);
        OutputStream os = connection.getOutputStream();
        os.write(data.getBytes());
        os.flush();
        os.close();
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();
        return response.toString();
    }
}

