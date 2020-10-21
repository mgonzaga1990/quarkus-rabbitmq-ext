package io.smallrye.reactive.messaging.rabbitmq;

import io.smallrye.mutiny.Uni;
import io.smallrye.reactive.messaging.rabbitmq.connector.RabbitMQSender;
import io.vertx.core.json.JsonObject;
import io.vertx.rabbitmq.RabbitMQClient;

import java.util.Optional;
import java.util.logging.Logger;

public class RabbitMQExchange {

    private static final Logger log = Logger.getLogger(RabbitMQExchange.class.getName());

    /**
     * The rest is configured via exchange
     * @param config
     * @param queueOrChannel
     * @param rabbitMQSender
     * @param client
     * @param exchangeOption
     */
    public static void configureExchange(RabbitMQConnectorOutgoingConfiguration config,
                                     String queueOrChannel,
                                     RabbitMQSender rabbitMQSender,
                                     RabbitMQClient client,
                                     Optional<String> exchangeOption) {

        log.info("Configuring exchange. . . ");

        final JsonObject jsonObject = new JsonObject()
                .put("x-dead-letter-exchange", "my.deadletter.exchange")
                .put("alternate-exchange", "my.alternate.exchange")
                .put("x-message-ttl", 10_000L);

        //declare exchange
        final String routingKey = config.getRoutingKey().orElse("default-key");
        final String exchange = exchangeOption.get();
        client.exchangeDeclare(exchange, config.getExchangeType(), config.getDurable(), config.getAutoDelete(), jsonObject, onResult -> {
            if (!onResult.succeeded()) {
                log.severe("Failed to declare exchange! " + onResult.cause().getMessage());
            } else {
                //declare queue
                final Boolean exchangeDurable = config.getExchangeDurable();
                final Boolean exchangeExclusive = config.getExchangeExclusive();
                final Boolean exchangeAutoDelete = config.getExchangeAutoDelete();

                client.queueDeclare(queueOrChannel, exchangeDurable, exchangeExclusive, exchangeAutoDelete, jsonObject, queueResult -> {
                    if(!queueResult.succeeded()){
                        log.severe("Queue failed to be declared! " + queueResult.cause().getMessage());
                    } else {
                        //bind exchange to queue
                        client.queueBind(queueOrChannel,exchange, routingKey, bindResult->{
                            if(!bindResult.succeeded()){
                                log.severe("Queue binding to exchange failed! " + bindResult.cause().getMessage());
                            }else{
                                //listen to the client publisher
                                Uni.createFrom().item(rabbitMQSender.broadcastProcessor())
                                        .subscribe().with(payload -> {
                                    payload.subscribe().with(message -> {
                                        JsonObject msg = new JsonObject()
                                                .put("body", String.valueOf(message.getPayload()))
                                                .put("contentType",config.getContentType());
                                        client.basicPublish(exchange,routingKey,msg,r1->{
                                            if(r1.succeeded()){
                                                log.info("Message sent");
                                            }else{
                                                r1.cause().printStackTrace();
                                            }
                                        });
                                    });
                                });
                            }
                        });
                    }
                });
            }
        });
    }
}
