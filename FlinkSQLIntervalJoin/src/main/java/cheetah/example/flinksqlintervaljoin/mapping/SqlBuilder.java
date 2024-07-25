package cheetah.example.flinksqlintervaljoin.mapping;

import org.apache.flink.table.api.DataTypes;
import org.apache.flink.table.api.Schema;

import java.util.Map;

public class SqlBuilder {

    private static final String WATERMARK_COLUMN_NAME = "ts";
    private static final int WATER_TIMESTAMP_PRECISION = 3;
    private static final String AND = " AND ";

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

    public String concatenateWithAnd(String... clauses) {
        var stringBuilder = new StringBuilder();
        for (String clause: clauses) {
            stringBuilder.append(clause);
            stringBuilder.append(AND);
        }

        var indexOfLastAnd = stringBuilder.length() - AND.length();
        stringBuilder.delete(indexOfLastAnd, stringBuilder.length());   // remove last " AND "
        return stringBuilder.toString();
    }

    public String buildFromClauseFromSources(String... sources) {
        var stringBuilder = new StringBuilder();
        for (String source: sources) {
            stringBuilder.append(escapeString(source));
            stringBuilder.append(',');
        }

        stringBuilder.deleteCharAt(stringBuilder.length() - 1);   // remove last ','
        return stringBuilder.toString();
    }

    public String buildNotExistsClause(String source, String whereClause) {
        return "NOT EXISTS (SELECT 1 FROM " + escapeString(source)
                + " WHERE " + whereClause
                + ")";
    }

    public String buildJoinKeyNullClause(String source, String keyColumn) {
        return escapeString(source) + "." +  escapeString(keyColumn) + " is NULL";
    }

    public String buildInsertIntoStatement(String sink, String selectClause, String fromClause, String whereClause) {
        return "INSERT INTO " + escapeString(sink) + " SELECT " + selectClause + " FROM "
                + fromClause + " WHERE " + whereClause;
    }

    public static String escapeString(String string) {
        return '`' + string + '`';
    }


}
