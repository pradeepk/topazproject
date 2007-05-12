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

import java.net.URI;
import java.net.URISyntaxException;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import static org.plos.Constants.PLOS_ONE_USER_KEY;

import org.plos.action.BaseActionSupport;

import org.plos.annotation.otm.CommentAnnotation;

import org.plos.configuration.OtmConfiguration;

import org.plos.rating.otm.Rating;
import org.plos.rating.otm.RatingSummary;

import org.plos.user.PlosOneUser;

import org.topazproject.otm.OtmException;
import org.topazproject.otm.Session;
import org.topazproject.otm.Transaction;
import org.topazproject.otm.criterion.Restrictions;

import com.opensymphony.webwork.ServletActionContext;

import com.opensymphony.xwork.validator.annotations.RequiredStringValidator;

/**
 * General Rating action class to store and retrieve a users's rating
 *
 * @author Stephen Cheng
 */
public class RateAction extends BaseActionSupport {
  private double           insight;
  private double           reliability;
  private double           style;
  private double           overall;
  private String           articleUri;
  private String           commentTitle;
  private String           comment;
  private OtmConfiguration otmFactory;
  private static final Log log = LogFactory.getLog(RateAction.class);
  private RatingsPEP       pep;

/**
   * Creates a new RateAction object.
   */
  public RateAction() {
    try {
      pep                      = new RatingsPEP();
    } catch (Exception e) {
      throw new Error("Failed to create Ratings PEP", e);
    }
  }

