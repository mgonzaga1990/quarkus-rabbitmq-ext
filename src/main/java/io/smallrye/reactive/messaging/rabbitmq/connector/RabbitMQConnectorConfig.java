package io.smallrye.reactive.messaging.rabbitmq.connector;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.subscription.MultiEmitter;
import io.smallrye.reactive.messaging.rabbitmq.ConsumerHandler;
import io.smallrye.reactive.messaging.rabbitmq.RabbitMQConnectorCommonConfiguration;
import io.smallrye.reactive.messaging.rabbitmq.RabbitMQConnectorIncomingConfiguration;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import static io.smallrye.reactive.messaging.rabbitmq.RabbitMQHelper.*;

import io.vertx.core.Vertx;
import io.vertx.rabbitmq.*;
import io.vertx.rabbitmq.impl.RabbitMQClientImpl;
import org.eclipse.microprofile.reactive.messaging.Message;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

public abstract class RabbitMQConnectorConfig {

    protected final List<RabbitMQClient> clients = new CopyOnWriteArrayList<>();
    private static final Logger log = Logger.getLogger(RabbitMQConnectorConfig.class.getName());

    protected RabbitMQOptions configurationOptions(RabbitMQConnectorCommonConfiguration config){
       return new RabbitMQOptions()
                .setUser(config.getUsername())
                .setPassword(config.getPassword())
                .setPort(config.getPort())
                .setHost(config.getHost())
                .setUri(config.getUrl().orElse(null))

                .setConnectionTimeout(config.getConnectionTimeout())
                .setConnectionRetries(config.getConnectionRetry())
                .setRequestedHeartbeat(config.getRequestHeartbeat())
                .setHandshakeTimeout(config.getHandshakeTimeout())
                .setRequestedChannelMax(config.getRequestedChannelMax())
                .setNetworkRecoveryInterval(config.getNetworkRecoveryInterval())
                .setAutomaticRecoveryEnabled(config.getAutomaticRecovery());

    }

    protected void connection(Vertx vertx,
                              RabbitMQConnectorCommonConfiguration configuration,
                              String queueOrChannel,
                              MultiEmitter<? super Message<String>> emitter,
                              ConsumerHandler handler) {

        final RabbitMQClient client = new RabbitMQClientImpl(vertx, configurationOptions(configuration));
        client.start(voidAsyncResult -> {
            if (voidAsyncResult.succeeded()) {
                //flag to create queue if not exists
                queueDeclareWithConfig(client, queueOrChannel);

                handler.handle(queueOrChannel, emitter, client);
            }
        });
        this.clients.add(client);
    }

}
