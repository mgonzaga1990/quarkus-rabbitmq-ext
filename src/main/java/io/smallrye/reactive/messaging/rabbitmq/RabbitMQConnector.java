package io.smallrye.reactive.messaging.rabbitmq;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.reactive.messaging.annotations.ConnectorAttribute;
import io.smallrye.reactive.messaging.connectors.ExecutionHolder;
import io.smallrye.reactive.messaging.rabbitmq.connector.RabbitMQConnectorConfig;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.core.Vertx;
import io.vertx.rabbitmq.QueueOptions;
import io.vertx.rabbitmq.RabbitMQClient;
import io.vertx.rabbitmq.RabbitMQMessage;
import io.vertx.rabbitmq.impl.RabbitMQClientImpl;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.spi.Connector;
import org.eclipse.microprofile.reactive.messaging.spi.IncomingConnectorFactory;
import org.eclipse.microprofile.reactive.messaging.spi.OutgoingConnectorFactory;
import org.eclipse.microprofile.reactive.streams.operators.PublisherBuilder;
import org.eclipse.microprofile.reactive.streams.operators.ReactiveStreams;
import org.eclipse.microprofile.reactive.streams.operators.SubscriberBuilder;

import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.BeforeDestroyed;
import javax.enterprise.event.Observes;
import javax.enterprise.event.Reception;
import javax.inject.Inject;
import java.util.logging.Logger;

import static io.smallrye.reactive.messaging.annotations.ConnectorAttribute.Direction.INCOMING_AND_OUTGOING;
import static io.smallrye.reactive.messaging.rabbitmq.RabbitMQHelper.queueDeclareWithConfig;

@ApplicationScoped
@Connector(RabbitMQConnector.CONNECTOR_NAME)

//connection related attribute
@ConnectorAttribute(name = "username", direction = INCOMING_AND_OUTGOING, description = "The username used to authenticate to the broker", type = "string", alias = "rabbitmq-username",defaultValue = "guest")
@ConnectorAttribute(name = "password", direction = INCOMING_AND_OUTGOING, description = "The password used to authenticate to the broker", type = "string", alias = "rabbitmq-password",defaultValue = "guest")
@ConnectorAttribute(name = "host", direction = INCOMING_AND_OUTGOING, description = "The broker hostname", type = "string", alias = "rabbitmq-host", defaultValue = "localhost")
@ConnectorAttribute(name = "url", direction = INCOMING_AND_OUTGOING, description = "The broker uri", type = "string", alias = "rabbitmq-url")

@ConnectorAttribute(name = "connection-retry", direction = INCOMING_AND_OUTGOING, description = "The number of reconnection attempts", type = "int", alias = "rabbitmq-connection-timeout", defaultValue = "2")
@ConnectorAttribute(name = "connection-timeout", direction = INCOMING_AND_OUTGOING, description = "", type = "int", alias = "rabbitmq-reconnect-attempts", defaultValue = "50000")
@ConnectorAttribute(name = "request-heartbeat", direction = INCOMING_AND_OUTGOING, description = "", type = "int", alias = "rabbitmq-request-timeout", defaultValue = "500")
@ConnectorAttribute(name = "handshake-timeout", direction = INCOMING_AND_OUTGOING, description = "", type = "int", alias = "rabbitmq-handshake-timeout", defaultValue = "500")
@ConnectorAttribute(name = "requested-channel-max", direction = INCOMING_AND_OUTGOING, description = "", type = "int", alias = "rabbitmq-requested-channel-max", defaultValue = "2047")
@ConnectorAttribute(name = "network-recovery-interval", direction = INCOMING_AND_OUTGOING, description = "", type = "int", alias = "rabbitmq-network-recovery-interval", defaultValue = "500")
@ConnectorAttribute(name = "automatic-recovery", direction = INCOMING_AND_OUTGOING, description = "", type = "boolean", alias = "rabbitmq-automatic-recovery", defaultValue = "true")

//queue related attribute
@ConnectorAttribute(name = "queue", direction = INCOMING_AND_OUTGOING, description = "", type = "string")
@ConnectorAttribute(name = "auto-create-queue", direction = INCOMING_AND_OUTGOING, description = "", type = "boolean", mandatory = true)

