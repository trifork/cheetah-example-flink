package cheetah.example.avrosqlapplicationmode.job;

import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.*;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import io.strimzi.kafka.oauth.client.ClientConfig;
import io.strimzi.kafka.oauth.client.JaasClientOauthLoginCallbackHandler;
import io.strimzi.kafka.oauth.common.Config;
import org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule;
import org.apache.avro.Schema;

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
import java.util.*;

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
        String bootstrapServer = parameters.get("kafka-bootstrap-servers");
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);
        logger.info("Setup parameter and config done!------------------------------------------------------------------------------------------------------------------------------");

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
        logger.info("Metadata for the table done!------------------------------------------------------------------------------------------------------------------------------");
        logger.info(tableMetadata);

        //Create source table / topic with stream table environment
        createAvroSource(userSourceTopic, tableEnv, tableMetadata, groupId, bootstrapServer, registryUrl);
        logger.info("Source table!------------------------------------------------------------------------------------------------------------------------------");

        // Create sink table / topic with stream table environment
        createSink(userSinkTopic, tableEnv, tableMetadata, groupId, bootstrapServer, registryUrl);
        logger.info("Sink table!------------------------------------------------------------------------------------------------------------------------------");

        tableEnv.executeSql(userSQL);
        logger.info("Result executed!------------------------------------------------------------------------------------------------------------------------------");
    }

    static public void createAvroSource(String topicName, StreamTableEnvironment tableEnv, String tableMetadata, String groupId, String bootstrapServer, String registerUrl) {
        //Create input topic / table
        //If topic already exists - table data is based upon that and any data inserted is inserted into topic aswell.

        String tableSQL = "CREATE TABLE IF NOT EXISTS " + topicName + " (" +
                tableMetadata +
                ") WITH (" +
                "'connector'='kafka'," +
                "'topic'='" + topicName + "'," +
                "'properties.bootstrap.servers' = '" + bootstrapServer + "'," +
                "'properties.group.id' = '" + groupId + "'," +
                // 'avro' and 'avro-confluent' are not compatible within deserialization
                "'format' = 'avro-confluent'," +
                "'avro-confluent.url' = '" + registerUrl + "'," +
                "'avro-confluent.subject' = '" + topicName + "-value'," +
                "'avro-confluent.properties.bearer.auth.credentials.source'='OAUTHBEARER'," +
                "'avro-confluent.properties.bearer.auth.issuer.endpoint.url'='http://keycloak:1852/realms/local-development/protocol/openid-connect/token'," +
                "'avro-confluent.properties.bearer.auth.client.id'='default-access'," +
                "'avro-confluent.properties.bearer.auth.client.secret'='default-access-secret'," +
                "'avro-confluent.properties.bearer.auth.scope'='schema-registry'," +
                "'avro-confluent.properties.bearer.auth.logical.cluster'='.'," +
                "'avro-confluent.properties.bearer.auth.identity.pool.id'='.'," +
                "'scan.startup.mode' = 'earliest-offset'," +
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

    static public void createSink(String topicName, StreamTableEnvironment tableEnv, String tableMetadata, String groupId, String bootstrapServer, String registerUrl) {
        //Create sink topic / table
        //If topic already exists - table data is based upon that and any data inserted is inserted into topic aswell.
        //The format is "json" for have the same formatting as input
        //If the format is setted to "avro" work not perfectly verifyable (the message will be a binary)
        //If the format is setted to "avro-confluent" works either but adding information of the type for each field in the message -> Value

        String tableSQL = "CREATE TABLE IF NOT EXISTS " + topicName + " (" +
                tableMetadata +
                ") WITH (" +
                "'connector'='kafka'," +
                "'topic'='" + topicName + "'," +
                "'properties.bootstrap.servers' = '" + bootstrapServer + "'," +
                "'properties.group.id' = '" + groupId + "'," +
                //change 'format' and 'value.format' to 'json' if required
                //change 'format' and 'value.format' to 'avro-confluent' and uncomment the 'avro-confluent.url' and related fields if required
//                "'avro-confluent.url' = '" + registerUrl + "'," +
//                "'avro-confluent.subject' = '" + topicName + "-value'," +
//                "'avro-confluent.properties.bearer.auth.credentials.source'='OAUTHBEARER'," +
//                "'avro-confluent.properties.bearer.auth.issuer.endpoint.url'='http://keycloak:1852/realms/local-development/protocol/openid-connect/token'," +
//                "'avro-confluent.properties.bearer.auth.client.id'='default-access'," +
//                "'avro-confluent.properties.bearer.auth.client.secret'='default-access-secret'," +
//                "'avro-confluent.properties.bearer.auth.scope'='schema-registry'," +
//                "'avro-confluent.properties.bearer.auth.logical.cluster'='.'," +
//                "'avro-confluent.properties.bearer.auth.identity.pool.id'='.'," +
                "'format' = 'avro'," +
                "'value.format' = 'avro'," +
                "'sink.partitioner' = 'fixed'," +
                "'sink.delivery-guarantee' = 'none'," +
                "'scan.startup.mode' = 'earliest-offset'," +
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

        // Get the list of ddl instruction that has to be confronted with each field of the metadata
        List<String> ddlTypes = getDdlTypes();

        int i = 0;

        for (Schema.Field listField : listSchema) {

            String value = String.valueOf(listField.name());
            String field = rawFieldToSql(String.valueOf(listField.schema().getType().getName()));

            // Surround with backtick if the name of the field is the same of one of ddl sql instruction to declare it as field name
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

