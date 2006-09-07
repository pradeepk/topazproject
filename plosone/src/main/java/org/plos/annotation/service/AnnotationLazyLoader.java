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
import org.plos.service.PermissionServiceGetter;
import org.plos.ApplicationException;
import org.plos.permission.service.PermissionWebService;

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
  private String resourceId;
  private PermissionServiceGetter permissionServiceGetter;

  private static enum AnnotationVisibility {PUBLIC, PRIVATE, UNKNOWN}
  private AnnotationVisibility annotationVisibility = AnnotationVisibility.UNKNOWN;

  /**
   * @param bodyUrl bodyUrl
   * @param resourceId resourceId
   * @param permissionServiceGetter permissionServiceGetter
   */
  public AnnotationLazyLoader(final String bodyUrl, final String resourceId, final PermissionServiceGetter permissionServiceGetter) {
    this.bodyUrl = bodyUrl;
    this.resourceId = resourceId;
    this.permissionServiceGetter = permissionServiceGetter;
  }

  public String getBody() throws ApplicationException {
    if (null == bodyContent) {
      bodyContent = getBodyContent(bodyUrl);
    }
    return bodyContent;
  }

  public boolean isPublicVisible() throws ApplicationException, RemoteException {
    annotationVisibility = fetchAnnotationVisibility();

    return AnnotationVisibility.PUBLIC == annotationVisibility;
  }

  /**
   * This method would be overridden or provided a command pattern to execute and return the result from the execution
   * @return whether the visibility of the annotation is public or private
   * @throws ApplicationException
   * @throws java.rmi.RemoteException
   */
  protected AnnotationVisibility fetchAnnotationVisibility() throws ApplicationException, RemoteException {
    final String[] grants = getPermissionWebService().listGrants(resourceId);
    final List<String> grantsList = Arrays.asList(grants);

    if (grantsList.contains(DELETE_GRANT)) {
      return AnnotationVisibility.PRIVATE;
    }
    return AnnotationVisibility.PUBLIC;
  }

  private PermissionWebService getPermissionWebService() {
    return permissionServiceGetter.getPermissionWebService();
  }

  protected static String getBodyContent(final String bodyUrl) throws ApplicationException {
    try {
      return FileUtils.getTextFromUrl(bodyUrl);
    } catch (IOException e) {
      throw new ApplicationException(e);
    }
  }

}

