/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.rating.action;

import org.plos.models.CommentAnnotation;
import org.plos.models.Rating;

/**
 * Article ratings & comments by user.
 *
 * Data structures used by display & browse for the ratings & comments for an Article by user.
 */
public class ArticleRatingSummary {

  private String creator = null;
  private int style;
  private int insight;
  private int reliability;
  private int overall;
  private String commentTitle;
  private String commentValue;

  public void addRating(Rating rating) {
    
    if (Rating.INSIGHT_TYPE.equals(rating.getType())) {
      this.insight = rating.getBody().getValue();
    } else if (Rating.STYLE_TYPE.equals(rating.getType())) {
      this.style = rating.getBody().getValue();
    } else if (Rating.RELIABILITY_TYPE.equals(rating.getType())) {
      this.reliability = rating.getBody().getValue();
    } else if (Rating.OVERALL_TYPE.equals(rating.getType())) {
      this.overall = rating.getBody().getValue();
    }
  }

  public void addComment(CommentAnnotation comment) {
    
    this.commentTitle = comment.getTitle();
    this.commentValue = comment.getBody().getValue();
  }

  public void setCreator(String creator) {
    this.creator = creator;
  }
  public String getCreator() {
    return creator;
  }

  public void setStyle(int style) {
    this.style = style;
  }
  public int getStyle() {
    return style;
  }

  public void setInsight(int insight) {
    this.insight = insight;
  }
  public int getInsight() {
    return insight;
  }

  public void setReliability(int reliability) {
    this.reliability = reliability;
  }
  public int getReliability() {
    return reliability;
  }

  public void setOverall(int overall) {
    this.overall = overall;
  }
  public int getOverall() {
    return overall;
  }

  public void setCommentTitle(String title) {
    this.commentTitle = title;
  }
  public String getCommentTitle() {
    return commentTitle;
  }

  public void setCommentValue(String value) {
    this.commentValue = value;
  }
  public String getCommentValue() {
    return commentValue;
  }
}
