package org.topazproject.ws.annotation;

/**
 * The reply thread starting with this reply.
 *
 * @author Pradeep Krishnan
 */
public class ReplyThread extends ReplyInfo {
  private ReplyThread[] replies;

  /**
   * Get replies.
   *
   * @return replies as ReplyThread[].
   */
  public ReplyThread[] getReplies() {
    return replies;
  }

  /**
   * Set replies.
   *
   * @param replies the value to set.
   */
  public void setReplies(ReplyThread[] replies) {
    this.replies = replies;
  }
}
