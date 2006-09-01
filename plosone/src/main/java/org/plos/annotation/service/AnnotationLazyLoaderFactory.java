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

public class AnnotationLazyLoaderFactory {
  public AnnotationLazyLoader create(final String bodyUrl) {
    return new AnnotationLazyLoader(bodyUrl);
  }
}
