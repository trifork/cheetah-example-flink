package cheetah.example.flinkstates.util;

import cheetah.example.flinksqlintervaljoin.util.ODataUtil;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.ep.EntityProviderException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.ClassLoaderUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ODataUtilTests {

    @Test
    public void testReadEdm() throws URISyntaxException, IOException, EntityProviderException, EdmException {
        var edmXmlResourceUri = ClassLoaderUtils.getDefaultClassLoader().getResource("salesorder_metadata_edm_xml.xml").toURI();
        var edmBytes = Files.readAllBytes(Paths.get(edmXmlResourceUri));

        var edm = ODataUtil.readEdm(edmBytes);

        // test samples
        var salesOrderEntity = edm.getEntityType("API_SALES_ORDER_SRV", "A_SalesOrderType");
        Assertions.assertEquals("String", salesOrderEntity.getProperty("SalesOrder").getType().getName());
        Assertions.assertEquals("Decimal", salesOrderEntity.getProperty("TotalNetAmount").getType().getName());

        var salesOrderItemEntity = edm.getEntityType("API_SALES_ORDER_SRV", "A_SalesOrderItemType");
        Assertions.assertEquals("String", salesOrderItemEntity.getProperty("SalesOrder").getType().getName());
        Assertions.assertEquals("DateTime", salesOrderItemEntity.getProperty("PricingDate").getType().getName());
    }
}
