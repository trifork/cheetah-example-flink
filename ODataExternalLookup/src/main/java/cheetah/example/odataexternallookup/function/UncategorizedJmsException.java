package cheetah.example.odataexternallookup.function;

import javax.jms.JMSException;

/**
 * JmsException to be thrown when no other matching subclass found.
 *
 */
@SuppressWarnings("serial")
public class UncategorizedJmsException extends JMSException {

    /**
     * Constructor that takes a message.
     * 
     * @param msg the detail message
     */
    public UncategorizedJmsException(String msg) {
        super(msg);
    }

    /**
     * Constructor that takes a message and a root cause.
     * 
     * @param msg   the detail message
     * @param cause the cause of the exception. This argument is generally
     *              expected to be a proper subclass of
     *              {@link jakarta.jms.JMSException},
     *              but can also be a JNDI NamingException or the like.
     */
    public UncategorizedJmsException(String msg, Throwable cause) {
        super(msg);
    }

    /**
     * Constructor that takes a root cause only.
     * 
     * @param cause the cause of the exception. This argument is generally
     *              expected to be a proper subclass of
     *              {@link jakarta.jms.JMSException},
     *              but can also be a JNDI NamingException or the like.
     */
    public UncategorizedJmsException(Throwable cause) {
        super("Uncategorized exception occurred during JMS processing");
    }

}