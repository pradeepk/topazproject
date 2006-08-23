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
import org.topazproject.ws.annotation.ReplyInfo;

/**
 * Action class to get a list of replies to annotations.
 */
public class ListReplyAction extends ActionSupport {
  private String root;
  private String inReplyTo;
  private ReplyInfo[] replies;

  public String execute() throws Exception {
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
}
