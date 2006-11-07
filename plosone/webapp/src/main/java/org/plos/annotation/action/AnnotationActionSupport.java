/* $HeadURL::                                                                            $
 * $Id:AnnotationActionSupport.java 722 2006-10-02 16:42:45Z viru $
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.annotation.action;

import org.plos.action.BaseActionSupport;
import org.plos.annotation.service.AnnotationService;
import org.plos.user.service.UserService;

/**
 * To be subclassed by Action classes for Annotations and Replies that can use common stuff among them
 */
public abstract class AnnotationActionSupport extends BaseActionSupport {
  private AnnotationService annotationService;
  private UserService userService;

  /**
   * Set the annotations service.
   * @param annotationService annotationService
   */
  public final void setAnnotationService(final AnnotationService annotationService) {
    this.annotationService = annotationService;
  }

  /**
   * Note: The visibility of this method is default so that the JSON serializer does not get into
   * an infinite circular loop when trying to serialize the action. 
   * @return the AnnotationService
   */
  final AnnotationService getAnnotationService() {
    return annotationService;
  }

  /**
   * @return the user service
   */
  public UserService getUserService() {
    return userService;
  }

  /**
   * Set the user service
   * @param userService userService
   */
  public void setUserService(final UserService userService) {
    this.userService = userService;
  }
}
