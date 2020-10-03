package io.smallrye.reactive.messaging.rabbitmq;

import io.smallrye.mutiny.subscription.MultiEmitter;
import io.vertx.rabbitmq.RabbitMQClient;
import io.vertx.rabbitmq.RabbitMQMessage;
import org.eclipse.microprofile.reactive.messaging.Message;

@FunctionalInterface
public interface ConsumerHandler< T extends RabbitMQMessage> {
    void handle(String queueOrChannel, MultiEmitter<? super Message<T>> emitter, RabbitMQClient client);
}
