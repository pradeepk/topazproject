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

/**
 * A parameter object that is used for collecting parameters required for finding any authentication properties for a given resource.
 */
public class ResourcePropsForAuth {
  private final String annotationUrl;
  private final String principal;
  private final PermissionWebService permissionWebService;

  public ResourcePropsForAuth(final String annotationUrl, final String principal, final PermissionWebService permissionWebService) {
    this.annotationUrl = annotationUrl;
    this.principal = principal;
    this.permissionWebService = permissionWebService;
  }

  public String getAnnotationUrl() {
    return annotationUrl;
  }

  public String getPrincipal() {
    return principal;
  }

  public PermissionWebService getPermissionWebService() {
    return permissionWebService;
  }
}
