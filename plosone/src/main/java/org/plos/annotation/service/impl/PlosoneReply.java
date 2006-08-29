/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.annotation.service.impl;

import org.topazproject.ws.annotation.ReplyInfo;
import org.plos.annotation.service.Reply;
import com.opensymphony.util.TextUtils;

/**
 * Plosone wrapper around the ReplyInfo from topaz service. It provides
 * - A way to escape title/body text when returning the result to the web layer
 * - Fetch the body content eagerly.
 * - a separation from any topaz changes
 */
public class PlosoneReply implements Reply {
  private final ReplyInfo reply;

  public PlosoneReply(final ReplyInfo reply) {
    this.reply = reply;
  }

  /** {@inheritDoc} */
  public String getBody() {
    // TODO: fetch the body content right away and escape it
//    return TextUtils.htmlEncode(annotation.getTitle());
    
    return reply.getBody();
  }

  /** {@inheritDoc} */
  public void setBody(final String s) {
    reply.setBody(s);
  }

  /** {@inheritDoc} */
  public String getCreated() {
    return reply.getCreated();
  }

  /** {@inheritDoc} */
  public void setCreated(final String s) {
    reply.setCreated(s);
  }

  /** {@inheritDoc} */
  public String getCreator() {
    return reply.getCreator();
  }

  /** {@inheritDoc} */
  public void setCreator(final String s) {
    reply.setCreator(s);
  }

  /** {@inheritDoc} */
  public String getId() {
    return reply.getId();
  }

  /** {@inheritDoc} */
  public void setId(final String s) {
    reply.setId(s);
  }

  /** {@inheritDoc} */
  public String getInReplyTo() {
    return reply.getInReplyTo();
  }

  /** {@inheritDoc} */
  public void setInReplyTo(final String s) {
    reply.setInReplyTo(s);
  }

  /** {@inheritDoc} */
  public String getMediator() {
    return reply.getMediator();
  }

  /** {@inheritDoc} */
  public void setMediator(final String s) {
    reply.setMediator(s);
  }

  /** {@inheritDoc} */
  public String getRoot() {
    return reply.getRoot();
  }

  /** {@inheritDoc} */
  public void setRoot(final String s) {
    reply.setRoot(s);
  }

  /** {@inheritDoc} */
  public int getState() {
    return reply.getState();
  }

  /** {@inheritDoc} */
  public void setState(final int i) {
    reply.setState(i);
  }

  /** {@inheritDoc} */
  public String getTitle() {
    return TextUtils.htmlEncode(reply.getTitle());
  }

  /** {@inheritDoc} */
  public void setTitle(final String s) {
    reply.setTitle(s);
  }

  /** {@inheritDoc} */
  public String getType() {
    return reply.getType();
  }

  /** {@inheritDoc} */
  public void setType(final String s) {
    reply.setType(s);
  }

  /** {@inheritDoc} */
  public boolean equals(final Object o) {
    return reply.equals(o);
  }

  /** {@inheritDoc} */
  public int hashCode() {
    return reply.hashCode();
  }
}
