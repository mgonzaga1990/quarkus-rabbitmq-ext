package io.smallrye.reactive.messaging.rabbitmq;

import io.smallrye.mutiny.Uni;
import io.smallrye.reactive.messaging.rabbitmq.connector.RabbitMQSender;
import io.vertx.core.json.JsonObject;
import io.vertx.rabbitmq.RabbitMQClient;
import io.vertx.rabbitmq.RabbitMQOptions;

import java.util.Optional;
import java.util.logging.Logger;

public abstract class RabbitMQConfiguration {

    private static final Logger log = Logger.getLogger(RabbitMQConfiguration.class.getName());

    protected RabbitMQOptions configuration(RabbitMQConnectorOutgoingConfiguration config) {
        return new RabbitMQOptions()
                .setUser(config.getUsername())
                .setPassword(config.getPassword())
                .setPort(config.getPort())
                .setHost(config.getHost())
                .setUri(config.getUrl().orElse(null))
                .setConnectionTimeout(6000)
                .setRequestedHeartbeat(60)
                .setHandshakeTimeout(6000)
                .setRequestedChannelMax(5)
                .setNetworkRecoveryInterval(500)
                .setAutomaticRecoveryEnabled(true);
    }


}
