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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.annotation.service.ApplicationException;

/**
 * Action class to delete a given reply.
 */
public class DeleteReplyAction extends AnnotationActionSupport {
  private String id;
  private String root;
  private String inReplyTo;

  private static final Log log = LogFactory.getLog(DeleteReplyAction.class);

  public String deleteReplyWithId() throws Exception {
    try {
      getAnnotationService().deleteReply(id);
    } catch (final ApplicationException e) {
      log.error(e, e);
      addActionError("Reply deletion failed with error message: " + e.getMessage());
      return ERROR;
    }
    return SUCCESS;
  }

  public String deleteReplyWithRootAndReplyTo() throws Exception {
    try {
      getAnnotationService().deleteReply(root, inReplyTo);
    } catch (final ApplicationException e) {
      log.error(e, e);
      addActionError("Reply deletion failed with error message: " + e.getMessage());
      return ERROR;
    }
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