@ConnectorAttribute(name = "port", direction = INCOMING_AND_OUTGOING, description = "The broker port", type = "int", alias = "rabbitmq-port", defaultValue = "5672")
@ConnectorAttribute(name = "virtual-host", direction = INCOMING_AND_OUTGOING, description = "A comma separated virtualhost(s)", type = "string", alias = "rabbitmq-virtual-host")

public final class RabbitMQConnector extends RabbitMQConnectorConfig implements IncomingConnectorFactory,OutgoingConnectorFactory {

    private static final Logger log = Logger.getLogger(RabbitMQConnector.class.getName());
    public static final String CONNECTOR_NAME = "smallrye-rabbitmq";

    @Inject
    private ExecutionHolder executionHolder;

    @Override
    public PublisherBuilder<? extends Message<RabbitMQMessage>> getPublisherBuilder(Config config) {
        final RabbitMQConnectorIncomingConfiguration ic = new RabbitMQConnectorIncomingConfiguration(config);
        final String queueOrChannel = ic.getQueue().orElse(ic.getChannel());

        final Multi<RabbitMQVMessage<RabbitMQMessage>> publisher = Multi.createFrom().emitter(emitter -> {
            connection(this.vertx().getDelegate(),ic, queueOrChannel, emitter,(channel, e, client) -> {
                client.basicConsumer(queueOrChannel,new QueueOptions().setAutoAck(false), rabbitMQConsumerAsyncResult -> {
                    if (rabbitMQConsumerAsyncResult.succeeded()) {
                        rabbitMQConsumerAsyncResult.result()
                                .handler(message -> e.emit(new RabbitMQVMessage<>(message,client)));
                    } else {
                        e.fail(rabbitMQConsumerAsyncResult.cause());
                    }
                });
            });
        });


        return ReactiveStreams.fromPublisher(publisher);
    }

    @Override
    public SubscriberBuilder<? extends Message<?>, Void> getSubscriberBuilder(Config config) {
        final RabbitMQConnectorOutgoingConfiguration oc = new RabbitMQConnectorOutgoingConfiguration(config);
        final String queueOrChannel = oc.getQueue().orElse(oc.getChannel());

        final RabbitMQSender rabbitMQSender = RabbitMQSender.create();

        final RabbitMQClient client = new RabbitMQClientImpl(this.vertx().getDelegate(), configurationOptions(oc));
        client.start(voidAsyncResult -> {
            if (voidAsyncResult.succeeded()) {
                //flag to create queue if not exists
                if(oc.getAutoCreateQueue()){
                    queueDeclareWithConfig(client, queueOrChannel);
                }
                Uni.createFrom().publisher(rabbitMQSender.broadcastProcessor()) .subscribe().with(payload ->{
                    final String payloadData = payload.getPayload();
                    log.info("Data received " + payloadData);

                    client.confirmSelect(confirmResult -> {
                        if(confirmResult.succeeded()) {
                            JsonObject message = new JsonObject();
                            message.put("body",payloadData);
                            client.basicPublish("", queueOrChannel, message, pubResult -> {
                                if (pubResult.succeeded()) {

                                    // Check the message got confirmed by the broker.
                                    client.waitForConfirms(waitResult -> {
                                        if(waitResult.succeeded())
                                            log.info("Message published !");
                                        else
                                            waitResult.cause().printStackTrace();
                                    });
                                } else {
                                    pubResult.cause().printStackTrace();
                                }
                            });
                        } else {
                            confirmResult.cause().printStackTrace();
                        }
                    });
                });
            }
        });

        return ReactiveStreams.fromSubscriber(rabbitMQSender);
    }

    public void terminate(@Observes(notifyObserver = Reception.IF_EXISTS) @Priority(50) @BeforeDestroyed(ApplicationScoped.class) Object event){
        clients.forEach(c -> c.stop(voidAsyncResult -> {
          if(voidAsyncResult.succeeded()){
              log.info("server stopped");
          }else{
              voidAsyncResult.cause().printStackTrace();
          }
        }));
        clients.clear();
    }

    private Vertx vertx() {
        return this.executionHolder.vertx();
    }

}
