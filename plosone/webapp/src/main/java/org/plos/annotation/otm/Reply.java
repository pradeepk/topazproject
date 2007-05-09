/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.annotation.otm;

import java.net.URI;
import org.topazproject.otm.annotations.Embedded;
import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.UriPrefix;

/**
 * Reply meta-data.
 *
 * @author Pradeep Krishnan
 */
@Entity(type = Reply.NS + "Reply")
@UriPrefix(Reply.NS)
public class Reply extends Annotea {
  public static final String NS = "http://www.w3.org/2001/03/thread#";
  private URI  root;
  private URI  inReplyTo;
  private StreamableBody body;
  
  /**
   * Creates a new Reply object.
   */
  public Reply() {
  }

  public Reply(URI id) {
    super(id);
  }
  /**
   * Get root.
   *
   * @return root as URI.
   */
  public URI getRoot() {
    return root;
  }

  /**
   * Set root.
   *
   * @param root the value to set.
   */
  public void setRoot(URI root) {
    this.root = root;
  }

  /**
   * Get inReplyTo.
   *
   * @return inReplyTo as URI.
   */
  public URI getInReplyTo() {
    return inReplyTo;
  }

  /**
   * Set inReplyTo.
   *
   * @param inReplyTo the value to set.
   */
  public void setInReplyTo(URI inReplyTo) {
    this.inReplyTo = inReplyTo;
  }

  /**
   * @return Returns the body.
   */
  public AnnotationBody getBody() {
    return body;
  }

  /**
   * @param body The body to set.
   */
  public void setBody(AnnotationBody body) {
    this.body = (StreamableBody)body;
  }
}
