package io.smallrye.reactive.messaging.rabbitmq;

import java.util.Optional;
import org.eclipse.microprofile.config.Config;

/**
 * Extract the incoming configuration for the {@code smallrye-rabbitmq} connector.
*/
public class RabbitMQConnectorIncomingConfiguration extends RabbitMQConnectorCommonConfiguration {

  /**
   * Creates a new RabbitMQConnectorIncomingConfiguration.
   */
  public RabbitMQConnectorIncomingConfiguration(Config config) {
    super(config);
    validate();
  }

  public void validate() {
    super.validate();
  }
}
