package cheetah.example.flinksql_interval_join.mapping;

import org.apache.flink.table.api.DataTypes;
import org.apache.flink.table.api.Schema;

import java.util.Map;

public class SqlSchemaBuilder {

    private static final String WATERMARK_COLUMN_NAME = "ts";
    private static final int WATER_TIMESTAMP_PRECISION = 3;

    public Schema transformTypeMappingToSchemaWithKafkaTimestamp(Map<String, String> namesAndTypes) {
        var schema = transformTypeMappingToSchema(namesAndTypes);
        return Schema.newBuilder().fromSchema(schema).columnByMetadata(WATERMARK_COLUMN_NAME, DataTypes.TIMESTAMP(WATER_TIMESTAMP_PRECISION), "timestamp").build(); // add watermark column
    }

    public Schema transformTypeMappingToSchema(Map<String, String> namesAndTypes) {
        var builder = Schema.newBuilder();
        namesAndTypes.forEach(builder::column);
        return builder.build();
    }

    public String buildSelectStringForMappingsWithLeftTablePrecedence(Map<String, String> namesAndTypesLeft, String leftTableName,
                                                                      Map<String, String> namesAndTypesRight, String rightTableName) {
        var resultStringBuilder = new StringBuilder();
        resultStringBuilder.append(buildSelectStringForMapping(namesAndTypesLeft, leftTableName));
        resultStringBuilder.append(',');

        var escapedRightTableNameWithDot = escapeString(rightTableName) + '.';

        namesAndTypesRight.forEach((propertyName, propertyTypeString) -> {
            if (!namesAndTypesLeft.containsKey(propertyName)) { // Do not add columns that already exist in left source
                resultStringBuilder.append(escapedRightTableNameWithDot);
                resultStringBuilder.append(escapeString(propertyName));
                resultStringBuilder.append(',');
            }
        });

        resultStringBuilder.deleteCharAt(resultStringBuilder.length() - 1); //remove last comma ','
        return resultStringBuilder.toString();
    }

    public String buildSelectStringForMapping(Map<String, String> namesAndTypes, String tableName) {
        var resultStringBuilder = new StringBuilder();
        var escapedTableNameWithDot = escapeString(tableName) + '.';

        namesAndTypes.forEach((propertyName, propertyTypeString) -> {
            resultStringBuilder.append(escapedTableNameWithDot);
            resultStringBuilder.append(escapeString(propertyName));
            resultStringBuilder.append(',');
        });

        resultStringBuilder.deleteCharAt(resultStringBuilder.length() - 1); //remove last comma ','
        return resultStringBuilder.toString();
    }

    public String buildJoinString(String leftSourceTable, String leftJoinKeyName, String rightSourceTable, String rightJoinKeyName) {
        return escapeString(leftSourceTable) + " INNER JOIN " + escapeString(rightSourceTable)
                + " ON " + buildEqualityString(leftSourceTable, leftJoinKeyName, rightSourceTable, rightJoinKeyName);
    }

    public String buildLeftJoinString(String leftSourceTable, String leftJoinKeyName, String rightSourceTable, String rightJoinKeyName) {
        return escapeString(leftSourceTable) + " LEFT OUTER JOIN " + escapeString(rightSourceTable)
                + " ON " + buildEqualityString(leftSourceTable, leftJoinKeyName, rightSourceTable, rightJoinKeyName);
    }


    public String buildEqualityString(String leftSourceTable, String leftJoinKeyName, String rightSourceTable, String rightJoinKeyName) {
        return escapeString(leftSourceTable) + '.' + escapeString(leftJoinKeyName)
                + '=' + escapeString(rightSourceTable) + '.' + escapeString(rightJoinKeyName);
    }

    public String buildIntervalClause(String leftSourceTable, String rightSourceTable, int timeoutTimeSeconds) {
        var leftWatermarkColumn = escapeString(leftSourceTable) + '.' + WATERMARK_COLUMN_NAME;
        var rightWatermarkColumn = escapeString(rightSourceTable) + '.' + WATERMARK_COLUMN_NAME;

        String quotedTime = "'" + timeoutTimeSeconds + "'";
        int numDigitsForTime = String.valueOf(timeoutTimeSeconds).length();
        var interval = "INTERVAL " + quotedTime + " SECOND(" + numDigitsForTime + ")";

        return leftWatermarkColumn + " BETWEEN "
                + rightWatermarkColumn + " - " + interval + " AND "
                + rightWatermarkColumn + " + " + interval;
    }

    private String escapeString(String string) {
        return '`' + string + '`';
    }
}
