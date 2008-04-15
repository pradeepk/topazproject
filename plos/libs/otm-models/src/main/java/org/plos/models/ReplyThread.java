/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007-2008 by Topaz, Inc.
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
package org.plos.models;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.topazproject.otm.annotations.Predicate;

/**
 * Reply meta-data.
 *
 * @author Pradeep Krishnan
 */
public class ReplyThread extends Reply {
  @Predicate(uri=Reply.NS + "inReplyTo", inverse=true, notOwned=true)
  private List<ReplyThread> replies = new ArrayList<ReplyThread>();

  /**
   * Creates a new ReplyThread object.
   */
  public ReplyThread() {
  }

  /**
   * Creates a new ReplyThread object.
   *
   * @param id reply id
   */
  public ReplyThread(URI id) {
    super(id);
  }

  /**
   *
   * @return the thread of replies
   */
  public List<ReplyThread> getReplies() {
    return replies;
  }

  /**
   *
   * @param replies the thread of replies
   */
  public void setReplies(List<ReplyThread> replies) {
    this.replies = replies;
  }

  /**
   * Add a reply to this.
   *
   * @param r the reply to add
   */
  public void addReply(ReplyThread r) {
    r.setRoot(getRoot());
    r.setInReplyTo(getId().toString());
    replies.add(r);
  }
}
