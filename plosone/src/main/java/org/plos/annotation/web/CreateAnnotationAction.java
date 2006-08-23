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
import com.opensymphony.xwork.validator.annotations.RequiredStringValidator;
import com.opensymphony.xwork.validator.annotations.ValidatorType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.annotation.service.AnnotationService;
import org.plos.annotation.service.ApplicationException;
import org.plos.util.FileUtils;
import org.plos.util.ProfanityCheckingService;
import org.plos.util.UserInputSecurityCheckingService;

import java.util.List;

/**
 * Actions for working with annotations.
 */
public class CreateAnnotationAction extends ActionSupport {
  private AnnotationService annotationService;
  private String target;
  private String targetContext;
  private String title;
  private String body;
  private String mimeType = "text/plain";
  private String annotationId;

  private ProfanityCheckingService profanityCheckingService;
  private UserInputSecurityCheckingService userInputSecurityCheckingService;

  public static final Log log = LogFactory.getLog(CreateAnnotationAction.class);

  /**
   * Create annotation failed.
   * @return status
   * @throws Exception
   */
  public String execute() throws Exception {
    try {
      final List<String> profanityValidationMessages = profanityCheckingService.validate(body);
      final List<String> securityValidationMessages = userInputSecurityCheckingService.escape(body);

      if (profanityValidationMessages.isEmpty() && securityValidationMessages.isEmpty() ) {
        annotationId = annotationService.createAnnotation(target, targetContext, title, mimeType, body);
      } else {
        addMessages(profanityValidationMessages, "profanity check", "body");
        addMessages(securityValidationMessages, "security check", "body");
        return ERROR;
      }
    } catch (final ApplicationException e) {
      log.error(e, e);
      addActionError("Annotation creation failed with error message: " + e.getMessage());
      return ERROR;
    }
    addActionMessage("Annotation created with id:" + annotationId);
    return SUCCESS;
  }

  private void addMessages(final List<String> validationMessages, final String checkType, final String fieldName) {
    if (!validationMessages.isEmpty()) {
      final StringBuilder sb = new StringBuilder();
      for (final String message : validationMessages) {
        sb.append(message).append(FileUtils.NEW_LINE);
      }
      addFieldError(fieldName, "Annotation creation failed " + checkType + " with following messages: " + sb.toString().trim());
    }
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
   * @param targetContext targetContext
   */
  public void setTargetContext(final String targetContext) {
    this.targetContext = targetContext;
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

  /**
   * @return the target
   */
  @RequiredStringValidator(type= ValidatorType.FIELD, fieldName= "target", message="You must specify the target that this annotation is applied on")
  public String getTarget() {
    return target;
  }

  /**
   * @return the context for the annotation
   */
  @RequiredStringValidator(type= ValidatorType.FIELD, fieldName= "targetContext", message="You must specify the context for this annotation")
  public String getTargetContext() {
    return targetContext;
  }

  /**
   * @return the title
   */
  public String getTitle() {
    return title;
  }

  /**
   * @return the annotation content
   */
  @RequiredStringValidator(type= ValidatorType.FIELD, fieldName= "body", message="You must say something in your annotation")
  public String getBody() {
    return body;
  }

  public String getMimeType() {
    return mimeType;
  }

  public void setProfanityCheckingService(final ProfanityCheckingService profanityCheckingService) {
    this.profanityCheckingService = profanityCheckingService;
  }

  public void setUserInputSecurityCheckingService(final UserInputSecurityCheckingService userInputSecurityCheckingService) {
    this.userInputSecurityCheckingService = userInputSecurityCheckingService;
  }
}