  /**
   * Rates an article for the currently logged in user.  Will look to see if there are
   * existing rating. If so, will update the ratings, otherwise will insert new ones.
   *
   * @return WebWork action status
   */
  public String rateArticle() {
    Session           session            = otmFactory.getFactory().openSession();
    Transaction       tx                 = null;
    PlosOneUser       user               =
      (PlosOneUser) ServletActionContext.getRequest().getSession().getAttribute(PLOS_ONE_USER_KEY);
    Date              now                = new Date(System.currentTimeMillis());

    Rating            insightRating      = null;
    Rating            styleRating        = null;
    Rating            reliabilityRating  = null;
    RatingSummary     insightSummary     = null;
    RatingSummary     styleSummary       = null;
    RatingSummary     reliabilitySummary = null;
    RatingSummary     overallSummary     = null;

    CommentAnnotation ratingComment      = null;
    URI               annotatedArticle   = null;

    try {
      annotatedArticle = new URI(articleUri);
    } catch (URISyntaxException ue) {
      log.info("Could not construct article URI: " + articleUri, ue);

      return ERROR;
    }

    pep.checkObjectAccess(RatingsPEP.SET_RATINGS, URI.create(user.getUserId()), annotatedArticle);

    try {
      tx = session.beginTransaction();

      if (log.isDebugEnabled()) {
        log.debug("Retrieving Ratings Summaries for article: " + articleUri);
      }

      List     summaryList =
        session.createCriteria(RatingSummary.class).add(Restrictions.eq("annotates", articleUri))
                .list();
      Iterator iter        = summaryList.iterator();

      while (iter.hasNext()) {
        RatingSummary ratingSummary = (RatingSummary) iter.next();

        if (Rating.INSIGHT_TYPE.equals(ratingSummary.getType())) {
          insightSummary = ratingSummary;
        } else if (Rating.STYLE_TYPE.equals(ratingSummary.getType())) {
          styleSummary = ratingSummary;
        } else if (Rating.RELIABILITY_TYPE.equals(ratingSummary.getType())) {
          reliabilitySummary = ratingSummary;
        } else if (Rating.OVERALL_TYPE.equals(ratingSummary.getType())) {
          overallSummary = ratingSummary;
        }
      }

      if (log.isDebugEnabled()) {
        log.debug("Retrieving user ratings for article: " + articleUri + " and user: "
                  + user.getUserId());
      }

      List ratingsList =
        session.createCriteria(Rating.class).add(Restrictions.eq("annotates", articleUri))
                .add(Restrictions.eq("creator", user.getUserId())).list();

      iter = ratingsList.iterator();

      boolean updatingRating = false;

      while (iter.hasNext()) {
        updatingRating = true;

        Rating rating = (Rating) iter.next();

        if (Rating.INSIGHT_TYPE.equals(rating.getType())) {
          insightRating = rating;
        } else if (Rating.STYLE_TYPE.equals(rating.getType())) {
          styleRating = rating;
        } else if (Rating.RELIABILITY_TYPE.equals(rating.getType())) {
          reliabilityRating = rating;
        }
      }

      if (insight > 0) {
        if (insightRating == null) {
          insightRating = new Rating();
        } else {
          if (insightSummary != null) {
            insightSummary.removeRating(insightRating.retrieveValue());
          }
        }

        insightRating.setType(Rating.INSIGHT_TYPE);
        insightRating.setContext("");
        insightRating.setAnnotates(annotatedArticle);
        insightRating.assignValue((int) insight);
        insightRating.setCreator(user.getUserId());
        insightRating.setCreated(now);

        if (insightSummary == null) {
          insightSummary = new RatingSummary();
          insightSummary.setAnnotates(annotatedArticle);
          insightSummary.setType(Rating.INSIGHT_TYPE);
        }

        insightSummary.addRating((int) insight);
      }

      if (style > 0) {
        if (styleRating == null) {
          styleRating = new Rating();
        } else {
          if (styleSummary != null) {
            styleSummary.removeRating(styleRating.retrieveValue());
          }
        }

        styleRating.setType(Rating.STYLE_TYPE);
        styleRating.setContext("");
        styleRating.setAnnotates(annotatedArticle);
        styleRating.assignValue((int) style);
        styleRating.setCreator(user.getUserId());
        styleRating.setCreated(now);

        if (styleSummary == null) {
          styleSummary = new RatingSummary();
          styleSummary.setAnnotates(annotatedArticle);
          styleSummary.setType(Rating.STYLE_TYPE);
        }

        styleSummary.addRating((int) style);
      }

      if (reliability > 0) {
        if (reliabilityRating == null) {
          reliabilityRating = new Rating();
        } else {
          if (reliabilitySummary != null) {
            reliabilitySummary.removeRating(reliabilityRating.retrieveValue());
          }
        }

        reliabilityRating.setType(Rating.RELIABILITY_TYPE);
        reliabilityRating.setContext("");
        reliabilityRating.setAnnotates(annotatedArticle);
        reliabilityRating.assignValue((int) reliability);
        reliabilityRating.setCreator(user.getUserId());
        reliabilityRating.setCreated(now);

        if (reliabilitySummary == null) {
          reliabilitySummary = new RatingSummary();
          reliabilitySummary.setAnnotates(annotatedArticle);
          reliabilitySummary.setType(Rating.RELIABILITY_TYPE);
        }

        reliabilitySummary.addRating((int) reliability);
      }

      List<CommentAnnotation> commentList =
        session.createCriteria(CommentAnnotation.class).add(Restrictions.eq("annotates", articleUri))
                .add(Restrictions.eq("creator", user.getUserId())).list();

      if (commentList.size() > 0) {
        ratingComment = commentList.get(0);
      }

      if (!StringUtils.isBlank(commentTitle) || !StringUtils.isBlank(comment)) {
        if (ratingComment == null) {
          ratingComment = new CommentAnnotation();
        }

        ratingComment.setContext("");
        ratingComment.setAnnotates(annotatedArticle);
        ratingComment.setTitle(commentTitle);
        ratingComment.assignComment(comment);
        ratingComment.setCreator(user.getUserId());
        ratingComment.setCreated(now);
      }

      if (styleRating != null) {
        if (style > 0)
          session.saveOrUpdate(styleRating);
        else {
          styleSummary.removeRating(styleRating.retrieveValue());
          session.delete(styleRating);
        }
      }

      if (insightRating != null) {
        if (insight > 0)
          session.saveOrUpdate(insightRating);
        else {
          insightSummary.removeRating(insightRating.retrieveValue());
          session.delete(insightRating);
        }
      }

      if (reliabilityRating != null) {
        if (reliability > 0)
          session.saveOrUpdate(reliabilityRating);
        else {
          reliabilitySummary.removeRating(reliabilityRating.retrieveValue());
          session.delete(reliabilityRating);
        }
      }

      if (styleSummary != null) {
        styleSummary.setCreated(now);
        session.saveOrUpdate(styleSummary);
      }

      if (insightSummary != null) {
        insightSummary.setCreated(now);
        session.saveOrUpdate(insightSummary);
      }

      if (reliabilitySummary != null) {
        reliabilitySummary.setCreated(now);
        session.saveOrUpdate(reliabilitySummary);
      }

      if (overallSummary == null) {
        overallSummary = new RatingSummary();
        overallSummary.setAnnotates(annotatedArticle);
        overallSummary.setType(Rating.OVERALL_TYPE);
      }

      calculateOverall(updatingRating, overallSummary, insightSummary, styleSummary,
                       reliabilitySummary);
      overallSummary.setCreated(now);
      session.saveOrUpdate(overallSummary);

      if (ratingComment != null) {
        session.saveOrUpdate(ratingComment);
      }

      tx.commit(); // Flush happens automatically
    } catch (OtmException e) {
      try {
        if (tx != null)
          tx.rollback();
      } catch (OtmException re) {
        log.warn("rollback failed", re);
      }

      throw e; // or display error message
    } finally {
      try {
        session.close();
      } catch (OtmException ce) {
        log.warn("close failed", ce);
      }
    }

    return SUCCESS;
  }

