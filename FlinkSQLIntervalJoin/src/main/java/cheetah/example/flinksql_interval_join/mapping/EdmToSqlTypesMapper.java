package cheetah.example.flinksql_interval_join.mapping;

import org.apache.flink.table.types.logical.LogicalTypeRoot;
import org.apache.olingo.odata2.api.edm.Edm;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.edm.EdmTyped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;

public class EdmToSqlTypesMapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(EdmToSqlTypesMapper.class);

    public LinkedHashMap<String, String> mapEdmPropertiesToSQLTypeStrings(Edm edm, String entityTypeNamespace, String entityTypeName) throws EdmException {
        var entityType = edm.getEntityType(entityTypeNamespace, entityTypeName);
        var resultMap = new LinkedHashMap<String, String>();

        for (var propertyName : entityType.getPropertyNames()) {
            var property = entityType.getProperty(propertyName);
            var propertyTypeString = getTypeStringForEdmProperty(property);
            resultMap.put(propertyName, propertyTypeString);
        }

        return resultMap;
    }

    // NOTE !
    // This method should be examined more closely before used in any production-environment.
    // One of the most important considerations are the types that take arguments, for example VARCHAR:
    // You would define the max length n of a String with VARCHAR(n), and since we are not doing that, it will assume some default max length.
    // Examine, if the default behaviors are okay!
    private String getTypeStringForEdmProperty(EdmTyped property) throws EdmException {
        var type = property.getType().getName();

        switch (type) {
            case "DateTime", "DateTimeOffset" -> {
                return LogicalTypeRoot.VARCHAR.name(); // Works and no special treatment needed unlike "TIMESTAMP";
            }
            case "Time", "Duration" -> {
                return "TIME";
            }
            case "Date" -> {
                return "DATE";
            }
            case "Decimal" -> {
                return LogicalTypeRoot.DECIMAL.name();
            }
            case "Double" -> {
                return LogicalTypeRoot.DOUBLE.name();
            }
            case "Single" -> {
                return LogicalTypeRoot.FLOAT.name();
            }
            case "Int16" -> {
                return LogicalTypeRoot.SMALLINT.name();
            }
            case "Int32" -> {
                return LogicalTypeRoot.INTEGER.name();
            }
            case "Int64" -> {
                return LogicalTypeRoot.BIGINT.name();
            }
            case "String" -> {
                return LogicalTypeRoot.VARCHAR.name();
            }
            case "Boolean" -> {
                return LogicalTypeRoot.BOOLEAN.name();
            }
            case "Guid" -> {
                return LogicalTypeRoot.VARCHAR.name();
            }
            case "Byte", "SByte", "Binary" -> {
                return LogicalTypeRoot.VARBINARY.name();
            }
            default -> {
                LOGGER.warn("Unhandled type => " + type);
                return LogicalTypeRoot.RAW.name();
            }
        }
    }
}
