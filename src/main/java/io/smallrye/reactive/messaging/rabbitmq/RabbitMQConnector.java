package io.smallrye.reactive.messaging.rabbitmq;

import io.smallrye.mutiny.Multi;
import io.smallrye.reactive.messaging.annotations.ConnectorAttribute;
import io.smallrye.reactive.messaging.connectors.ExecutionHolder;
import io.smallrye.reactive.messaging.rabbitmq.connector.RabbitMQSender;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.core.Vertx;
import io.vertx.rabbitmq.*;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.spi.Connector;
import org.eclipse.microprofile.reactive.messaging.spi.IncomingConnectorFactory;
import org.eclipse.microprofile.reactive.messaging.spi.OutgoingConnectorFactory;
import org.eclipse.microprofile.reactive.streams.operators.PublisherBuilder;
import org.eclipse.microprofile.reactive.streams.operators.ReactiveStreams;
import org.eclipse.microprofile.reactive.streams.operators.SubscriberBuilder;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Optional;
import java.util.logging.Logger;

import static io.smallrye.reactive.messaging.annotations.ConnectorAttribute.Direction.*;

@ApplicationScoped
@Connector(RabbitMQConnector.CONNECTOR_NAME)

//connection related attribute
@ConnectorAttribute(name = "username", direction = INCOMING_AND_OUTGOING, description = "The username used to authenticate to the broker", type = "string", alias = "rabbitmq-username",defaultValue = "guest")
@ConnectorAttribute(name = "password", direction = INCOMING_AND_OUTGOING, description = "The password used to authenticate to the broker", type = "string", alias = "rabbitmq-password",defaultValue = "guest")
@ConnectorAttribute(name = "host", direction = INCOMING_AND_OUTGOING, description = "The broker hostname", type = "string", alias = "rabbitmq-host", defaultValue = "localhost")
@ConnectorAttribute(name = "url", direction = INCOMING_AND_OUTGOING, description = "The broker uri", type = "string", alias = "rabbitmq-url")
@ConnectorAttribute(name = "port", direction = INCOMING_AND_OUTGOING, description = "The broker port", type = "int", alias = "rabbitmq-port", defaultValue = "5672")

@ConnectorAttribute(name = "queue", direction = INCOMING_AND_OUTGOING, description = "", type = "string")
@ConnectorAttribute(name = "connection-timeout", direction = INCOMING_AND_OUTGOING, description = "", type = "int",defaultValue = "6000")
@ConnectorAttribute(name = "heartbeat", direction = INCOMING_AND_OUTGOING, description = "", type = "int",defaultValue = "60")
@ConnectorAttribute(name = "handshake-timeout", direction = INCOMING_AND_OUTGOING, description = "", type = "int",defaultValue = "6000")
@ConnectorAttribute(name = "channel-max", direction = INCOMING_AND_OUTGOING, description = "", type = "int",defaultValue = "5")
@ConnectorAttribute(name = "recovery-interval", direction = INCOMING_AND_OUTGOING, description = "", type = "int",defaultValue = "500")
@ConnectorAttribute(name = "automatic-recovery", direction = INCOMING_AND_OUTGOING, description = "", type = "boolean",defaultValue = "true")

//outgoing config
@ConnectorAttribute(name = "exchange", direction = OUTGOING, description = "", type = "string")
@ConnectorAttribute(name = "exchange-durable", direction = OUTGOING, description = "", type = "boolean",defaultValue = "true")
@ConnectorAttribute(name = "exchange-exclusive", direction = OUTGOING, description = "", type = "boolean",defaultValue = "false")
@ConnectorAttribute(name = "exchange-auto-delete", direction = OUTGOING, description = "", type = "boolean",defaultValue = "true")
@ConnectorAttribute(name = "durable", direction = OUTGOING, description = "", type = "boolean",defaultValue = "true")
@ConnectorAttribute(name = "auto-delete", direction = OUTGOING, description = "", type = "boolean",defaultValue = "true")
@ConnectorAttribute(name = "exchange-type", direction = OUTGOING, description = "", type = "string",defaultValue = "fanout")
@ConnectorAttribute(name = "routing-key", direction = OUTGOING, description = "", type = "string")
@ConnectorAttribute(name = "content-type", direction = OUTGOING, description = "", type = "string",defaultValue = "application/octet-stream")

