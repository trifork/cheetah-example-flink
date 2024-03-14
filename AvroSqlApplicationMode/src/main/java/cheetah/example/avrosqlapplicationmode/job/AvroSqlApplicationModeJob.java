package cheetah.example.avrosqlapplicationmode.job;

import org.apache.flink.api.common.ExecutionConfig;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.*;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import io.strimzi.kafka.oauth.client.ClientConfig;
import io.strimzi.kafka.oauth.client.JaasClientOauthLoginCallbackHandler;
import io.strimzi.kafka.oauth.common.Config;
import org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule;

import io.apicurio.registry.serde.SerdeConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import org.apache.avro.Schema;

import java.util.*;

import java.lang.reflect.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.Serializable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

/** AvroSqlApplicationModeJob sets up the data processing job. */
public class AvroSqlApplicationModeJob implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(AvroSqlApplicationModeJob.class);

    public static void main(final String[] args) throws Exception {

        ParameterTool parameters = ParameterTool.fromArgs(args);

        //Extract groupId, clientId and source for config
        String groupId = parameters.get("groupId");
        String userSourceTopic = parameters.get("source");
        String userSinkTopic = parameters.get("sink");
        String userSQL = parameters.get("sql").replaceAll("%27", "'");

        String registryUrl = parameters.get("sr-url");
//        String bootstrapServer = parameters.get("kafka-bootstrap-servers");
//        String tokenUrl = parameters.get("token-url");
//        String apicurioClientId = parameters.get("apicurio-client-id");
//        String clientSecret = parameters.get("client-secret");
//        String scope = parameters.get("scope");

        //EnvironmentSettings settings = EnvironmentSettings.newInstance().inStreamingMode().build();

        EnvironmentSettings envSettings = EnvironmentSettings.newInstance()
                .inStreamingMode()
                .build();
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
//        TableEnvironment tEnv = TableEnvironment.create(envSettings);



//        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
//        ExecutionConfig executionConfig = env.getConfig();
//        executionConfig.enableForceAvro();

        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);

