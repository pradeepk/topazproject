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

import java.util.ArrayList;
import java.util.List;

import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.Predicate;
import org.topazproject.otm.annotations.Rdf;

/**
 * AbstractAnnotation meta-data.
 *
 * @author Pradeep Krishnan
 */
@Entity(type = Annotea.NS + "Annotation")
public abstract class AbstractAnnotation extends Annotea {
  private URI                                                                       annotates;
  private String                                                                    context;
  @Predicate(uri = Rdf.rdf + "type")
  private String type;
  @Predicate(uri = Rdf.dc_terms + "replaces")
  private AbstractAnnotation                                                                supersedes;
  @Predicate(uri = Rdf.dc_terms + "isReplacedBy")
  private AbstractAnnotation                                                                supersededBy;
  @Predicate(uri = Reply.NS + "inReplyTo", inverse = true)
  private List<ReplyThread>                                                         replies =
    new ArrayList<ReplyThread>();

/**
   * Creates a new AbstractAnnotation object.
   */
  public AbstractAnnotation() {
  }

/**
   * Creates a new AbstractAnnotation object.
   */
  public AbstractAnnotation(URI id) {
    super(id);
  }

  /**
   * Get annotates.
   *
   * @return annotates as Uri.
   */
  public URI getAnnotates() {
    return annotates;
  }

  /**
   * Set annotates.
   *
   * @param annotates the value to set.
   */
  public void setAnnotates(URI annotates) {
    this.annotates = annotates;
  }

  /**
   * Get context.
   *
   * @return context as String.
   */
  public String getContext() {
    return context;
  }

  /**
   * Set context.
   *
   * @param context the value to set.
   */
  public void setContext(String context) {
    this.context = context;
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public List<ReplyThread> getReplies() {
    return replies;
  }

  /**
   * DOCUMENT ME!
   *
   * @param replies DOCUMENT ME!
   */
  public void setReplies(List<ReplyThread> replies) {
    this.replies = replies;
  }

  /**
   * DOCUMENT ME!
   *
   * @param r DOCUMENT ME!
   */
  public void addReply(ReplyThread r) {
    r.setRoot(getId());
    r.setInReplyTo(getId());
    replies.add(r);
  }

  /**
   * Get supersedes.
   *
   * @return supersedes.
   */
  public AbstractAnnotation getSupersedes() {
    return supersedes;
  }

  /**
   * Set supersedes.
   *
   * @param supersedes the value to set.
   */
  public void setSupersedes(AbstractAnnotation supersedes) {
    this.supersedes = supersedes;
  }

  /**
   * Get supersededBy.
   *
   * @return supersededBy.
   */
  public AbstractAnnotation getSupersededBy() {
    return supersededBy;
  }

  /**
   * Set supersededBy.
   *
   * @param supersededBy the value to set.
   */
  public void setSupersededBy(AbstractAnnotation supersededBy) {
    this.supersededBy = supersededBy;
  }

  /**
   * Get type.
   *
   * @return type as String.
   */
  public String getType() {
    return type;
  }

  /**
   * Set type.
   *
   * @param type the value to set.
   */
  public void setType(String type) {
    this.type = type;
  }

}
