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

import org.topazproject.ws.annotation.AnnotationInfo;
import org.topazproject.ws.annotation.ReplyInfo;
import org.plos.service.PermissionServiceGetter;

/**
 * Factory to return instances of lazy loaders as required by annotations and replies
 */
public class AnnotationLazyLoaderFactory {
  private PermissionServiceGetter permissionServiceGetter;

  /**
   * Create an instance of the AnnotationLazyLoader as required for each annotation
   * @param annotation annotation
   * @return an instance of a lazy loader
   */
  public AnnotationLazyLoader create(final AnnotationInfo annotation) {
    return new AnnotationLazyLoader(annotation.getBody(), annotation.getId(), permissionServiceGetter);
  }

  /**
   * Create an instance of the AnnotationLazyLoader as required for each reply
   * @param reply reply
   * @return an instance of a lazy loader
   */
  public AnnotationLazyLoader create(final ReplyInfo reply) {
    return new AnnotationLazyLoader(reply.getBody(), reply.getId(), permissionServiceGetter);
  }

  public void setPermissionServiceGetter(final PermissionServiceGetter permissionServiceGetter) {
    this.permissionServiceGetter = permissionServiceGetter;
  }
}
