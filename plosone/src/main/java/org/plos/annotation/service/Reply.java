/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.annotation.service;

import org.topazproject.ws.annotation.ReplyInfo;

/**
 * Plosone wrapper around the ReplyInfo from topaz service. It provides
 * - A way to escape title/body text when returning the result to the web layer
 * - Fetch the body content eagerly.
 * - a separation from any topaz changes
 */
public abstract class Reply extends BaseAnnotation {
  private final ReplyInfo reply;

  public Reply(final ReplyInfo reply) {
    this.reply = reply;
  }

  /**
   * Get created.
   *
   * @return created as String.
   */
  public String getCreated() {
    return reply.getCreated();
  }

  /**
   * Set created.
   *
   * @param created the value to set.
   */
  public void setCreated(final String created) {
    reply.setCreated(created);
  }

  /**
   * Get creator.
   *
   * @return creator as String.
   */
  public String getCreator() {
    return reply.getCreator();
  }

  /**
   * Set creator.
   *
   * @param creator the value to set.
   */
  public void setCreator(final String creator) {
    reply.setCreator(creator);
  }

  /**
   * Get id.
   *
   * @return id as String.
   */
  public String getId() {
    return reply.getId();
  }

  /**
   * Set id.
   *
   * @param id the value to set.
   */
  public void setId(final String id) {
    reply.setId(id);
  }

  /**
   * Get inReplyTo.
   *
   * @return inReplyTo as String.
   */
  public String getInReplyTo() {
    return reply.getInReplyTo();
  }

  /**
   * Set inReplyTo.
   *
   * @param inReplyTo the value to set.
   */
  public void setInReplyTo(final String inReplyTo) {
    reply.setInReplyTo(inReplyTo);
  }

  /**
   * Get mediator.
   *
   * @return mediator as String.
   */
  public String getMediator() {
    return reply.getMediator();
  }

  /**
   * Set mediator.
   *
   * @param mediator the value to set.
   */
  public void setMediator(final String mediator) {
    reply.setMediator(mediator);
  }

  /**
   * Get root.
   *
   * @return root as String.
   */
  public String getRoot() {
    return reply.getRoot();
  }

  /**
   * Set root.
   *
   * @param root the value to set.
   */
  public void setRoot(final String root) {
    reply.setRoot(root);
  }

  /**
   * Get state.
   *
   * @return state as int.
   */
  public int getState() {
    return reply.getState();
  }

  /**
   * Set state.
   *
   * @param state the value to set.
   */
  public void setState(final int state) {
    reply.setState(state);
  }

  /**
   * Get title.
   *
   * @return title as String.
   */
  public String getTitle() {
    return escapeText(reply.getTitle());
  }

  /**
   * Set title.
   *
   * @param title the value to set.
   */
  public void setTitle(final String title) {
    reply.setTitle(title);
  }

  /**
   * Get type.
   *
   * @return type as String.
   */
  public String getType() {
    return reply.getType();
  }

  /**
   * Set type.
   *
   * @param type the value to set.
   */
  public void setType(final String type) {
    reply.setType(type);
  }
}
