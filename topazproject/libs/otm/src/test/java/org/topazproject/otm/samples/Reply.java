/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.otm.samples;

import org.topazproject.mulgara.itql.ItqlHelper;

import org.topazproject.otm.annotations.Id;
import org.topazproject.otm.annotations.Model;
import org.topazproject.otm.annotations.Ns;
import org.topazproject.otm.annotations.Rdf;

/**
 * Reply meta-data.
 *
 * @author Pradeep Krishnan
 */
@Rdf(Reply.NS + "Reply")
@Ns(Reply.NS)
public class Reply extends Annotia {
  public static final String NS = "http://www.w3.org/2001/03/thread#";
  private String                                   root;
  private String                                   inReplyTo;

  /**
   * Creates a new Reply object.
   */
  public Reply() {
  }

  public Reply(String id) {
    super(id);
  }
  /**
   * Get root.
   *
   * @return root as String.
   */
  public String getRoot() {
    return root;
  }

  /**
   * Set root.
   *
   * @param root the value to set.
   */
  public void setRoot(String root) {
    this.root = root;
  }

  /**
   * Get inReplyTo.
   *
   * @return inReplyTo as String.
   */
  public String getInReplyTo() {
    return inReplyTo;
  }

  /**
   * Set inReplyTo.
   *
   * @param inReplyTo the value to set.
   */
  public void setInReplyTo(String inReplyTo) {
    this.inReplyTo = inReplyTo;
  }

}
