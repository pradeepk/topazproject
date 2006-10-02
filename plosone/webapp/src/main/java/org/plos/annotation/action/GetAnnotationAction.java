/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.annotation.action;

import com.opensymphony.xwork.validator.annotations.RequiredStringValidator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.annotation.service.Annotation;
import org.plos.ApplicationException;

/**
 * Used to fetch an annotation given an id.
 */
public class GetAnnotationAction extends AnnotationActionSupport {
  private String annotationId;
  private Annotation annotation;

  private static final Log log = LogFactory.getLog(GetAnnotationAction.class);

  public String execute() throws Exception {
    try {
      annotation = getAnnotationService().getAnnotation(annotationId);
    } catch (final ApplicationException e) {
      log.error(e, e);
      addActionError("Annotation fetching failed with error message: " + e.getMessage());
      return ERROR;
    }
    return SUCCESS;
  }

  /**
   * Set the annotationId for the annotation to fetch
   * @param annotationId annotationId
   */
  public void setAnnotationId(final String annotationId) {
    this.annotationId = annotationId;
  }

  @RequiredStringValidator(message = "Annotation Id is a required field")
  public String getAnnotationId() {
    return annotationId;
  }

  public Annotation getAnnotation() {
    return annotation;
  }
}
