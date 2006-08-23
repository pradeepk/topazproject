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
 * Action class to delete a given reply.
 */
public class DeleteReplyAction extends ActionSupport {
  private String id;
  private String root;
  private String inReplyTo;

  public String deleteReplyWithId() throws Exception {
    return SUCCESS;
  }

  public String deleteReplyWithRootAndReplyTo() throws Exception {
    return SUCCESS;
  }

  public void setId(final String id) {
    this.id = id;
  }

  public void setRoot(final String root) {
    this.root = root;
  }

  public void setInReplyTo(final String inReplyTo) {
    this.inReplyTo = inReplyTo;
  }

  public String getId() {
    return id;
  }

  public String getRoot() {
    return root;
  }

  public String getInReplyTo() {
    return inReplyTo;
  }
}