  private void calculateOverall(boolean update, RatingSummary overall, RatingSummary insight,
                                RatingSummary style, RatingSummary reliability) {
    int    numCategories = 0;
    double runningTotal  = 0;

    if (insight != null) {
      numCategories++;
      runningTotal += insight.retrieveAverage();
      log.debug("INSIGHT: numCats = " + numCategories + " runningTotal: " + runningTotal);
    }

    if (style != null) {
      numCategories++;
      runningTotal += style.retrieveAverage();
      log.debug("STYLE: numCats = " + numCategories + " runningTotal: " + runningTotal);
    }

    if (reliability != null) {
      numCategories++;
      runningTotal += reliability.retrieveAverage();
      log.debug("RELIABILITY: numCats = " + numCategories + " runningTotal: " + runningTotal);
    }

    if (!update) {
      overall.assignNumRatings(overall.retrieveNumRatings() + 1);
    } else if ((this.style < 1) && (this.insight < 1) && (this.reliability < 1)) {
      overall.assignNumRatings(overall.retrieveNumRatings() - 1);
    }

    overall.assignTotal(runningTotal / numCategories);
  }

  /**
   * Get the ratings and comment for the logged in user
   *
   * @return WebWork action status
   */
  public String retrieveRatingsForUser() {
    Session     session = otmFactory.getFactory().openSession();
    Transaction tx      = null;

    try {
      tx = session.beginTransaction();

      PlosOneUser user =
        (PlosOneUser) ServletActionContext.getRequest().getSession().getAttribute(PLOS_ONE_USER_KEY);

      if (user == null) {
        log.info("User is null for retrieving user ratings");
        addActionError("Must be logged in");

        return ERROR;
      }

      List<Rating> ratingsList =
        session.createCriteria(Rating.class).add(Restrictions.eq("annotates", articleUri))
                .add(Restrictions.eq("creator", user.getUserId())).list();

      if (ratingsList.size() < 1) {
        log.debug("didn't find any matching ratings for user: " + user.getUserId());
        addActionError("No ratings for user");

        return ERROR;
      }

      Iterator<Rating> iter = ratingsList.iterator();

      while (iter.hasNext()) {
        Rating rating = iter.next();

        if (Rating.INSIGHT_TYPE.equals(rating.getType()))
          setInsight(rating.retrieveValue());
        else if (Rating.STYLE_TYPE.equals(rating.getType()))
          setStyle(rating.retrieveValue());
        else if (Rating.RELIABILITY_TYPE.equals(rating.getType()))
          setReliability(rating.retrieveValue());
      }

      List<CommentAnnotation> commentList =
        session.createCriteria(CommentAnnotation.class).add(Restrictions.eq("annotates", articleUri))
                .add(Restrictions.eq("creator", user.getUserId())).list();

      if (commentList.size() < 1) {
        log.debug("didn't find any matching comment for user: " + user.getUserId());
      } else {
        setCommentTitle(commentList.get(0).getTitle());
        setComment(commentList.get(0).retrieveComment());
      }

      tx.commit(); // Flush happens automatically
    } catch (OtmException e) {
      try {
        if (tx != null)
          tx.rollback();
      } catch (OtmException re) {
        log.warn("rollback failed", re);
      }

      throw e; // or display error message
    } finally {
      try {
        session.close();
      } catch (OtmException ce) {
        log.warn("close failed", ce);
      }
    }

    return SUCCESS;
  }

