/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.plos.annotation;

import org.plos.annotation.service.Annotation;
import org.plos.annotation.service.Reply;

import java.util.Comparator;

/**
 * Simple wrapper class around an Annotation and the associated list of replies.
 * Implements the compartor interface to sort by reverse chronological order.
 * 
 * @author Stephen Cheng
 *
 */
public class Commentary implements Comparator<Commentary> {
  private Annotation annotation;  
  private Reply[]replies;
  
  public Commentary() {
  }

  /**
   * @return Returns the annotation.
   */
  public Annotation getAnnotation() {
    return annotation;
  }

  /**
   * @param annotation The annotation to set.
   */
  public void setAnnotation(Annotation annotation) {
    this.annotation = annotation;
  }

  /**
   * @return Returns the replies.
   */
  public Reply[] getReplies() {
    return replies;
  }

  /**
   * @param replies The replies to set.
   */
  public void setReplies(Reply[] replies) {
    this.replies = replies;
  }
  
  /**
   * This comparator does a reverse sort based on the last reply to the annotation.  If not replies
   * are present, the annotation time is used.
   * 
   * @param a
   * @param b
   * @return
   */
  public int compare (Commentary a, Commentary b){
    String dateA, dateB;
    Reply[] allReplies = a.getReplies();
    if (allReplies == null || allReplies.length == 0) {
      dateA = annotation. getCreated();
    } else {
      dateA = allReplies[allReplies.length - 1].getCreated();
    }
    allReplies = b.getReplies();
    if (allReplies == null || allReplies.length == 0) {
      dateB = annotation.getCreated();
    } else {
      dateB = allReplies[allReplies.length - 1].getCreated();
    }    
    return dateB.compareTo(dateA);
  }
}
