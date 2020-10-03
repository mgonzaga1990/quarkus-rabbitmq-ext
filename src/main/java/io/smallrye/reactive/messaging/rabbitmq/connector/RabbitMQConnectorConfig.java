package io.smallrye.reactive.messaging.rabbitmq.connector;

import io.smallrye.mutiny.subscription.MultiEmitter;
import io.smallrye.reactive.messaging.rabbitmq.ConsumerHandler;
import io.smallrye.reactive.messaging.rabbitmq.RabbitMQConnectorCommonConfiguration;
import io.smallrye.reactive.messaging.rabbitmq.RabbitMQVMessage;
import io.vertx.core.Vertx;
import io.vertx.rabbitmq.RabbitMQClient;
import io.vertx.rabbitmq.RabbitMQMessage;
import io.vertx.rabbitmq.RabbitMQOptions;
import io.vertx.rabbitmq.impl.RabbitMQClientImpl;
import org.eclipse.microprofile.reactive.messaging.Message;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

import static io.smallrye.reactive.messaging.rabbitmq.RabbitMQHelper.queueDeclareWithConfig;

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
                              MultiEmitter<? super RabbitMQVMessage<RabbitMQMessage>> emitter,
                              ConsumerHandler handler) {

        final RabbitMQClient client = new RabbitMQClientImpl(vertx, configurationOptions(configuration));
        client.start(voidAsyncResult -> {
            if (voidAsyncResult.succeeded()) {

                //auto create queue ttl
                if(configuration.getAutoCreateQueue()){
                    queueDeclareWithConfig(client, queueOrChannel);
                }

                handler.handle(queueOrChannel, emitter, client);
            }
        });
        this.clients.add(client);
    }

}
