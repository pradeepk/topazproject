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

import org.plos.permission.service.PermissionWebService;

/**
 * Gets the correct instance of the permission service for the current user session.
 * Wired in correctly by spring IOC
 */
public class PermissionServiceGetter {
  private PermissionWebService permissionWebService;

  /**
   * @return the user specific instance of the PermissionWebService.
   */
  public PermissionWebService getPermissionWebService() {
    return permissionWebService;
  }

  /**
   * Set the permissionWebService
   * @param permissionWebService permissionWebService
   */
  public void setPermissionWebService(final PermissionWebService permissionWebService) {
    this.permissionWebService = permissionWebService;
  }
}
