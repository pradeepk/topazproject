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
 * Factory to return instances of lazy loaders as required by annotations and replies
 */
public class AnnotationLazyLoaderFactory {
  private PermissionWebService permissionWebService;

  /**
   * Create an instance of the AnnotationLazyLoader as requied for each annotation
   * @param bodyUrl bodyUrl
   * @param annotationUrl annotationUrl
   * @param principal principal
   * @return an instance of a lazy loader
   */
  public AnnotationLazyLoader create(final String bodyUrl, final String annotationUrl, final String principal) {
    final ResourcePropsForAuth resourcePropsForAuth = new ResourcePropsForAuth(annotationUrl, principal, permissionWebService);
    return new AnnotationLazyLoader(bodyUrl, resourcePropsForAuth);
  }

  public void setPermissionWebService(final PermissionWebService permissionWebService) {
    this.permissionWebService = permissionWebService;
  }
}
