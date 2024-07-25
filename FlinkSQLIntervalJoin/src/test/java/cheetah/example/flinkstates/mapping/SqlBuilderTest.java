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
}
