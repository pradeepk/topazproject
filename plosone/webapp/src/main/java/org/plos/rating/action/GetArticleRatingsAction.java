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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import static org.plos.Constants.PLOS_ONE_USER_KEY;
import org.plos.action.BaseActionSupport;
import org.plos.article.service.ArticleOtmService;
import org.plos.configuration.OtmConfiguration;
import org.plos.models.CommentAnnotation;
import org.plos.models.Rating;
import org.plos.models.RatingSummary;
import org.plos.user.PlosOneUser;

import org.topazproject.otm.OtmException;
import org.topazproject.otm.Session;
import org.topazproject.otm.Transaction;
import org.topazproject.otm.criterion.Restrictions;

import com.opensymphony.webwork.ServletActionContext;
import org.springframework.beans.factory.annotation.Required;

/**
 * Rating action class to retrive all ratings for an Article.
 *
 * @author Jeff Suttor
 */
public class GetArticleRatingsAction extends BaseActionSupport {

  private Session            session;
  private ArticleOtmService  articleOtmService;
  private String             articleURI;
  private boolean            hasRated = false;
  private Map<String, ArticleRatingSummary> articleRatings = new HashMap();

  private static final Log log = LogFactory.getLog(GetArticleRatingsAction.class);
  private RatingsPEP pep;

  private RatingsPEP getPEP() {
    try {
      if (pep == null)
        pep                           = new RatingsPEP();
    } catch (Exception e) {
      throw new Error("Failed to create Ratings PEP", e);
    }
    return pep;
  }

  /**
   * Execute the ratings action.
   *
   * @return WebWork action status
   */
  public String execute() {
    Transaction   tx                 = null;
    PlosOneUser   user               =
      (PlosOneUser) ServletActionContext.getRequest().getSession().getAttribute(PLOS_ONE_USER_KEY);

    getPEP().checkAccess(RatingsPEP.GET_RATINGS, URI.create(articleURI));

    try {
      tx = session.beginTransaction();

      if (log.isDebugEnabled()) {
        log.debug("retrieving all ratings for: " + articleURI);
      }

      // TODO: straight OQL might be better?
      // list of Ratings that annotate this article
      List<Rating> ratings = session.createCriteria(Rating.class).add(Restrictions.eq("annotates", articleURI)).list();
      // populate a Map by user of ratings
      for (Rating rating :ratings) {
        // does an entry exist for this user?
        ArticleRatingSummary userArticleRatings = articleRatings.get(rating.getCreator());
        if (userArticleRatings == null) {
          userArticleRatings = new ArticleRatingSummary();
          userArticleRatings.setCreatorURI(rating.getCreator());
          // TODO: resolve creatorURI to creatorName, for now, it will be filled in w/tmp String below
          articleRatings.put(rating.getCreator(), userArticleRatings);
        }

        // update Rating type for this user
        userArticleRatings.addRating(rating);
        
        // respect existing values for articleURI & articleTitle, should also be more effecient
        if (userArticleRatings.getArticleURI() == null) {
          userArticleRatings.setArticleURI(articleURI);
        }
        if (userArticleRatings.getArticleTitle() == null) {
          // TODO: ArticleOtmService...
          userArticleRatings.setArticleTitle("Article title place holder for testing, resolve " + articleURI);
        }

        // respect existing values for creatorURI & creatorName, should also be more effecient
        if (userArticleRatings.getCreatorURI() == null) {
          userArticleRatings.setCreatorURI(rating.getCreator());
        }
        if (userArticleRatings.getCreatorName() == null) {
          // TODO: user service or native OTM
          userArticleRatings.setCreatorName("Creator name place holder for testing, resolve " + rating.getCreator());
        }

        hasRated = true;
      }

      // also add user comments
      List<CommentAnnotation> commentList =
        session.createCriteria(CommentAnnotation.class).add(Restrictions.eq("annotates", articleURI)).list();
      for (CommentAnnotation comment : commentList) {

        // does an entry exist for this user?
        ArticleRatingSummary userArticleRatings = articleRatings.get(comment.getCreator());
        if (userArticleRatings == null) {
          userArticleRatings = new ArticleRatingSummary();
          userArticleRatings.setCreatorURI(comment.getCreator());
          articleRatings.put(comment.getCreator(), userArticleRatings);
        }

        // TODO: current ratings model assumes 1 & only 1 comment exists,
        //   expand to mutiple typed comments?
        // update Comment for this user
        userArticleRatings.addComment(comment);

        hasRated = true;
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
    }

    return SUCCESS;
  }

  /**
   * Gets the URI of the article being rated.
   *
   * @return Returns the articleURI.
   */
  public String getArticleURI() {
    return articleURI;
  }

  /**
   * Sets the URI of the article being rated.
   *
   * @param articleURI The articleUri to set.
   */
  public void setArticleURI(String articleURI) {
    this.articleURI = articleURI;
  }

  /**
   * Gets the otm session.
   *
   * @return Returns the otm session.
   */
  public Session getOtmSession() {
    return session;
  }

  /**
   * Set the OTM session. Called by spring's bean wiring.
   *
   * @param session The otm session to set.
   */
  @Required
  public void setOtmSession(Session session) {
    this.session = session;
  }

  /**
   * Gets the ArticleOtmService.
   * Use ArticleOtmService v native OTM as it's aware of Article semantics.
   *
   * @return The ArticleOtmService.
   */
  public ArticleOtmService getArticleOtmService() {
    return articleOtmService;
  }

  /**
   * Sets the ArticleOtmService.
   * Use ArticleOtmService v native OTM as it's aware of Article semantics.
   * Called by Spring's bean wiring.
   *
   * @param ArticleOtmService to set.
   */
  @Required
  public void setArticleOtmService(ArticleOtmService articleOtmService) {
    this.articleOtmService = articleOtmService;
  }

  /**
   * Tests if this article has been rated.
   *
   * @return Returns the hasRated.
   */
  public boolean isHasRated() {
    return hasRated;
  }

  /**
   * Sets a flag to indicate that an article has been rated.
   *
   * @param hasRated The hasRated to set.
   */
  public void setHasRated(boolean hasRated) {
    this.hasRated = hasRated;
  }

  /**
   * Gets all ratings for the Article.
   *
   * @return Returns Ratings for the Article.
   */
  public Collection<ArticleRatingSummary> getArticleRatings() {

    // TODO: remove dubbing
    if (log.isDebugEnabled()) {
      log.debug("getArticleRatings():" + articleRatings.size());
    }
    return articleRatings.values();
  }
}
