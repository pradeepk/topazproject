/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.annotation.web;

import com.opensymphony.xwork.ActionSupport;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.annotation.service.AnnotationService;
import org.plos.annotation.service.ApplicationException;

/**
 * Actions for working with annotations.
 */
public class CreateAction extends ActionSupport {
  private AnnotationService annotationService;
  private String target;
  private String context;
  private String title;
  private String body;
  private String mimeType = "text/plain";
  private String annotationId;

  public static final Log log = LogFactory.getLog(CreateAction.class);

  /**
   * Create annotation failed.
   * @return status
   * @throws Exception
   */
  public String execute() throws Exception {
    try {
      annotationId = annotationService.createAnnotation(target, context, title, mimeType, body);
    } catch (final ApplicationException e) {
      log.error(e, e);
      addActionError("Annotation creation failed with error message: " + e.getMessage());
      return ERROR;
    }
    return SUCCESS;
  }

  /**
   * TODO: move up to a parent class
   * Set the annotations service.
   * @param annotationService annotationService
   */
  public void setAnnotationService(final AnnotationService annotationService) {
    this.annotationService = annotationService;
  }

  /**
   * Set the target that it annotates.
   * @param target target
   */
  public void setTarget(final String target) {
    this.target = target;
  }

  /**
   * Set the context of the annotation
   * @param context context
   */
  public void setContext(final String context) {
    this.context = context;
  }

  /**
   * Set the title of the annotation
   * @param title title
   */
  public void setTitle(final String title) {
    this.title = title;
  }

  /**
   * Set the body of the annotation
   * @param body body
   */
  public void setBody(final String body) {
    this.body = body;
  }

  /**
   * Set the mimeType of the annotation
   * @param mimeType mimeType
   */
  public void setMimeType(final String mimeType) {
    this.mimeType = mimeType;
  }

  /**
   * Get the id of the newly created annotation
   * @return annotation id
   */
  public String getAnnotationId() {
    return annotationId;
  }
}
