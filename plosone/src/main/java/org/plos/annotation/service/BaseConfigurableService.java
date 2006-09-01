/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.annotation.service;

import org.topazproject.authentication.ProtectedService;
import org.topazproject.authentication.ProtectedServiceFactory;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.MapConfiguration;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

import com.opensymphony.xwork.ActionContext;

/**
 * Base service class to be subclassed by any services which have common configuration requirements.
 */
public abstract class BaseConfigurableService {

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

  protected Map getSessionMap() {
    //TODO: get rid of the dependency on ActionContext
    return ActionContext.getContext().getSession();
  }

  /**
   * Create a configuration map initialized with the given config map
   * @param configMap configMap
   * @return a config map
   */
  protected MapConfiguration createMapConfiguration(final Map configMap) {
    return new MapConfiguration(configMap);
  }
}
