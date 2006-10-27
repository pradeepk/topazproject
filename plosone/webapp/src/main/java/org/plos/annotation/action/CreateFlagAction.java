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
import org.plos.ApplicationException;

/**
 * Create a flag for a given annotation or reply
 */
public class CreateFlagAction extends AnnotationActionSupport {
  private String target;
  private String comment;
  private String mimeType = "text/plain";
  private String annotationId;
  private String reasonCode;

  public static final Log log = LogFactory.getLog(CreateAnnotationAction.class);

  /**
   * {@inheritDoc}
   * Create a flag for a given annotation or reply
   */
  public String execute() throws Exception {
    try {
      annotationId = getAnnotationService().createFlag(target, reasonCode, comment, mimeType);
    } catch (final ApplicationException e) {
      log.error(e, e);
      addActionError("Flag creation failed with error message: " + e.getMessage());
      return ERROR;
    }
    addActionMessage("Flag created with id:" + annotationId);
    return SUCCESS;
  }

  /**
   * @return the target
   */
  @RequiredStringValidator(message="You must specify the target annotation/reply for this flag comment")
  public String getTarget() {
    return target;
  }

  /**
   * Set the target that it annotates.
   * @param target target
   */
  public void setTarget(final String target) {
    this.target = target;
  }

  /**
   * @return the annotation content
   */
  @RequiredStringValidator(message="You must say something in your flag comment")
  public String getComment() {
    return comment;
  }

  /**
   * Set the comment of the annotation
   * @param comment comment
   */
  public void setComment(final String comment) {
    this.comment = comment;
  }

  /**
   * @return the reason code for the flagging
   */
  @RequiredStringValidator(message="You must specify the reason code for this flag comment")
  public String getReasonCode() {
    return reasonCode;
  }

  /**
   * Set the reason code for this flag
   * @param reasonCode reasonCode
   */
  public void setReasonCode(final String reasonCode) {
    this.reasonCode = reasonCode;
  }

  /**
   * Get the id of the newly created annotation
   * @return annotation id
   */
  public String getAnnotationId() {
    return annotationId;
  }

}
