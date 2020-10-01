package io.smallrye.reactive.messaging.rabbitmq;

import io.vertx.core.json.JsonObject;
import io.vertx.rabbitmq.RabbitMQClient;

import java.util.logging.Logger;

public class RabbitMQHelper {
    private static final Logger log = Logger.getLogger(RabbitMQHelper.class.getName());

    public static void queueDeclareWithConfig(RabbitMQClient client, String queue) {
        JsonObject config = new JsonObject();
        config.put("x-message-ttl", 10_000L);

        client.queueDeclare(queue, true, false, true, config, queueResult -> {
            if (queueResult.succeeded()) {
                log.info("Queue declared!");
            } else {
                log.info("Queue failed to be declared!");
                queueResult.cause().printStackTrace();
            }
        });
    }
}