//        Properties props = new Properties();
//
//        props.putIfAbsent(SerdeConfig.REGISTRY_URL, registryUrl);
//        props.putIfAbsent(SerdeConfig.AUTO_REGISTER_ARTIFACT, true);
//        props.putIfAbsent(SerdeConfig.ARTIFACT_RESOLVER_STRATEGY, "io.apicurio.registry.serde.avro.strategy.RecordIdStrategy");
//        props.putIfAbsent(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
//        props.putIfAbsent(ConsumerConfig.GROUP_ID_CONFIG, "Consumer-" + userSourceTopic);
//        props.putIfAbsent(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
//        props.putIfAbsent(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
//        props.putIfAbsent(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
//        props.putIfAbsent(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class.getName());
//        props.putIfAbsent(AbstractKafkaSchemaSerDeConfig.BEARER_AUTH_CREDENTIALS_SOURCE, "OAUTHBEARER");
//        props.putIfAbsent(AbstractKafkaSchemaSerDeConfig.BEARER_AUTH_ISSUER_ENDPOINT_URL, tokenUrl);
//        props.putIfAbsent(AbstractKafkaSchemaSerDeConfig.BEARER_AUTH_CLIENT_ID, apicurioClientId);
//        props.putIfAbsent(AbstractKafkaSchemaSerDeConfig.BEARER_AUTH_CLIENT_SECRET, clientSecret);
//        props.putIfAbsent(AbstractKafkaSchemaSerDeConfig.BEARER_AUTH_SCOPE, scope);
//        props.putIfAbsent(AbstractKafkaSchemaSerDeConfig.BEARER_AUTH_LOGICAL_CLUSTER, ".");
//        props.putIfAbsent(AbstractKafkaSchemaSerDeConfig.BEARER_AUTH_IDENTITY_POOL_ID, ".");
        logger.info("Setup parameter and config done!--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");

        // GET the subjects and take the last as reference
        String getRequestUrl = registryUrl + "/subjects";
        String stringSubjects = sendGetRequest(getRequestUrl);
        String [] jsonSubjects = getStringArray (stringSubjects);
        String subject = jsonSubjects [jsonSubjects.length - 1];
        System.out.println("Subject: ");
        System.out.println(subject);

        // GET the last version
        getRequestUrl = registryUrl + "/subjects/" + subject + "/versions";
        String versions = sendGetRequest(getRequestUrl);
        String [] jsonVersions = getStringArray (versions);
        String version = jsonVersions [jsonVersions.length - 1];
        System.out.println("Version: ");
        System.out.println(version);

        // GET the raw registry
        getRequestUrl = registryUrl + "/schemas/ids/" + version;
        String stringSchema = sendGetRequest(getRequestUrl);

        // Transform the registry into JSON
        JsonNode schema = getJsonNode (stringSchema);

        // Formatted schema achieved
        String tableMetadata = jsonSchemaToSql(schema);
        logger.info("Metadata for the table done!--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        logger.info(tableMetadata);

        //Create source table / topic with normal table environment
        createAvroTable(userSourceTopic, tableEnv, tableMetadata, groupId);
        logger.info("Source table!--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");

        // Create sink table / topic
        createAvroTable(userSinkTopic, tableEnv, tableMetadata, groupId);
        logger.info("Sink table!--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");


        // Execute the query
        tableEnv.executeSql(userSQL);
        logger.info("Result executed!---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");

    }

    static public void createAvroTable(String topicName, StreamTableEnvironment tableEnv, String tableSql, String groupId) {
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
                "'format' = 'avro-confluent'," +
//                "'avro.codec' = 'null'," +
                "'avro-confluent.url' = '" + "http://schema-registry:8080/apis/ccompat/v7" +"'," +
                "'avro-confluent.subject' = '" + topicName + "-value'," +
                "'avro-confluent.properties.bearer.auth.credentials.source'='OAUTHBEARER'," +
                "'avro-confluent.properties.bearer.auth.issuer.endpoint.url'='http://keycloak:1852/realms/local-development/protocol/openid-connect/token'," +
                "'avro-confluent.properties.bearer.auth.client.id'='default-access'," +
                "'avro-confluent.properties.bearer.auth.client.secret'='default-access-secret'," +
                "'avro-confluent.properties.bearer.auth.scope'='schema-registry'," +
                "'avro-confluent.properties.bearer.auth.logical.cluster'='.'," +
                "'avro-confluent.properties.bearer.auth.identity.pool.id'='.'," +
//                "'key.format' = 'avro'," +
                "'value.format' = 'json'," +
//                "'key.fields' = 'timestamp'," +
//                "'key.fields-prefix' = 'times'," +
                "'value.fields-include' = 'ALL'," +
                //"'avro-confluent.schema' = '" + schema + "'," +
                "'scan.startup.mode' = 'earliest-offset'," +
                //"'scan.bounded.mode' = 'latest-offset', " +
                "'properties.sasl.mechanism' = '" + OAuthBearerLoginModule.OAUTHBEARER_MECHANISM + "', " +
                "'properties.security.protocol' = 'SASL_PLAINTEXT', " +
                "'properties.sasl.login.callback.handler.class' = '" + JaasClientOauthLoginCallbackHandler.class.getName() + "', " +
                "'properties.sasl.jaas.config' = '" + OAuthBearerLoginModule.class.getName() + " required "
                + Config.OAUTH_CLIENT_ID + "= " + "default-access" + " "
                + Config.OAUTH_CLIENT_SECRET + "=\"default-access-secret\" "
                + Config.OAUTH_SCOPE + "=\"kafka\" "
                + ClientConfig.OAUTH_TOKEN_ENDPOINT_URI + "=\"http://keycloak:1852/realms/local-development/protocol/openid-connect/token\";'" +
                ")";

        tableEnv.executeSql(tableSQL).print();
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

    static public JsonNode getJsonNode(String string) throws IOException {

        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(string, JsonNode.class);
    }

    static public String [] getStringArray(String string) throws IOException {

        ObjectMapper objectMapper = new ObjectMapper();
        return  objectMapper.readValue(string, String[].class);
    }

    static public String jsonSchemaToSql (JsonNode schema) {

        String sql = "";

        Schema temp = new Schema.Parser().parse(schema.get("schema").asText());
        List<Schema.Field> listSchema =  temp.getFields();
        int numberOfElements = listSchema.size();

        List<String> ddlTypes = getDdlTypes();

        int i = 0;

        for (Schema.Field listField : listSchema) {

            String value = String.valueOf(listField.name());
            String field = rawFieldToSql(String.valueOf(listField.schema().getType().getName()));

            // Surround with backtick if the name of the field is the same of one of ddl sql instruction
            if (ddlTypes.contains(value.toLowerCase())){
                value = "`" + value + "`";
            }

            // Manage the end the start and the other statements of a sql query
            if (i == numberOfElements - 1) {
                sql = sql + " " + value + " " + field;
            } else if (i == 0) {
                sql = sql + value + " " + field + ",";
            } else {
                sql = sql + " " + value + " " + field + ",";
            }

            i++;
        }
        return sql;
    }

    static private String rawFieldToSql (String type) {

        String sql = "";

        if (Objects.equals(type, "string")) {
            sql = "VARCHAR";
        } else if (Objects.equals(type, "int")) {
            sql = "INTEGER";
        } else if (Objects.equals(type, "boolean")) {
            sql = "BOOLEAN";
        } else if (Objects.equals(type, "bytes")) {
            sql = "BINARY";
        } else if (Objects.equals(type, "long")) {
            sql = "BIGINT";
        } else if (Objects.equals(type, "double")) {
            sql = "DOUBLE";
        } else if (Objects.equals(type, "float")) {
            sql = "FLOAT";
        }

        return sql;
    }

    static private List<String> getDdlTypes () {

        List<String> dataTypeList = new ArrayList<>();
        // Get all methods from the DataTypes class
        Method[] methods = DataTypes.class.getDeclaredMethods();

        for (Method method : methods) {
            // Ensure the method is public and static
            if (java.lang.reflect.Modifier.isPublic(method.getModifiers()) && java.lang.reflect.Modifier.isStatic(method.getModifiers())) {

                    String dataTypeMethodNames = getLowerCaseMethodNamesOrEmpty(method);
                    // Ensure that ddls are unique and valid
                    if (!dataTypeList.contains(dataTypeMethodNames) && !Objects.equals(dataTypeMethodNames, "")) {
                        dataTypeList.add("value");
                        dataTypeList.add(dataTypeMethodNames.toLowerCase());
                    }
            }
        }

        return dataTypeList;
    }

    private static String getLowerCaseMethodNamesOrEmpty(Method method) {
        String cleanedMethodName = "";

        // Check if method name is written entirely in uppercase letters as ddl instruction should be
        if (method.getName().equals(method.getName().toUpperCase())) {
            cleanedMethodName = cleanMethodName(method.getName().toLowerCase());
        }

        return cleanedMethodName;
    }

    private static String cleanMethodName(String methodName) {
        // Replace all symbols other than underscores with empty string
        return methodName.replaceAll("[^a-zA-Z0-9_]", "");
    }

}

