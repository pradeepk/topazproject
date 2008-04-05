/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2008 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.plos.annotation.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.plos.ApplicationException;

import org.plos.user.service.UserService;

import org.plos.util.DateParser;
import org.plos.util.InvalidDateException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

/**
 * Plosone wrapper around the ReplyInfo from topaz service. It provides
 * - A way to escape title/body text when returning the result to the web layer
 * - a separation from any topaz changes
 */
public abstract class Reply extends BaseAnnotation {
  private static final Log log = LogFactory.getLog(Reply.class);

  private final ReplyInfo reply;
  private Collection<Reply> replies = new ArrayList<Reply>();
  private String creatorName;
  private UserService userService;

  public Reply(final ReplyInfo reply) {
    this.reply = reply;
  }

  /**
   * Constructor that takes in a UserService object in addition to ReplyInfo in order
   * to retrieve the username.
   * 
   * @param reply
   * @param userSvc
   */
  public Reply(final ReplyInfo reply, UserService userSvc) {
    this.reply = reply;
    this.userService = userSvc;
  }

  /**
   * Get created date.
   * @return created as java.util.Date.
   */
  public Date getCreatedAsDate() {
    try {
      return DateParser.parse(reply.getCreated());
    } catch (InvalidDateException ide) {
    }
    return null;
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
   * Get creator.
   *
   * @return creator as String.
   */
  public String getCreator() {
    return reply.getCreator();
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
   * Get inReplyTo.
   *
   * @return inReplyTo as String.
   */
  public String getInReplyTo() {
    return reply.getInReplyTo();
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
   * Get root.
   *
   * @return root as String.
   */
  public String getRoot() {
    return reply.getRoot();
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
   * Get title.
   *
   * @return title as String.
   */
  public String getCommentTitle() {
    return escapeText(reply.getTitle());
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

  /** see BaseAnnotation#isPublic */
  public boolean isPublic() {
    throw new UnsupportedOperationException("A reply is always public, so please don't call this");
  }

  /**
   * @return Returns the creatorName.
   */
  public String getCreatorName() {
    if (creatorName == null) {
      try {
        log.debug("getting User Object for id: " + getCreator());
        creatorName = userService.getUsernameByTopazId(getCreator());
      } catch (ApplicationException ae) {
        creatorName = getCreator();
      }
    }
    return creatorName;
  }

  /**
   * @param creatorName The creatorName to set.
   */
  public void setCreatorName(String creatorName) {
    this.creatorName = creatorName;
  }

  /**
   * @return Returns the userService.
   */
  protected UserService getUserService() {
    return userService;
  }

  /**
   * @param userService The userService to set.
   */
  public void setUserService(UserService userService) {
    this.userService = userService;
  }
}
