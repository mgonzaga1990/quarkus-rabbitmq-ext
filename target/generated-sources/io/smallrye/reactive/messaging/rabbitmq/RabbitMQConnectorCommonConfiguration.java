package io.smallrye.reactive.messaging.rabbitmq;

import java.util.Optional;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.reactive.messaging.spi.ConnectorFactory;

/**
 * Extracts the common configuration for the {@code smallrye-rabbitmq} connector.
*/
 public class RabbitMQConnectorCommonConfiguration {
  protected final Config config;

  /**
   * Creates a new RabbitMQConnectorCommonConfiguration.
   */
  public RabbitMQConnectorCommonConfiguration(Config config) {
    this.config = config;
  }

  /**
   * @return the connector configuration
   */
  public Config config() {
    return this.config;
  }

  /**
   * Retrieves the value stored for the given alias.
   * @param alias the attribute alias, must not be {@code null} or blank
   * @param type the targeted type
   * @param <T> the targeted type
   * @return the configuration value for the given alias, empty if not set
   */
  protected <T> Optional<T> getFromAlias(String alias, Class<T> type) {
    return ConfigProvider.getConfig().getOptionalValue(alias, type);
  }

  /**
   * Retrieves the value stored for the given alias. Returns the default value if not present.
   * @param alias the attribute alias, must not be {@code null} or blank
   * @param type the targeted type
   * @param defaultValue the default value
   * @param <T> the targeted type
   * @return the configuration value for the given alias, empty if not set
   */
  protected <T> T getFromAliasWithDefaultValue(String alias, Class<T> type, T defaultValue) {
    return getFromAlias(alias, type).orElse(defaultValue);
  }

  /**
   * @return the channel name
   */
  public String getChannel() {
    return config.getValue(ConnectorFactory.CHANNEL_NAME_ATTRIBUTE, String.class);
  }

  /**
  * Gets the username value from the configuration.
  * Attribute Name: username
  * Description: The username used to authenticate to the broker
  * MicroProfile Config Alias: rabbitmq-username
  * Default Value: guest
  * @return the username
  */
  public String getUsername() {
    return config.getOptionalValue("username", String.class)
     .orElseGet(() -> getFromAliasWithDefaultValue("rabbitmq-username", String.class, "guest"));
  }

  /**
  * Gets the password value from the configuration.
  * Attribute Name: password
  * Description: The password used to authenticate to the broker
  * MicroProfile Config Alias: rabbitmq-password
  * Default Value: guest
  * @return the password
  */
  public String getPassword() {
    return config.getOptionalValue("password", String.class)
     .orElseGet(() -> getFromAliasWithDefaultValue("rabbitmq-password", String.class, "guest"));
  }

  /**
  * Gets the host value from the configuration.
  * Attribute Name: host
  * Description: The broker hostname
  * MicroProfile Config Alias: rabbitmq-host
  * Default Value: localhost
  * @return the host
  */
  public String getHost() {
    return config.getOptionalValue("host", String.class)
     .orElseGet(() -> getFromAliasWithDefaultValue("rabbitmq-host", String.class, "localhost"));
  }

  /**
  * Gets the url value from the configuration.
  * Attribute Name: url
  * Description: The broker uri
  * MicroProfile Config Alias: rabbitmq-url
  * @return the url
  */
  public Optional<String> getUrl() {
    Optional<String> maybe = config.getOptionalValue("url", String.class);
    if (maybe.isPresent()) { return maybe; }
    return getFromAlias("rabbitmq-url", String.class);
  }

  /**
  * Gets the queue value from the configuration.
  * Attribute Name: queue
  * Description: 
  * @return the queue
  */
  public Optional<String> getQueue() {
    return config.getOptionalValue("queue", String.class);
  }

  /**
  * Gets the port value from the configuration.
  * Attribute Name: port
  * Description: The broker port
  * MicroProfile Config Alias: rabbitmq-port
  * Default Value: 5672
  * @return the port
  */
  public Integer getPort() {
    return config.getOptionalValue("port", Integer.class)
     .orElseGet(() -> getFromAliasWithDefaultValue("rabbitmq-port", Integer.class, Integer.valueOf("5672")));
  }

  /**
  * Gets the virtual-host value from the configuration.
  * Attribute Name: virtual-host
  * Description: A comma separated virtualhost(s)
  * MicroProfile Config Alias: rabbitmq-virtual-host
  * @return the virtual-host
  */
  public Optional<String> getVirtualHost() {
    Optional<String> maybe = config.getOptionalValue("virtual-host", String.class);
    if (maybe.isPresent()) { return maybe; }
    return getFromAlias("rabbitmq-virtual-host", String.class);
  }

  /**
  * Gets the connection-retry value from the configuration.
  * Attribute Name: connection-retry
  * Description: The number of reconnection attempts
  * MicroProfile Config Alias: rabbitmq-connection-timeout
  * Default Value: 2
  * @return the connection-retry
  */
  public Integer getConnectionRetry() {
    return config.getOptionalValue("connection-retry", Integer.class)
     .orElseGet(() -> getFromAliasWithDefaultValue("rabbitmq-connection-timeout", Integer.class, Integer.valueOf("2")));
  }

  /**
  * Gets the connection-timeout value from the configuration.
  * Attribute Name: connection-timeout
  * Description: 
  * MicroProfile Config Alias: rabbitmq-reconnect-attempts
  * Default Value: 50000
  * @return the connection-timeout
  */
  public Integer getConnectionTimeout() {
    return config.getOptionalValue("connection-timeout", Integer.class)
     .orElseGet(() -> getFromAliasWithDefaultValue("rabbitmq-reconnect-attempts", Integer.class, Integer.valueOf("50000")));
  }

  /**
  * Gets the request-heartbeat value from the configuration.
  * Attribute Name: request-heartbeat
  * Description: 
  * MicroProfile Config Alias: rabbitmq-request-timeout
  * Default Value: 500
  * @return the request-heartbeat
  */
  public Integer getRequestHeartbeat() {
    return config.getOptionalValue("request-heartbeat", Integer.class)
     .orElseGet(() -> getFromAliasWithDefaultValue("rabbitmq-request-timeout", Integer.class, Integer.valueOf("500")));
  }

  /**
  * Gets the handshake-timeout value from the configuration.
  * Attribute Name: handshake-timeout
  * Description: 
  * MicroProfile Config Alias: rabbitmq-handshake-timeout
  * Default Value: 500
  * @return the handshake-timeout
  */
  public Integer getHandshakeTimeout() {
    return config.getOptionalValue("handshake-timeout", Integer.class)
     .orElseGet(() -> getFromAliasWithDefaultValue("rabbitmq-handshake-timeout", Integer.class, Integer.valueOf("500")));
  }

  /**
  * Gets the requested-channel-max value from the configuration.
  * Attribute Name: requested-channel-max
  * Description: 
  * MicroProfile Config Alias: rabbitmq-requested-channel-max
  * Default Value: 2047
  * @return the requested-channel-max
  */
  public Integer getRequestedChannelMax() {
    return config.getOptionalValue("requested-channel-max", Integer.class)
     .orElseGet(() -> getFromAliasWithDefaultValue("rabbitmq-requested-channel-max", Integer.class, Integer.valueOf("2047")));
  }

  /**
  * Gets the network-recovery-interval value from the configuration.
  * Attribute Name: network-recovery-interval
  * Description: 
  * MicroProfile Config Alias: rabbitmq-network-recovery-interval
  * Default Value: 500
  * @return the network-recovery-interval
  */
  public Integer getNetworkRecoveryInterval() {
    return config.getOptionalValue("network-recovery-interval", Integer.class)
     .orElseGet(() -> getFromAliasWithDefaultValue("rabbitmq-network-recovery-interval", Integer.class, Integer.valueOf("500")));
  }

  /**
  * Gets the automatic-recovery value from the configuration.
  * Attribute Name: automatic-recovery
  * Description: 
  * MicroProfile Config Alias: rabbitmq-automatic-recovery
  * Default Value: true
  * @return the automatic-recovery
  */
  public Boolean getAutomaticRecovery() {
    return config.getOptionalValue("automatic-recovery", Boolean.class)
     .orElseGet(() -> getFromAliasWithDefaultValue("rabbitmq-automatic-recovery", Boolean.class, Boolean.valueOf("true")));
  }

  public void validate() {
  }
}
