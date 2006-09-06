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
import org.topazproject.ws.permissions.Permissions;
import org.topazproject.ws.permissions.PermissionsClientFactory;

import javax.xml.rpc.ServiceException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.rmi.RemoteException;

/**
 * Wrapper on the topaz permission web service.
 */
public class PermissionWebService extends BaseConfigurableService {
  private Permissions permissionsService;

  public void init() throws IOException, URISyntaxException, ServiceException {
    final ProtectedService permissionProtectedService = createProtectedService(getConfiguration());
    permissionsService = PermissionsClientFactory.create(permissionProtectedService);
  }

  /**
   * @see org.topazproject.ws.permissions.Permissions#listGrants(String, String)
   *
   * @param resource resource
   * @param principal principal
   * @return a list of grants
   * @throws java.rmi.RemoteException
   */
  public String[] listGrants(String resource, String principal) throws RemoteException {
    return permissionsService.listGrants(resource, principal);
  }
}