//incoming config
@ConnectorAttribute(name = "max-internal-queue", direction = INCOMING, description = "", type = "int",defaultValue = "1000")
@ConnectorAttribute(name = "keep-most-recent", direction = INCOMING, description = "", type = "boolean",defaultValue = "true")

public final class RabbitMQConnector extends RabbitMQConfiguration implements OutgoingConnectorFactory, IncomingConnectorFactory {

    private static final Logger log = Logger.getLogger(RabbitMQConnector.class.getName());
    public static final String CONNECTOR_NAME = "smallrye-rabbitmq";

    @Inject
    private ExecutionHolder executionHolder;

    @Override
    public SubscriberBuilder<? extends Message<?>, Void> getSubscriberBuilder(Config configs) {
        final RabbitMQConnectorOutgoingConfiguration config = new RabbitMQConnectorOutgoingConfiguration(configs);
        final RabbitMQOptions rabbitMQOptions = configuration(config);

        final String queueOrChannel = config.getQueue().orElse(config.getChannel());

        final RabbitMQSender rabbitMQSender = RabbitMQSender.create();
        //configuration
        final RabbitMQClient client = RabbitMQClient.create(this.vertx().getDelegate(), rabbitMQOptions);
        client.start(voidAsyncResult -> {
            if (!voidAsyncResult.succeeded()) {
                //will throw an error if the queue doesn't exists
                log.severe("Fail to connect to RabbitMQ " + voidAsyncResult.cause().getMessage());
            }else{
                final Optional<String> exchangeOption = config.getExchange();
                if(!exchangeOption.isEmpty()){
                    RabbitMQExchange.configureExchange(config, queueOrChannel, rabbitMQSender, client, exchangeOption);
                }
            }
        });

        return ReactiveStreams.fromSubscriber(rabbitMQSender);
    }

    private Vertx vertx() {
        return this.executionHolder.vertx();
    }

    @Override
    public PublisherBuilder<? extends Message<RabbitMQMessage>> getPublisherBuilder(Config config) {
        final RabbitMQConnectorIncomingConfiguration ic = new RabbitMQConnectorIncomingConfiguration(config);
        final String queueOrChannel = ic.getQueue().orElse(ic.getChannel());

        final RabbitMQOptions rabbitMQOptions = configuration(ic);
        rabbitMQOptions.setConnectionRetries(5);
        rabbitMQOptions.setIncludeProperties(true);

        //configuration
        final RabbitMQClient client = RabbitMQClient.create(this.vertx().getDelegate(), rabbitMQOptions);

        final Multi<RabbitMQVMessage<RabbitMQMessage>> publisher = Multi.createFrom().emitter(multiEmitter -> {
            client.start(voidAsyncResult -> {
                if (!voidAsyncResult.succeeded()) {
                    log.severe("Fail to connect to RabbitMQ " + voidAsyncResult.cause().getMessage());
                } else {
                    final QueueOptions options = new QueueOptions()
                            .setMaxInternalQueueSize(ic.getMaxInternalQueue())
                            .setKeepMostRecent(ic.getKeepMostRecent())
                            .setAutoAck(false);
                    //create consumer
                    client.basicConsumer(queueOrChannel,options,rabbitMQConsumerAsyncResult -> {
                        if(!rabbitMQConsumerAsyncResult.succeeded()){
                            rabbitMQConsumerAsyncResult.cause().printStackTrace();
                            log.info("Fail to create consumer for " + queueOrChannel);
                        }else{
                            final RabbitMQConsumer rabbitMQConsumer = rabbitMQConsumerAsyncResult.result();
                            rabbitMQConsumer.handler(message -> {
                                final RabbitMQVMessage<RabbitMQMessage> item = new RabbitMQVMessage<>(message, client);
                                multiEmitter.emit(item);
                            });
                        }
                    });
                }
            });
        });


        return ReactiveStreams.fromPublisher(publisher);
    }
}
