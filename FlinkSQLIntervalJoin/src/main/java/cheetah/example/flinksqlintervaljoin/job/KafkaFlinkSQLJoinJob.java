package cheetah.example.flinksqlintervaljoin.job;

import cheetah.example.flinksqlintervaljoin.mapping.EdmToSqlTypesMapper;
import cheetah.example.flinksqlintervaljoin.mapping.SqlBuilder;

import cheetah.example.flinksqlintervaljoin.util.ODataUtil;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.*;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import io.strimzi.kafka.oauth.client.ClientConfig;
import io.strimzi.kafka.oauth.client.JaasClientOauthLoginCallbackHandler;
import io.strimzi.kafka.oauth.common.Config;
import org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * SqlApplicationModeJob sets up the data processing job.
 */
final public class KafkaFlinkSQLJoinJob implements Serializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaFlinkSQLJoinJob.class);
    private final ParameterTool parameters;
    private final StreamTableEnvironment tableEnv;
    private final SqlBuilder sqlBuilder;
    private final EdmToSqlTypesMapper mapper;

    // Using LinkedHashMaps to ensure column ordering (consider using apache LinkedMap?)
    private LinkedHashMap<String, String> leftTypeMapping;
    private LinkedHashMap<String, String> rightTypeMapping;
    private LinkedHashMap<String, String> jointTypeMapping;

    public KafkaFlinkSQLJoinJob(ParameterTool parameters, StreamTableEnvironment tableEnv, SqlBuilder sqlBuilder, EdmToSqlTypesMapper mapper) {
        this.parameters = parameters;
        this.tableEnv = tableEnv;
        this.sqlBuilder = sqlBuilder;
        this.mapper = mapper;
    }

    public static void main(final String[] args) throws Exception {
        ParameterTool parameters = ParameterTool.fromArgs(args);
        EnvironmentSettings settings = EnvironmentSettings.newInstance().inStreamingMode().build();
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env, settings);

        KafkaFlinkSQLJoinJob job = new KafkaFlinkSQLJoinJob(parameters, tableEnv, new SqlBuilder(), new EdmToSqlTypesMapper());
        job.setup();
    }

    public void setup() throws Exception {
        String oDataUrl = System.getenv("ODATA_URL");
        String odata_username = System.getenv("ODATA_USERNAME");
        String odata_password = System.getenv("ODATA_PASSWORD");
        String odataEntitiyset1 = System.getenv("ODATA_ENTIYSET_NAME_1");
        String odataEntitiyset2 = System.getenv("ODATA_ENTIYSET_NAME_2");
        String odataMetadataIdentifier1 = System.getenv("ODATA_ENTIYSET_METADATA_IDENTIFIER_1");
        String odataMetadataIdentifier2 = System.getenv("ODATA_ENTIYSET_METADATA_IDENTIFIER_2");
        String odataApiKey = System.getenv("ODATA_API_KEY");
        int timeoutTimeSeconds = Integer.parseInt(System.getenv("STATE_TIMEOUT_SECONDS"));

        // Create type mappings
        createMappings(odataMetadataIdentifier1, odataMetadataIdentifier2, odataEntitiyset1, odataEntitiyset2, oDataUrl);

        // Create source and sink tables
        createInAndOutTablesFromInputODataSchemas();

        // Create and execute SQL statements
        executeJoins(timeoutTimeSeconds);
    }

    private void executeJoins(int timeoutTimeSeconds) {
        var leftSourceTable = parameters.get("leftSource");
        var rightSourceTable = parameters.get("rightSource");
        var joinKeyName = parameters.get("join-key");

        //Execute user SQL and put into the sink
        var selectString = sqlBuilder.buildSelectStringForMappingsWithLeftTablePrecedence(leftTypeMapping, leftSourceTable, rightTypeMapping, rightSourceTable);
        var equalityClause = sqlBuilder.buildEqualityString(leftSourceTable, joinKeyName, rightSourceTable, joinKeyName);
        var intervalClause = sqlBuilder.buildIntervalClause(leftSourceTable, rightSourceTable, timeoutTimeSeconds);
        var whereClause = sqlBuilder.concatenateWithAnd(equalityClause, intervalClause);
        var fromBothSourcesClause = sqlBuilder.buildFromClauseFromSources(leftSourceTable, rightSourceTable);

        String sqlJoinStatement = sqlBuilder.buildInsertIntoStatement(parameters.get("sink"), selectString, fromBothSourcesClause, whereClause);
        //"INSERT INTO " + escapeString(parameters.get("sink")) + " SELECT " + selectString + " FROM "
        //+ leftEscaped + ", " + rightEscaped + " WHERE " + equalityClause + " AND " + intervalClause;

        var selectLeftClause = sqlBuilder.buildSelectStringForMapping(leftTypeMapping, leftSourceTable);
        var notExistsClause = sqlBuilder.buildNotExistsClause(rightSourceTable, whereClause);
        var fromLeftClause = sqlBuilder.buildFromClauseFromSources(leftSourceTable);

        String leftDLQStatement = sqlBuilder.buildInsertIntoStatement(parameters.get("leftDLQ"), selectLeftClause, fromLeftClause, notExistsClause);
        //"INSERT INTO " + escapeString(parameters.get("leftDLQ"))
        //+ " SELECT " + selectLeftClause + " FROM " + leftEscaped
        //+ " WHERE NOT EXISTS (SELECT 1 FROM " + rightEscaped
        //+ " WHERE " + equalityClause + " AND " + intervalClause
        //+ ")";

        var selectRightClause = sqlBuilder.buildSelectStringForMapping(rightTypeMapping, rightSourceTable);
        var rightJoinClause = sqlBuilder.buildLeftJoinString(rightSourceTable, joinKeyName, leftSourceTable, joinKeyName);
        var fromRightJoinClause = sqlBuilder.concatenateWithAnd(rightJoinClause, intervalClause);
        var joinKeyColumnNullClause = sqlBuilder.buildJoinKeyNullClause(leftSourceTable, joinKeyName);

        String rightDLQStatement = sqlBuilder.buildInsertIntoStatement(parameters.get("rightDLQ"), selectRightClause, fromRightJoinClause, joinKeyColumnNullClause);
        //"INSERT INTO " + escapeString(parameters.get("rightDLQ")) + " SELECT " + selectRightClause + " FROM " + rightJoinClause
        //+ " AND " + intervalClause + " WHERE " + leftEscaped + "." +  escapeString(joinKeyName) + " is NULL";

        LOGGER.debug(sqlJoinStatement);
        LOGGER.debug(leftDLQStatement);
        LOGGER.debug(rightDLQStatement);

        StatementSet statementSet = tableEnv.createStatementSet();
        statementSet.addInsertSql(sqlJoinStatement);
        statementSet.addInsertSql(leftDLQStatement);
        statementSet.addInsertSql(rightDLQStatement);

        statementSet.execute(); // Executes all statements at the "same time", i.e. we don't need to start reading the kafka topic from the beginning each time
    }


    private void createMappings(String leftODataMetadataIdentifier, String rightODataMetadataIdentifier,
                                String leftODataEntitySetName, String rightODataEntitySetName, String oDataUrl) throws Exception {

        var leftEntityTypeNamespace = leftODataMetadataIdentifier.split("/")[0];
        var leftEntityTypeName = leftODataMetadataIdentifier.split("/")[1];
        var rightEntityTypeNamespace = rightODataMetadataIdentifier.split("/")[0];
        var rightEntityTypeName = rightODataMetadataIdentifier.split("/")[1];

        var leftEdmSchema = ODataUtil.readEdm(ODataUtil.getEdmMetadataBytes(oDataUrl, leftODataEntitySetName));
        var rightEdmSchema = ODataUtil.readEdm(ODataUtil.getEdmMetadataBytes(oDataUrl, rightODataEntitySetName));

        leftTypeMapping = mapper.mapEdmPropertiesToSQLTypeStrings(leftEdmSchema, leftEntityTypeNamespace, leftEntityTypeName);
        rightTypeMapping = mapper.mapEdmPropertiesToSQLTypeStrings(rightEdmSchema, rightEntityTypeNamespace, rightEntityTypeName);
        jointTypeMapping = createJointTypeMapping(leftTypeMapping, rightTypeMapping);
    }


    private void createInAndOutTablesFromInputODataSchemas() {

        var schemaStringLeftSource = sqlBuilder.transformTypeMappingToSchemaWithKafkaTimestamp(leftTypeMapping);
        var schemaStringRightSource = sqlBuilder.transformTypeMappingToSchemaWithKafkaTimestamp(rightTypeMapping);
        var outputSchemaString = sqlBuilder.transformTypeMappingToSchema(jointTypeMapping);
        var leftDLQSchemaString = sqlBuilder.transformTypeMappingToSchema(leftTypeMapping);
        var rightDLQSchemaString = sqlBuilder.transformTypeMappingToSchema(rightTypeMapping);

        Map<String, String> tableParams = new HashMap<>();
        tableParams.put("groupId", parameters.get("groupId"));
        tableParams.put("clientId", parameters.get("clientId"));

        // left input
        tableParams.put("userTopic", parameters.get("leftSource"));
        createTable(tableParams, schemaStringLeftSource, tableEnv, "json");

        // left DLQ
        tableParams.put("userTopic", parameters.get("leftDLQ"));
        createTable(tableParams, leftDLQSchemaString, tableEnv, "debezium-json"); // Join creates Update stream instead of Append stream -> CDC format required

        //Create another source table / topic
        tableParams.put("userTopic", parameters.get("rightSource"));
        createTable(tableParams, schemaStringRightSource, tableEnv, "json");

        // right DLQ
        tableParams.put("userTopic", parameters.get("rightDLQ"));
        createTable(tableParams, rightDLQSchemaString, tableEnv, "debezium-json");

        //Create sink table / topic
        tableParams.put("userTopic", parameters.get("sink"));
        createTable(tableParams, outputSchemaString, tableEnv, "json");

    }

    private void createTable(Map<String, String> tableParams, Schema tableSchema, StreamTableEnvironment tableEnv, String format) {
        //Create input topic / table
        //If topic already exists - table data is based upon that and
        //any data inserted is inserted into topic as well.
        //If topic doesnt exist - new topic is created.
        var tableName = SqlBuilder.escapeString(tableParams.get("userTopic"));
        var tableDescriptor = TableDescriptor.forConnector("kafka")
                .schema(tableSchema)
                .format(format)
                .option("topic", tableParams.get("userTopic"))
                .option("properties.bootstrap.servers", "kafka:19092")
                .option("properties.group.id", tableParams.get("groupId") + "-" + tableParams.get("userTopic"))
                .option("scan.startup.mode", "earliest-offset")
                .option("properties.sasl.mechanism", OAuthBearerLoginModule.OAUTHBEARER_MECHANISM)
                .option("properties.security.protocol", "SASL_PLAINTEXT")
                .option("properties.sasl.login.callback.handler.class", JaasClientOauthLoginCallbackHandler.class.getName())
                .option("properties.sasl.jaas.config", OAuthBearerLoginModule.class.getName() + " required "
                        + Config.OAUTH_CLIENT_ID + "= default-access "
                        + Config.OAUTH_CLIENT_SECRET + "=\"default-access-secret\" "
                        + Config.OAUTH_SCOPE + "=\"kafka\" "
                        + ClientConfig.OAUTH_TOKEN_ENDPOINT_URI + "=\"http://keycloak:1852/realms/local-development/protocol/openid-connect/token\";"
                )
                .build();
        tableEnv.createTable(tableName, tableDescriptor);
    }

    public LinkedHashMap<String, String> createJointTypeMapping(LinkedHashMap<String, String> leftTypeMapping, LinkedHashMap<String, String> rightTypeMapping) {
        var resultMap = new LinkedHashMap<>(leftTypeMapping);

        rightTypeMapping.forEach((key, value) -> {
            if (!leftTypeMapping.containsKey(key)) { // for equal key names, leftTypeMapping takes precedence
                resultMap.put(key, value);
            }
        });

        return resultMap;
    }

    private LinkedHashMap<String, String> createRightMappingWithoutKeysFromLeft(LinkedHashMap<String, String> leftTypeMapping, LinkedHashMap<String, String> rightTypeMapping) {
        var resultMap = new LinkedHashMap<String, String>();

        rightTypeMapping.forEach((key, value) -> {
            if (!leftTypeMapping.containsKey(key)) {
                resultMap.put(key, value);
            }
        });

        return resultMap;
    }
}
