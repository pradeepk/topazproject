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

import org.apache.commons.configuration.MapConfiguration;
import org.topazproject.authentication.ProtectedService;

import javax.xml.rpc.ServiceException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Map;

/**
 * Wrapper on the topaz permission web service.
 */
public class PermissionWebService extends BaseConfigurableService {
  private MapConfiguration permissionConfiguration;

  public void init() throws IOException, URISyntaxException {
    final ProtectedService permissionProtectedService = createProtectedService(this.permissionConfiguration);
//    Permissions service = PermissionsClientFactory.create(permissionProtectedService);
  }

  /**
   * Set the permission service configuration.
   * @param configMap configMap
   * @throws java.net.MalformedURLException
     * @throws javax.xml.rpc.ServiceException
     */
  public void setPermissionServiceConfiguration(final Map configMap) throws MalformedURLException, ServiceException {
    permissionConfiguration = new MapConfiguration(configMap);
  }

}
