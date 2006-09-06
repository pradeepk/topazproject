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

import org.plos.util.FileUtils;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.List;

/**
 * This is a worker class that would have/or be supplied the logic to retrieve functionality to fetch the values when requested for.
 * Fetch extra annotation properties on demand/lazily.
 * This will also cache the values already fetched.
 */
public class AnnotationLazyLoader {
  private String bodyContent;
  private final String bodyUrl;
  private static final String DELETE_GRANT = "delete";

  private static enum AnnotationVisibility {PUBLIC, PRIVATE, UNKNOWN}

  private AnnotationVisibility annotationVisibility = AnnotationVisibility.UNKNOWN;

  private PermissionWebService permissionWebService;
  private ResourcePropsForAuth resourcePropsForAuth;

  /**
   * @param bodyUrl bodyUrl
   * @param resourcePropsForAuth resourcePropsForAuth
   */
  public AnnotationLazyLoader(final String bodyUrl, final ResourcePropsForAuth resourcePropsForAuth) {
    this.bodyUrl = bodyUrl;
    this.permissionWebService = resourcePropsForAuth.getPermissionWebService();
  }

  public String getBody() throws ApplicationException {
    if (null == bodyContent) {
      bodyContent = getBodyContent(bodyUrl);
    }
    return bodyContent;
  }

  public boolean isPublicVisible() throws ApplicationException, RemoteException {
    if (AnnotationVisibility.UNKNOWN == annotationVisibility) {
      annotationVisibility = fetchAnnotationVisibility();
    }

    return AnnotationVisibility.PUBLIC == annotationVisibility;
  }

  /**
   * This method would be overridden or provided a command pattern to execute and return the result from the execution
   * @return whether the visibility of the annotation is public or private
   * @throws ApplicationException
   * @throws java.rmi.RemoteException
   */
  protected AnnotationVisibility fetchAnnotationVisibility() throws ApplicationException, RemoteException {
    final String[] grants = permissionWebService.listGrants(resourcePropsForAuth.getAnnotationUrl(), resourcePropsForAuth.getPrincipal());
    final List<String> grantsList = Arrays.asList(grants);

    if (grantsList.contains(DELETE_GRANT)) {
      return AnnotationVisibility.PRIVATE;
    }
    return AnnotationVisibility.PUBLIC;
  }

  public void setResourcePropsForAuth(final ResourcePropsForAuth resourcePropsForAuth) {
    this.resourcePropsForAuth = resourcePropsForAuth;
  }

  protected static String getBodyContent(final String bodyUrl) throws ApplicationException {
    try {
      return FileUtils.getTextFromUrl(bodyUrl);
    } catch (IOException e) {
      throw new ApplicationException(e);
    }
  }

}

