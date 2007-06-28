/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.configuration;

import java.net.URL;
import java.net.MalformedURLException;
import java.util.Enumeration;
import java.io.IOException;
import java.io.FileNotFoundException;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.ConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A singleton that manages the load/unload/re-load of Configuration.<p>
 *
 * Configuration consists of a layered set of configuration files where configuration
 * in a higher layer overrides those of the lower layers. Starting from the lowest layer,
 * configuration consists of:
 * <ul>
 *   <li>/global-defaults.xml - A resource in this library
 *   <li>/defaults.xml - A resource or resources on libraries and webapps using this lib
 *   <li>/etc/topaz/plosone.xml - A set of user overrides in /etc. The name of this file
 *        can be changed for webapps that use WebAppInitializer by changing web.xml or
 *        by setting the org.plos.configuraiton system property.
 * </ul>
 *
 * TODO: Document dev-mode system property (when it is implemented)
 *
 * @author Pradeep Krishnan
 * @author Eric Brown
 */
public class ConfigurationStore {
  private static final Log                log = LogFactory.getLog(ConfigurationStore.class);
  private static final ConfigurationStore instance      = new ConfigurationStore();
  private Configuration                   configuration = null;

  /**
   * A property used to define the location of the master set of configuration overrides.
   * This is usually a xml or properties file in /etc somewhere. Note that this must be
   * a URL. (For example: file:///etc/topaz/plosone.xml.)
   */
  public static final String CONFIG_URL = "org.plos.configuration";

  /**
   * Default configuration overrides in /etc
   */
  public static final String DEFAULT_CONFIG_URL = "file:///etc/topaz/plosone.xml";

  /**
   * Name of resource(s) that contain defaults in a given library or web application.<p>
   *
   * Note that multiple copies of these may exist in the same classpath. ALL are read
   * and added to the configuration. It is assumed that developers use appropriate
   * namespaces to avoid collisions.<p>
   *
   * Also note that this does not begin with a / as ClassLoader.getResources() does
   * not want this to begin with a /.
   */
  public static final String DEFAULTS_RESOURCE = "defaults.xml";

  /**
   * The name of the global defaults that exist in this library.<p>
   *
   * It is assumed there is only one of these in the classpath. If somebody defines
   * a second copy of this, the results are undefined. (TODO: Detect this.)
   */
  public static final String GLOBAL_DEFAULTS_RESOURCE = "/global-defaults.xml";

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
   *
   * @throws RuntimeException if the configuration factory is not initialized
   */
  public Configuration getConfiguration() {
    if (configuration != null)
      return configuration;

    throw new RuntimeException("ERROR: Configuration not loaded or initialized.");
  }

  /**
   * Load/Reload the configuration from the factory config url.
   *
   * @param configURL URL to the config file for ConfigurationFactory
   * @throws ConfigurationException when the config factory configuration has an error
   */
  public void loadConfiguration(URL configURL) throws ConfigurationException {
    CompositeConfiguration composite = new CompositeConfiguration();

    // TODO: Detect and handle "dev" mode

    // Load from /etc/... (optional)
    if (configURL != null) {
      Configuration config = null;
      try {
        if (configURL.getFile().endsWith("properites"))
          config = new PropertiesConfiguration(configURL);
        else
          config = new XMLConfiguration(configURL);

        composite.addConfiguration(config);
      } catch (ConfigurationException ce) {
        if (!(ce.getCause() instanceof FileNotFoundException))
          throw ce;
        log.info("Unable to open '" + configURL + "'");
      }
    }

    // Add defaults.xml found in classpath
    try {
      Enumeration<URL> rs = getClass().getClassLoader().getResources(DEFAULTS_RESOURCE);
      while(rs.hasMoreElements())
        composite.addConfiguration(new XMLConfiguration(rs.nextElement()));
    } catch (IOException ioe) {
      // Don't understand how this could ever happen
      throw new Error("Unexpected error", ioe);
    }

    // Add global-defaults.xml (presumably found in this jar)
    composite.addConfiguration(new XMLConfiguration(
                                 getClass().getResource(GLOBAL_DEFAULTS_RESOURCE)));

    configuration = composite;
  }

  /**
   * Use the default commons configuration specified by this library.
   *
   * @throws ConfigurationException when the configuration can't be found.
   */
  public void loadDefaultConfiguration() throws ConfigurationException {
    // Allow JVM level property to override everything else
    String name = System.getProperty(CONFIG_URL);
    if (name == null)
      name = DEFAULT_CONFIG_URL;

    try {
      loadConfiguration(new URL(name));
    } catch (MalformedURLException e) {
      throw new ConfigurationException("Invalid value of '" + name + "' for '" + CONFIG_URL +
                                       "'. Must be a valid URL.");
    }
  }

  /**
   * Unload the current configuration.
   */
  public void unloadConfiguration() {
    configuration = null;
  }
}
