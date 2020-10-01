package io.smallrye.reactive.messaging.rabbitmq;

import io.smallrye.mutiny.subscription.MultiEmitter;
import io.vertx.rabbitmq.RabbitMQClient;
import org.eclipse.microprofile.reactive.messaging.Message;

@FunctionalInterface
public interface ConsumerHandler {
    void handle(String queueOrChannel, MultiEmitter<? super Message<String>> emitter, RabbitMQClient client);
}
