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

/**
 * Fetch extra annotation properties on demand/lazily.
 */
public class AnnotationLazyLoader {
  private String bodyContent;
  private AnnotationVisibility annotationVisibility = AnnotationVisibility.UNKNOWN;
  private final String bodyUrl;

  public AnnotationLazyLoader(final String bodyUrl) {
    this.bodyUrl = bodyUrl;
  }

  public String getBody() throws ApplicationException {
    if (null == bodyContent) {
      bodyContent = getBodyContent(bodyUrl);
    }
    return bodyContent;
  }

  public AnnotationVisibility getVisibility() throws ApplicationException {
    if (AnnotationVisibility.UNKNOWN == annotationVisibility) {
      annotationVisibility = fetchAnnotationVisibility();
    }
    return annotationVisibility;
  }

  public AnnotationVisibility fetchAnnotationVisibility() throws ApplicationException {
    return null;
  }

  protected static String getBodyContent(final String bodyUrl) throws ApplicationException {
    try {
      return FileUtils.getTextFromUrl(bodyUrl);
    } catch (IOException e) {
      throw new ApplicationException(e);
    }
  }

}

enum AnnotationVisibility {
  PUBLIC, PRIVATE, UNKNOWN
}
