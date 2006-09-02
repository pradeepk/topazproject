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

import com.opensymphony.xwork.validator.annotations.RequiredStringValidator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.annotation.service.ApplicationException;
import org.plos.util.FileUtils;
import org.plos.util.ProfanityCheckingService;

import java.util.List;

/**
 * Action to create an annotation. It also does profanity validation on the user content.
 */
public class CreateAnnotationAction extends AnnotationActionSupport {
  private String target;
  private String targetContext;
  private String title;
  private String body;
  private String mimeType = "text/plain";
  private String annotationId;
  private ProfanityCheckingService profanityCheckingService;

  public static final Log log = LogFactory.getLog(CreateAnnotationAction.class);

  /**
   * {@inheritDoc}
   * Also does some profanity check for title and body before creating the annotation.
   */
  public String execute() throws Exception {
    try {
      final List<String> profanityValidationMessagesInTitle = profanityCheckingService.validate(title);
      final List<String> profanityValidationMessagesInBody = profanityCheckingService.validate(body);

      if (profanityValidationMessagesInBody.isEmpty() && profanityValidationMessagesInTitle.isEmpty()) {
        annotationId = getAnnotationService().createAnnotation(target, targetContext, title, mimeType, body);
      } else {
        addMessages(profanityValidationMessagesInBody, "profanity check", "body");
        addMessages(profanityValidationMessagesInTitle, "profanity check", "title");
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

  private void addMessages(final List<String> messages, final String checkType, final String fieldName) {
    if (!messages.isEmpty()) {
      final StringBuilder sb = new StringBuilder();
      for (final String message : messages) {
        sb.append(message).append(FileUtils.NEW_LINE);
      }
      addFieldError(fieldName, "Annotation creation failed " + checkType + " with following messages: " + sb.toString().trim());
    }
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
  @RequiredStringValidator(message="You must specify the target that this annotation is applied on")
  public String getTarget() {
    return target;
  }

  /**
   * @return the context for the annotation
   */
  @RequiredStringValidator(message="You must specify the context for this annotation")
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
  @RequiredStringValidator(message="You must say something in your annotation")
  public String getBody() {
    return body;
  }

  /**
   * @return the mime type
   */
  public String getMimeType() {
    return mimeType;
  }

  /**
   * Set the profanityCheckingService
   * @param profanityCheckingService profanityCheckingService
   */
  public void setProfanityCheckingService(final ProfanityCheckingService profanityCheckingService) {
    this.profanityCheckingService = profanityCheckingService;
  }
}
