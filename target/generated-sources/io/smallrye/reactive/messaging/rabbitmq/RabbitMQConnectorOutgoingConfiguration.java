package io.smallrye.reactive.messaging.rabbitmq;

import java.util.Optional;
import org.eclipse.microprofile.config.Config;

/**
 * Extract the outgoing configuration for the {@code smallrye-rabbitmq} connector.
*/
public class RabbitMQConnectorOutgoingConfiguration extends RabbitMQConnectorCommonConfiguration {

  /**
   * Creates a new RabbitMQConnectorOutgoingConfiguration.
   */
  public RabbitMQConnectorOutgoingConfiguration(Config config) {
    super(config);
    validate();
  }

  public void validate() {
    super.validate();
  }
}
