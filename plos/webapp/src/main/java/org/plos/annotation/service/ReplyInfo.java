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

import java.net.URI;

import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.SimpleTimeZone;

import org.plos.models.Blob;
import org.plos.models.Reply;

/**
 * The Reply meta-data.
 *
 * @author Pradeep Krishnan
 */
public class ReplyInfo {
  private Reply reply;
  private static SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

  static {
    fmt.setTimeZone(new SimpleTimeZone(0, "UTC"));
  }
  /**
   * Creates a new ReplyInfo object.
   */
  public ReplyInfo(Reply reply) {
    this.reply      = reply;
  }
  /**
   * Get id.
   *
   * @return id as String.
   */
  public String getId() {
    return reply.getId().toString();
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
   * Get root.
   *
   * @return root as String.
   */
  public String getRoot() {
    URI u = reply.getRoot();
    return (u == null) ? null : u.toString();
  }

  /**
   * Get inReplyTo.
   *
   * @return inReplyTo as String.
   */
  public String getInReplyTo() {
    URI u = reply.getInReplyTo();
    return (u == null) ? null : u.toString();
  }

  /**
   * Get title.
   *
   * @return title as String.
   */
  public String getTitle() {
    return reply.getTitle();
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
   * Get created.
   *
   * @return created as String.
   */
  public String getCreated() {
    Date d = reply.getCreated();

    synchronized (fmt) {
      return (d == null) ? null : fmt.format(d);
    }
  }

  /**
   * Get body.
   *
   * @return body as String.
   */
  public Blob getBody() {
    return reply.getBody();
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
   * Get state.
   *
   * @return state as int.
   */
  public int getState() {
    return reply.getState();
  }

}
