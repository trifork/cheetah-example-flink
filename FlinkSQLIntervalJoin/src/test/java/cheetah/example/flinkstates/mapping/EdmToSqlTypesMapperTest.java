package cheetah.example.flinkstates.mapping;

import cheetah.example.flinksqlintervaljoin.mapping.EdmToSqlTypesMapper;
import org.apache.olingo.odata2.api.edm.*;
import org.apache.olingo.odata2.core.edm.EdmDateTime;
import org.apache.olingo.odata2.core.edm.EdmString;
import org.apache.olingo.odata2.core.edm.EdmTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;

import static org.mockito.Mockito.when;

public class EdmToSqlTypesMapperTest {

    @Test
    public void testMapEdmPropertiesToSQLTypeStrings() throws EdmException {
        var mockedEntityType = Mockito.mock(EdmEntityType.class);
        when(mockedEntityType.getPropertyNames()).thenReturn(Arrays.asList("StringProperty", "TimeProperty", "DateTimeProperty"));

        var stringProperty = Mockito.mock(EdmProperty.class);
        when(stringProperty.getType()).thenReturn(EdmString.getInstance());
        when(mockedEntityType.getProperty("StringProperty")).thenReturn(stringProperty);

        var timeProperty = Mockito.mock(EdmProperty.class);
        when(timeProperty.getType()).thenReturn(EdmTime.getInstance());
        when(mockedEntityType.getProperty("TimeProperty")).thenReturn(timeProperty);

        var dateTimeProperty = Mockito.mock(EdmProperty.class);
        when(dateTimeProperty.getType()).thenReturn(EdmDateTime.getInstance());
        when(mockedEntityType.getProperty("DateTimeProperty")).thenReturn(dateTimeProperty);

        var mockedEdm = Mockito.mock(Edm.class);
        when(mockedEdm.getEntityType("namespace", "name")).thenReturn(mockedEntityType);

        // Act
        var mapper = new EdmToSqlTypesMapper();
        var mapping = mapper.mapEdmPropertiesToSQLTypeStrings(mockedEdm, "namespace", "name");

        // Assert
        Assertions.assertEquals(3, mapping.size());
        Assertions.assertEquals("VARCHAR", mapping.get("StringProperty"));
        Assertions.assertEquals("TIME", mapping.get("TimeProperty"));
        Assertions.assertEquals("VARCHAR", mapping.get("DateTimeProperty"));
    }

}
