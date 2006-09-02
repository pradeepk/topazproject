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

  /**
   * @param bodyUrl bodyUrl
   * @return an instance of a lazy loader
   */
  public AnnotationLazyLoader create(final String bodyUrl) {
    return new AnnotationLazyLoader(bodyUrl);
  }
}
