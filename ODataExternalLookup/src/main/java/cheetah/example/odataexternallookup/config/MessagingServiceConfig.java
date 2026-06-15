package cheetah.example.odataexternallookup.config;

import java.io.Serializable;
import java.util.Map;

import com.sap.cloud.servicesdk.xbem.core.MessagingServiceFactory;
import com.sap.cloud.servicesdk.xbem.core.exception.MessagingException;
import com.sap.cloud.servicesdk.xbem.core.impl.MessagingServiceFactoryCreator;
import com.sap.cloud.servicesdk.xbem.extension.sapcp.jms.MessagingServiceJmsConnectionFactory;
import com.sap.cloud.servicesdk.xbem.extension.sapcp.jms.MessagingServiceJmsSettings;

public class MessagingServiceConfig implements Serializable {

    public MessagingServiceFactory getMessagingServiceFactory(Map<String, Object> credentials) {

        return MessagingServiceFactoryCreator.createFactoryFromCredentials(credentials);
    }

    public MessagingServiceJmsConnectionFactory getMessagingServiceJmsConnectionFactory(
            MessagingServiceFactory messagingServiceFactory) {
        try {
            /*
             * The settings object is preset with default values (see JavaDoc)
             * and can be adjusted. The settings aren't required and depend on
             * the use-case. Note: a connection will be closed after an idle
             * time of 5 minutes.
             */
            MessagingServiceJmsSettings settings = new MessagingServiceJmsSettings();
            settings.setFailoverMaxReconnectAttempts(5); // use -1 for unlimited attempts
            settings.setFailoverInitialReconnectDelay(3000);
            settings.setFailoverReconnectDelay(3000);
            settings.setJmsRequestTimeout(30000);
            settings.setAmqpIdleTimeout(-1);

            return messagingServiceFactory.createConnectionFactory(MessagingServiceJmsConnectionFactory.class,
                    settings);
        } catch (MessagingException e) {
            throw new IllegalStateException("Unable to create the Connection Factory", e);
        }
    }
}