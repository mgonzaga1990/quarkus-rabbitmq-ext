package io.smallrye.reactive.messaging.rabbitmq;

import io.vertx.rabbitmq.RabbitMQOptions;

import java.util.logging.Logger;

public abstract class RabbitMQConfiguration {

    private static final Logger log = Logger.getLogger(RabbitMQConfiguration.class.getName());

    protected RabbitMQOptions configuration(RabbitMQConnectorCommonConfiguration config) {
        return new RabbitMQOptions()
                .setUser(config.getUsername())
                .setPassword(config.getPassword())
                .setPort(config.getPort())
                .setHost(config.getHost())
                .setUri(config.getUrl().orElse(null))
                .setConnectionRetries(5)
                .setIncludeProperties(true)
                .setConnectionTimeout(config.getConnectionTimeout())
                .setRequestedHeartbeat(config.getHeartbeat())
                .setHandshakeTimeout(config.getHandshakeTimeout())
                .setRequestedChannelMax(config.getChannelMax())
                .setNetworkRecoveryInterval(config.getRecoveryInterval())
                .setAutomaticRecoveryEnabled(config.getAutomaticRecovery());
    }


}
