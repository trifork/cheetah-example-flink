package cheetah.example.odataexternallookup.function;

import java.util.Map;

import javax.annotation.Nullable;
import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.Session;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.source.RichParallelSourceFunction;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cheetah.example.odataexternallookup.config.MessagingServiceConfig;

// https://github.com/miwurster/flink-connector-jms/blob/master/src/main/java/org/apache/flink/streaming/connectors/jms/JmsQueueSource.java
// https://github.com/SAP-samples/event-mesh-client-java-samples/blob/main/emjapi-samples-jms-p2p/src/main/java/com/sap/xbem/sample/sapcp/jms/p2p/services/MessagingServiceRestController.java

@SuppressWarnings("deprecation")
public class JmsQueueSource extends RichParallelSourceFunction<JSONObject> {
    private static final long serialVersionUID = 42L;

    private static Logger LOG = LoggerFactory.getLogger(JmsQueueSource.class);

    private volatile boolean isRunning = true;

    private static final String QUEUE_PREFIX = "queue:"; // mandatory prefix for connection to a queue.

    // private String messageSelector;

    private Connection connection;

    private String queueName;

    private Map<String, Object> credentials;

    public JmsQueueSource(Map<String, Object> credentials, String queueName) {
        this.credentials = credentials;
        this.queueName = queueName;
    }

    @Override
    public void open(final Configuration parameters) throws Exception {
        super.open(parameters);

        var messagingServiceFactory = new MessagingServiceConfig().getMessagingServiceFactory(credentials);
        var connectionFactory = new MessagingServiceConfig()
                .getMessagingServiceJmsConnectionFactory(messagingServiceFactory);

        connection = connectionFactory.createConnection();
    }

    @Override
    public void run(final SourceContext<JSONObject> context) throws JMSException {

        MessageConsumer consumer = null;
        try {
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            connection.start();
            Queue queue = session.createQueue(QUEUE_PREFIX + queueName); // see comments above
            consumer = session.createConsumer(queue);
            while (isRunning) {

                // long timeout = 1000 * 10; // 10 seconds
                var message = consumer.receive(); // Blocking call. You can either define a
                // timeout or use a message listener
                context.collect(convert(message));

            }

        } catch (JMSException e) {
            LOG.error("Error receiving message from [{}]: {}", queueName, e.getLocalizedMessage());
            throw new UncategorizedJmsException(e);
        } finally {
            consumer.close();
        }
    }

    private JSONObject convert(Message object) throws JMSException {
        if (object instanceof BytesMessage) {
            BytesMessage message = (BytesMessage) object;
            byte[] byteData;
            try {
                byteData = new byte[(int) message.getBodyLength()];
                message.readBytes(byteData);

                return new JSONObject(new String(byteData));
            } catch (JMSException e) {
                throw e;
            }
        }
        throw new JMSException("Message type not supported" + object.getClass().getName());
    }

    @Override
    public void cancel() {
        isRunning = false;
    }

    @Override
    public void close() throws Exception {
        super.close();
        closeConnection(connection, true);
    }

    /**
     * Close the given JMS Connection and ignore any thrown exception.
     * <p>
     * This is useful for typical {@code finally} blocks in manual JMS code.
     * 
     * @param con  the JMS Connection to close (may be {@code null})
     * @param stop whether to call {@code stop()} before closing
     */
    private static void closeConnection(@Nullable Connection con, boolean stop) {
        if (con != null) {
            try {
                if (stop) {
                    try {
                        con.stop();
                    } finally {
                        con.close();
                    }
                } else {
                    con.close();
                }
            } catch (JMSException ex) {
                LOG.debug("Could not close JMS Connection", ex);
            } catch (Throwable ex) {
                // We don't trust the JMS provider: It might throw RuntimeException or Error.
                LOG.debug("Unexpected exception on closing JMS Connection", ex);
            }
        }
    }
}