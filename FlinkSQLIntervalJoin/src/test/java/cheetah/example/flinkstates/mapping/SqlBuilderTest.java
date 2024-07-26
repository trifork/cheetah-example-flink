package cheetah.example.flinkstates.mapping;

import cheetah.example.flinksqlintervaljoin.mapping.SqlBuilder;
import org.apache.flink.table.api.Schema;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;

public class SqlBuilderTest {

    private SqlBuilder sqlBuilder;
    @BeforeEach
    public void init() {
        sqlBuilder = new SqlBuilder();
    }
    @Test
    public void testTransformTypeMappingToSchemaWithKafkaTimestamp() {
        var typeMapping = new LinkedHashMap<String, String>();
        typeMapping.put("stringColumn", "VARCHAR");
        typeMapping.put("decimalColumn", "DECIMAL");

        // Act
        var schema = sqlBuilder.transformTypeMappingToSchemaWithKafkaTimestamp(typeMapping);

        // Assert
        var columns = schema.getColumns();

        Assertions.assertEquals("stringColumn", columns.get(0).getName());
        Assertions.assertEquals("[VARCHAR]", ((Schema.UnresolvedPhysicalColumn) columns.get(0)).getDataType().toString());

        Assertions.assertEquals("decimalColumn", columns.get(1).getName());
        Assertions.assertEquals("[DECIMAL]", ((Schema.UnresolvedPhysicalColumn) columns.get(1)).getDataType().toString());

        Assertions.assertEquals("ts", columns.get(2).getName());
        Assertions.assertEquals("TIMESTAMP(3)", ((Schema.UnresolvedMetadataColumn) columns.get(2)).getDataType().toString());
        Assertions.assertEquals("timestamp", ((Schema.UnresolvedMetadataColumn) columns.get(2)).getMetadataKey());
    }

    @Test
    public void testBuildSelectStringForMapping() {
        var typeMapping = new LinkedHashMap<String, String>();
        typeMapping.put("stringColumn", "VARCHAR");
        typeMapping.put("decimalColumn", "DECIMAL");
        typeMapping.put("timeColumn", "TIME");

        // Act
        var selectString = sqlBuilder.buildSelectStringForMapping(typeMapping, "nameOfTable");

        // Assert
        var expectedString = "`nameOfTable`.`stringColumn`,`nameOfTable`.`decimalColumn`,`nameOfTable`.`timeColumn`";
        Assertions.assertEquals(expectedString, selectString);
    }

    @Test
    public void testBuildSelectStringForMappingsWithLeftTablePrecedence() {
        var leftTypeMapping = new LinkedHashMap<String, String>();
        leftTypeMapping.put("leftStringColumn", "VARCHAR");
        leftTypeMapping.put("inBothColumn", "VARCHAR");

        var rightTypeMapping = new LinkedHashMap<String, String>();
        rightTypeMapping.put("rightStringColumn", "VARCHAR");
        rightTypeMapping.put("inBothColumn", "VARCHAR");

        // Act
        var selectString = sqlBuilder.buildSelectStringForMappingsWithLeftTablePrecedence(leftTypeMapping, "leftTable", rightTypeMapping, "rightTable");

        // Assert
        var expectedString = "`leftTable`.`leftStringColumn`,`leftTable`.`inBothColumn`,`rightTable`.`rightStringColumn`";
        Assertions.assertEquals(expectedString, selectString);
    }

    @Test
    public void testBuildLeftJoinString() {
        var leftSourceTable = "leftSource";
        var leftJoinKey = "leftKeyColumn";
        var rightSourceTable = "rightSource";
        var rightJoinKey = "rightKeyColumn";

        // Act
        var joinString = sqlBuilder.buildLeftJoinString(leftSourceTable, leftJoinKey, rightSourceTable, rightJoinKey);

        // Assert
        var expectedString = "`leftSource` LEFT OUTER JOIN `rightSource` ON `leftSource`.`leftKeyColumn`=`rightSource`.`rightKeyColumn`";
        Assertions.assertEquals(expectedString, joinString);
    }

    @Test
    public void testBuildEqualityString() {
        var leftSourceTable = "leftSource";
        var leftJoinKey = "leftKeyColumn";
        var rightSourceTable = "rightSource";
        var rightJoinKey = "rightKeyColumn";

        // Act
        var equalityString = sqlBuilder.buildEqualityString(leftSourceTable, leftJoinKey, rightSourceTable, rightJoinKey);


        // Assert
        var expectedString = "`leftSource`.`leftKeyColumn`=`rightSource`.`rightKeyColumn`";
        Assertions.assertEquals(expectedString, equalityString);
    }

    @Test
    public void testBuildIntervalString() {
        var leftSourceTable = "leftSource";
        var rightSourceTable = "rightSource";
        int timeoutTimeSeconds = 6000;

        // Act
        var intervalString = sqlBuilder.buildIntervalClause(leftSourceTable, rightSourceTable, timeoutTimeSeconds);


        // Assert
        var expectedString = "`leftSource`.ts BETWEEN `rightSource`.ts - INTERVAL '6000' SECOND(4) AND `rightSource`.ts + INTERVAL '6000' SECOND(4)";
        Assertions.assertEquals(expectedString, intervalString);
    }

    @Test
    public void testConcatenateWithAndOneValue() {
        var andString = sqlBuilder.concatenateWithAnd("some clause");
        Assertions.assertEquals("some clause", andString);
    }

    @Test
    public void testConcatenateWithAndMultipleValues() {
        var andString = sqlBuilder.concatenateWithAnd("some clause", "another clause", "three");
        Assertions.assertEquals("some clause AND another clause AND three", andString);
    }


    @Test
    public void testBuildFromClauseFromSourcesOneSource() {
        var fromString = sqlBuilder.buildFromClauseFromSources("one source");
        Assertions.assertEquals("`one source`", fromString);
    }

    @Test
    public void testBuildFromClauseFromSourcesMultipleSources() {
        var fromString = sqlBuilder.buildFromClauseFromSources("one source", "second", "thi.rd");
        Assertions.assertEquals("`one source`,`second`,`thi.rd`", fromString);
    }

    @Test
    public void testBuildNotExistsClause() {
        var sourceTable = "sourceTable";
        var whereClause = "someColumn = aValue";

        var notExistsClause = sqlBuilder.buildNotExistsClause(sourceTable, whereClause);
        var expectedString = "NOT EXISTS (SELECT 1 FROM `sourceTable` WHERE someColumn = aValue)";
        Assertions.assertEquals(expectedString, notExistsClause);
    }

    @Test
    public void testBuildJoinKeyNullClause() {
        var sourceTable = "sourceTable";
        var keyColumn = "key";

        var clause = sqlBuilder.buildJoinKeyNullClause(sourceTable, keyColumn);

        var expectedString = "`sourceTable`.`key` is NULL";
        Assertions.assertEquals(expectedString, clause);
    }

    @Test
    public void testBuildInsertIntoStatement() {
        var sink = "targetTable";
        var selectClause = "someSource.someColumn,x.y";
        var fromClause = "someSource,x";
        var whereClause = "someCondition";

        var insertIntoStatement = sqlBuilder.buildInsertIntoStatement(sink, selectClause, fromClause, whereClause);

        var expectedString = "INSERT INTO `targetTable` SELECT someSource.someColumn,x.y FROM someSource,x WHERE someCondition";
        Assertions.assertEquals(expectedString, insertIntoStatement);
    }

    @Test
    public void testEscapeString() {
        var string = "string";

        var escapedString = SqlBuilder.escapeString(string);

        Assertions.assertEquals("`string`", escapedString);
    }
}
