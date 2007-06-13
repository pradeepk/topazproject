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

import java.util.Date;

import org.plos.models.Rating;

/**
 * Article ratings & comments by user.
 *
 * Data structures used by display & browse for the ratings & comments for an Article by user.
 */
public class ArticleRatingSummary {

  private String articleURI;
  private String articleTitle;
  private Date   created;
  private String creatorURI;
  private String creatorName;
  private int    style;
  private int    insight;
  private int    reliability;
  private int    overall;
  private String commentTitle;
  private String commentValue;

  public ArticleRatingSummary(String articleURI, String articleTitle) {

    this.articleURI = articleURI;
    this.articleTitle = articleTitle;
  }

  public void addRating(Rating rating) {

    this.insight = rating.getBody().getInsightValue();
    this.style = rating.getBody().getStyleValue();
    this.reliability = rating.getBody().getReliabilityValue();
    this.overall = rating.getBody().getOverallValue();

    this.commentTitle = rating.getBody().getCommentTitle();
    this.commentValue = rating.getBody().getCommentValue();

    this.creatorURI = rating.getCreator();
  }

  public void setArticleURI(String articleURI) {
    this.articleURI = articleURI;
  }
  public String getArticleURI() {
    return articleURI;
  }

  public void setArticleTitle(String articleTitle) {
    this.articleTitle = articleTitle;
  }
  public String getArticleTitle() {
    return articleTitle;
  }

  public void setCreated(Date created) {
    this.created = created;
  }
  public Date getCreated() {
    return created;
  }

  public void setCreatorURI(String creatorURI) {
    this.creatorURI = creatorURI;
  }
  public String getCreatorURI() {
    if (creatorURI != null) {
      return creatorURI;
    }

    // TODO: it should never be null
    return("null");
  }

  public void setCreatorName(String creatorName) {
    this.creatorName = creatorName;
  }
  public String getCreatorName() {
    if (creatorName != null) {
      return creatorName;
    }
    // TODO: it should never be null
    return("null");
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
