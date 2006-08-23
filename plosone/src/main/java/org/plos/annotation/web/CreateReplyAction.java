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

/**
 * Action for creating a reply.
 */
public class CreateReplyAction extends ActionSupport {
  private String id;
  private String root;
  private String inReplyTo;
  private String title;
  private String mimeType;
  private String body;

  public String execute() throws Exception {
    return SUCCESS;
  }

  public String getReplyId() {
    return id;
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

  
}
