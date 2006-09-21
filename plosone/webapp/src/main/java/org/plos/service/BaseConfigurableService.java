/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.service;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.MapConfiguration;
import org.plos.web.UserContext;
import org.topazproject.authentication.ProtectedService;
import org.topazproject.authentication.ProtectedServiceFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

/**
 * Base service class to be subclassed by any services which have common configuration requirements.
 */
public abstract class BaseConfigurableService {
  private Configuration configuration;
  private UserContext userContext;

  /**
   * @param configuration configuration
   * @return an instance of protected service
   * @throws IOException
   * @throws URISyntaxException
   */
  protected ProtectedService createProtectedService(final Configuration configuration) throws IOException, URISyntaxException {
    return ProtectedServiceFactory.createService(configuration, getSessionMap());
  }

  /**
   * @return session variables in a map
   */
  public Map getSessionMap() {
    return userContext.getSessionMap();
  }

  /**
   * Set the user's context which can be used to obtain user's session values/attributes
   * @param userContext userContext
   */
  public void setUserContext(final UserContext userContext) {
    this.userContext = userContext;
  }

  /**
   * @return get user context
   */
  public UserContext getUserContext() {
    return userContext;
  }

  /**
   * Create a configuration map initialized with the given config map
   * @param configMap configMap
   * @return a config map
   */
  protected MapConfiguration createMapConfiguration(final Map configMap) {
    return new MapConfiguration(configMap);
  }

  /**
   * @return the configuration info
   */
  public Configuration getConfiguration() {
    return this.configuration;
  }

  /**
   * Set the initial configuration properties
   * @param configMap configMap
   */
  public void setConfigurationMap(final Map configMap) {
    configuration = createMapConfiguration(configMap);
  }

}
