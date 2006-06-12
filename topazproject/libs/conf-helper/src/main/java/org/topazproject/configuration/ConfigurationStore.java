package org.topazproject.configuration;

import java.net.URL;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.ConfigurationFactory;

/**
 * A singleton that manages the load/unload/re-load of Configuration.
 *
 * @author Pradeep Krishnan
 */
public class ConfigurationStore {
  private static final ConfigurationStore instance      = new ConfigurationStore();
  private Configuration                   configuration = null;

  /**
   * Create the singleton instance.
   */
  private ConfigurationStore() {
  }

  /**
   * Gets the singleton instance.
   *
   * @return Returns the only instance.
   */
  public static ConfigurationStore getInstance() {
    return instance;
  }

  /**
   * Gets the current configuration root.
   *
   * @return Returns the currently loaded configuration root
   */
  public Configuration getConfiguration() {
    return configuration;
  }

  /**
   * Load/Reload the configuration from the factory config url.
   *
   * @param configURL URL to the config file for ConfigurationFactory
   *
   * @throws ConfigurationException when the config factory configuration has an error
   */
  public void loadConfiguration(URL configURL) throws ConfigurationException {
    ConfigurationFactory factory = new ConfigurationFactory();

    factory.setConfigurationURL(configURL);

    configuration = factory.getConfiguration();
  }

  /**
   * Unload the current configuration.
   */
  public void unloadConfiguration() {
    configuration = null;
  }
}
