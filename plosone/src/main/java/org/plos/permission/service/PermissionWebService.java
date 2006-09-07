/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.permission.service;

import org.topazproject.authentication.ProtectedService;
import org.topazproject.ws.permissions.Permissions;
import org.topazproject.ws.permissions.PermissionsClientFactory;
import org.plos.service.BaseConfigurableService;

import javax.xml.rpc.ServiceException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.rmi.RemoteException;

/**
 * Wrapper on the topaz permission web service.
 */
public class PermissionWebService extends BaseConfigurableService {
  private Permissions permissionsService;
  private String currentPrincipal;

  public void init() throws IOException, URISyntaxException, ServiceException {
    final ProtectedService permissionProtectedService = createProtectedService(getConfiguration());
    permissionsService = PermissionsClientFactory.create(permissionProtectedService);
  }

  /**
   * @param resource resource
   * @return a list of grants for the current user/principal
   * @throws RemoteException
   */
  public String[] listGrants(final String resource) throws RemoteException {
    return listGrants(resource, getCurrentPrincipal());
  }

  /**
   * @see org.topazproject.ws.permissions.Permissions#listGrants(String, String)
   *
   * @param resource resource
   * @param principal principal
   * @return a list of grants
   * @throws java.rmi.RemoteException
   */
  private String[] listGrants(final String resource, final String principal) throws RemoteException {
    return permissionsService.listGrants(resource, principal);
  }

  /**
   * @return the current user's principle
   */
  public String getCurrentPrincipal() {
    return currentPrincipal;
  }

  /**
   * Set the current principle.
   * @param currentPrincipal currentPrincipal
   */
  public void setCurrentPrincipal(final String currentPrincipal) {
    this.currentPrincipal = currentPrincipal;
  }
}
