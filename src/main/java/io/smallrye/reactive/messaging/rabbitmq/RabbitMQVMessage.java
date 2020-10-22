package io.smallrye.reactive.messaging.rabbitmq;

import io.vertx.rabbitmq.RabbitMQClient;
import io.vertx.rabbitmq.RabbitMQMessage;
import org.eclipse.microprofile.reactive.messaging.Message;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Logger;

public class RabbitMQVMessage<T extends RabbitMQMessage> implements Message<T> {

    private static final Logger log = Logger.getLogger(RabbitMQVMessage.class.getName());

    private RabbitMQMessage mqMessage;
    private RabbitMQClient mqClient;

    RabbitMQVMessage(RabbitMQMessage message, RabbitMQClient client){
        this.mqMessage = message;
        this.mqClient = client;
    }

    @Override
    public T getPayload() {
        return (T) this.mqMessage;
    }

    @Override
    public Supplier<CompletionStage<Void>> getAck() {
        final Supplier<CompletionStage<Void>> ack = () -> CompletableFuture.supplyAsync(() -> {
            final long deliveryTag = this.mqMessage.envelope().deliveryTag();
            this.mqClient.basicAck(deliveryTag, false, asyncResult -> {
                if(asyncResult.succeeded()){
                    log.info("ack successfully");
                }else{
                    asyncResult.cause().printStackTrace();
                    //this.nack(asyncResult.cause());
                }
            });
            return (Void) null;
        });
        return ack;
    }

    //TODO : implement Nack
    @Override
    public Function<Throwable, CompletionStage<Void>> getNack() {
        return null;
    }
}