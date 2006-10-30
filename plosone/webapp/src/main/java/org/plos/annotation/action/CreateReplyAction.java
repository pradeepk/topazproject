/* $HeadURL::                                                                            $
 * $Id:CreateReplyAction.java 722 2006-10-02 16:42:45Z viru $
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

import java.util.List;

/**
 * Action for creating a reply.
 */
public class CreateReplyAction extends AnnotationActionSupport {
  private String replyId;
  private String root;
  private String inReplyTo;
  private String title;
  private String mimeType = "text/plain";
  private String body;

  private ProfanityCheckingService profanityCheckingService;

  private static final Log log = LogFactory.getLog(CreateReplyAction.class);

  public String execute() throws Exception {
    try {
      final List<String> profanityValidationMessagesInTitle = profanityCheckingService.validate(title);
      final List<String> profanityValidationMessagesInBody = profanityCheckingService.validate(body);

      if (profanityValidationMessagesInBody.isEmpty() && profanityValidationMessagesInTitle.isEmpty()) {
        replyId = getAnnotationService().createReply(root, inReplyTo, title, mimeType, body);
      } else {
        addMessages(profanityValidationMessagesInBody, "profanity check", "body");
        addMessages(profanityValidationMessagesInTitle, "profanity check", "title");
        return ERROR;
      }
    } catch (final ApplicationException e) {
      log.error(e, e);
      addActionError("Reply creation failed with error message: " + e.getMessage());
      return ERROR;
    }
    addActionMessage("Reply created with id:" + replyId);

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

  public String getReplyId() {
    return replyId;
  }

  public void setRoot(final String root) {
    this.root = root;
  }

  public void setInReplyTo(final String inReplyTo) {
    this.inReplyTo = inReplyTo;
  }

  public void setTitle(final String title) {
    this.title = title;
  }

  public void setMimeType(final String mimeType) {
    this.mimeType = mimeType;
  }

  public void setBody(final String body) {
    this.body = body;
  }

  @RequiredStringValidator(message = "The annotation id to which it applies is required")
  public String getRoot() {
    return root;
  }

  @RequiredStringValidator(message = "The annotation/reply id to which it applies is required")
  public String getInReplyTo() {
    return inReplyTo;
  }

  public String getTitle() {
    return title;
  }

  @RequiredStringValidator(message = "The comment/body is required")
  public String getBody() {
    return body;
  }

  public void setProfanityCheckingService(final ProfanityCheckingService profanityCheckingService) {
    this.profanityCheckingService = profanityCheckingService;
  }

}