  /**
   * Gets the URI of the article being rated.
   *
   * @return Returns the articleUri.
   */
  @RequiredStringValidator(message = "Article URI is a required field")
  public String getArticleUri() {
    return articleUri;
  }

  /**
   * Sets the URI of the article being rated.
   *
   * @param articleUri The articleUri to set.
   */
  public void setArticleUri(String articleUri) {
    this.articleUri = articleUri;
  }

  /**
   * Gets the style rating.
   *
   * @return Returns the style.
   */

  //@DoubleRangeFieldValidator(message = "Elegance must be <= 5 and greater than 0", key = "i18n.key", shortCircuit = true, minInclusive = "1.0", maxInclusive = "5.0")
  public double getStyle() {
    return style;
  }

  /**
   * Sets the style rating.
   *
   * @param style The elegance to set.
   */
  public void setStyle(double style) {
    this.style = style;
  }

  /**
   * Gets the insight rating.
   *
   * @return Returns the insight.
   */

  //@DoubleRangeFieldValidator(message = "Insight must be <= 5 and greater than 0", key = "i18n.key", shortCircuit = true, minInclusive = "0.0", maxInclusive = "5.0")
  public double getInsight() {
    return insight;
  }

  /**
   * Sets the insight rating.
   *
   * @param insight The insight to set.
   */
  public void setInsight(double insight) {
    if (log.isDebugEnabled())
      log.debug("setting insight to: " + insight);

    this.insight = insight;
  }

  /**
   * Gets the overall rating.
   *
   * @return Returns the overall.
   */
  public double getOverall() {
    return overall;
  }

  /**
   * Sets the overall rating.
   *
   * @param overall The overall to set.
   */
  public void setOverall(double overall) {
    this.overall = overall;
  }

  /**
   * Gets the reliability rating.
   *
   * @return Returns the security.
   */

  //@DoubleRangeFieldValidator(message = "Reliability must be <= 5 and greater than 0", key = "i18n.key", shortCircuit = true, minInclusive = "1.0", maxInclusive = "5.0")
  public double getReliability() {
    return reliability;
  }

  /**
   * Sets the reliability rating.
   *
   * @param reliability The security to set.
   */
  public void setReliability(double reliability) {
    this.reliability = reliability;
  }

  /**
   * Gets the rating comment.
   *
   * @return Returns the comment.
   */
  public String getComment() {
    return comment;
  }

  /**
   * Sets the ratings comment.
   *
   * @param comment The comment to set.
   */
  public void setComment(String comment) {
    this.comment = comment;
  }

  /**
   * Gets the rating comment title.
   *
   * @return Returns the commentTitle.
   */
  public String getCommentTitle() {
    return commentTitle;
  }

  /**
   * Sets the rating comment title.
   *
   * @param commentTitle The commentTitle to set.
   */
  public void setCommentTitle(String commentTitle) {
    this.commentTitle = commentTitle;
  }

  /**
   * Gets the OTM util.
   *
   * @return Returns the otmFactory.
   */
  public OtmConfiguration getOtmFactory() {
    return otmFactory;
  }

  /**
   * Sets the OTM util.
   *
   * @param otmFactory The otmFactory to set.
   */
  public void setOtmFactory(OtmConfiguration otmFactory) {
    this.otmFactory = otmFactory;
  }
}
