/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.plos.annotation.action;

import com.opensymphony.xwork2.validator.annotations.RequiredStringValidator;
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

  private static final Log log = LogFactory.getLog(CreateFlagAction.class);

  /**
   * Flag a Rating.
   *
   * @return WebWorks SUCCESS or ERROR.
   */
  public String createRatingFlag() {
    try {
      annotationId = getAnnotationService().createRatingFlag(target, reasonCode, comment, mimeType);
    } catch (final ApplicationException e) {
      log.error("Could not create flag for target: " + target, e);
      addActionError("Flag creation failed with error message: " + e.getMessage());
      return ERROR;
    }
    addActionMessage("Flag created with id:" + annotationId);
    return SUCCESS;
  }

  /**
   * Create a flag for a given annotation
   */
  public String createAnnotationFlag() throws Exception {
    return createFlag(true);
  }

  /**
   * Create a flag for a given reply
   */
  public String createReplyFlag() throws Exception {
    return createFlag(false);
  }

  private String createFlag(final boolean isAnnotation) {
    try {
      annotationId = getAnnotationService().createFlag(target, reasonCode, comment, mimeType, isAnnotation);
    } catch (final ApplicationException e) {
      log.error("Could not create flag for target: " + target, e);
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
