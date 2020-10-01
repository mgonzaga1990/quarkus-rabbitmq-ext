package io.smallrye.reactive.messaging.rabbitmq.i18n;

import org.jboss.logging.BasicLogger;
import org.jboss.logging.Logger;
import org.jboss.logging.annotations.Cause;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;

/**
 * Logging for AMQP Connector
 * Assigned ID range is 16300-
 */
@MessageLogger(projectCode = "",length = 5)
public interface RabbitMQLogger extends BasicLogger {
    RabbitMQLogger log = Logger.getMessageLogger(RabbitMQLogger.class,"io.smallrye.reactive.messaging.rabbit-mq");

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 16300, value = "Establishing connection with RabbitMQ broker")
    void establishingConnection();

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 16301, value = "Connection with RabbitMQ broker established")
    void connectionEstablished();

    @LogMessage(level = Logger.Level.ERROR)
    @Message(id = 16302, value = "RabbitMQ Connection failure")
    void connectionFailure(@Cause Throwable t);

    @LogMessage(level = Logger.Level.ERROR)
    @Message(id = 16303, value = "Unable to connect to the broker, retry will be attempted")
    void unableToConnectToBroker(@Cause Throwable t);

    @LogMessage(level = Logger.Level.ERROR)
    @Message(id = 16216, value = "Unable to recover from connection disruption")
    void unableToRecoverFromConnectionDisruption(@Cause Throwable t);
}
