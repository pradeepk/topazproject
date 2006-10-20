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

import org.plos.ApplicationException;
import org.topazproject.ws.annotation.ReplyInfo;

import java.util.Collection;
import java.util.ArrayList;

/**
 * Plosone wrapper around the ReplyInfo from topaz service. It provides
 * - A way to escape title/body text when returning the result to the web layer
 * - a separation from any topaz changes
 */
public abstract class Reply extends BaseAnnotation {
  private final ReplyInfo reply;
  private Collection<Reply> replies = new ArrayList<Reply>();

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
  public String getCommentTitle() {
    return escapeText(reply.getTitle());
  }

  /**
   * Set title.
   *
   * @param title the value to set.
   */
  public void setCommentTitle(final String title) {
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

  /**
   * @return true if the Reply is public, false otherwise
   * @throws org.plos.ApplicationException ApplicationException
   */
  public boolean isPublic() throws ApplicationException {
    return checkIfPublic(reply.getState());
  }

  /**
   * Add a (child) reply to this reply
   * @param reply reply
   */
  public void addReply(final Reply reply) {
    replies.add(reply);
  }

  /**
   * @return the replies to this reply
   */
  public Reply[] getReplies() {
    return replies.toArray(new Reply[replies.size()]);
  }
}
