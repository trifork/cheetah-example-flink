package cheetah.example.flinksqlintervaljoin.util;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.olingo.odata2.api.edm.Edm;
import org.apache.olingo.odata2.api.ep.EntityProvider;
import org.apache.olingo.odata2.api.ep.EntityProviderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static com.sap.cloud.sdk.cloudplatform.connectivity.HttpClientAccessor.getHttpClient;

public class ODataUtil {
    public static final String METADATA = "$metadata";
    public static final String SEPARATOR = "/";
    private static final Logger LOGGER = LoggerFactory.getLogger(ODataUtil.class);

    public static byte[] getEdmMetadataBytes(String serviceUrl, String entitySetName) throws IOException {

            String extendedServiceUrl = serviceUrl + //SEPARATOR +
                    entitySetName +
                    SEPARATOR + METADATA;

            LOGGER.info("Metadata url => " + extendedServiceUrl);

            final HttpGet get = new HttpGet(extendedServiceUrl);
            // get.setHeader(AUTHORIZATION_HEADER, getAuthorizationHeader());
            // get.setHeader(CSRF_TOKEN_HEADER, CSRF_TOKEN_FETCH);

            HttpResponse response = getHttpClient().execute(get);

            // m_csrfToken = response.getFirstHeader(CSRF_TOKEN_HEADER).getValue();
            // logger.info("CSRF token => " + m_csrfToken);

            var inputStream = response.getEntity().getContent();

            return inputStream.readAllBytes();

    }

    public static Edm readEdm(byte[] edmMetadataBytes) throws EntityProviderException, IllegalStateException {
        var source = new ByteArrayInputStream(edmMetadataBytes);
        return EntityProvider.readMetadata(source, false);
    }
}
