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
import org.topazproject.ws.annotation.ReplyInfo;

/**
 * Action class to get a list of replies to annotations.
 */
public class ListReplyAction extends AnnotationActionSupport {
  private String root;
  private String inReplyTo;
  private ReplyInfo[] replies;

  private static final Log log = LogFactory.getLog(ListReplyAction.class);
  public String execute() throws Exception {
    try {
      replies = getAnnotationService().listReplies(root, inReplyTo);
    } catch (final ApplicationException e) {
      log.error(e, e);
      addActionError("Annotation fetching failed with error message: " + e.getMessage());
      return ERROR;
    }
    return SUCCESS;
  }

  public void setRoot(final String root) {
    this.root = root;
  }

  public void setInReplyTo(final String inReplyTo) {
    this.inReplyTo = inReplyTo;
  }

  public ReplyInfo[] getReplies() {
    return replies;
  }

  @RequiredStringValidator(message = "root is required")
  public String getRoot() {
    return root;
  }

  @RequiredStringValidator(message = "InReplyTo is required")
  public String getInReplyTo() {
    return inReplyTo;
  }
}
