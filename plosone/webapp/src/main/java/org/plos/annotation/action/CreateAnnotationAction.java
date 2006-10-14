/* $HeadURL::                                                                            $
 * $Id:CreateAnnotationAction.java 722 2006-10-02 16:42:45Z viru $
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
import org.plos.ApplicationException;
import org.plos.util.FileUtils;
import org.plos.util.ProfanityCheckingService;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

/**
 * Action to create an annotation. It also does profanity validation on the user content.
 */
public class CreateAnnotationAction extends AnnotationActionSupport {
  private String target;
  private String title;
  private String body;
  private String mimeType = "text/plain";
  private String annotationId;
  private boolean isPublic = false;
  private String startPath;
  private int startOffset;
  private String endPath;
  private int endOffset;
  private String olderAnnotation;

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
        annotationId = getAnnotationService().createAnnotation(target, getTargetContext(), olderAnnotation, title, mimeType, body, isPublic);
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
   * Returning an xpointer of the following form:
   * 1) start-point(string-range(id("x20060728a")/p[1],'',288,1))/range-to(end-point(string-range(id("x20060801a")/h3[1],'',39,1)))
   * 2) (later) string-range(/article[1]/body[1]/sec[1]/p[2],"",194,344)
   * @return the context for the annotation
   * @throws org.plos.ApplicationException ApplicationException
   */
  public String getTargetContext() throws ApplicationException {
    try {
      return target + "#xpointer" + URLEncoder.encode("(start-point(string-range(" + startPath + ",''," + startOffset + ",1))" +
              "/range-to(end-point(string-range(" + endPath + ",''," + endOffset + ",1))))", "UTF-8");
    } catch (final UnsupportedEncodingException e) {
      log.error(e);
      throw new ApplicationException(e);
    }
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

  /** @param isPublic set the visibility of annotation */
  public void setPublic(final boolean isPublic) {
    this.isPublic = isPublic;
  }

  /** @return whether the annotation is public */
  public boolean isPublic() {
    return isPublic;
  }

  /** @return the end point offset */
  public int getEndOffset() {
    return endOffset;
  }

  /** @param endOffset set the end point offset */
  public void setEndOffset(final int endOffset) {
    this.endOffset = endOffset;
  }

  /** @return return the end point path */
  @RequiredStringValidator(message="You must specify a value")
  public String getEndPath() {
    return endPath;
  }

  /** @param endPath set the end point path */
  public void setEndPath(final String endPath) {
    this.endPath = endPath;
  }

  /** @return the start point offset */
  public int getStartOffset() {
    return startOffset;
  }

  /** @param startOffset set the start point offset */
  public void setStartOffset(final int startOffset) {
    this.startOffset = startOffset;
  }

  /** @return the start point path */
  @RequiredStringValidator(message="You must specify a value")
  public String getStartPath() {
    return startPath;
  }

  /** @param startPath set start point path */
  public void setStartPath(final String startPath) {
    this.startPath = startPath;
  }

  /** @return the older annotation that it supersedes */
  public String getOlderAnnotation() {
    return olderAnnotation;
  }

  /** @param olderAnnotation the older annotation that it supersedes */
  public void setOlderAnnotation(String olderAnnotation) {
    this.olderAnnotation = olderAnnotation;
  }
}
