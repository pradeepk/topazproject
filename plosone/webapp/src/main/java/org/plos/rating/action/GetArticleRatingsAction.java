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
import org.plos.article.util.ArticleUtil;
import org.plos.article.util.NoSuchArticleIdException;
import org.plos.configuration.OtmConfiguration;
import org.plos.models.Article;
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
  private String             articleTitle;
  private String             articleDescription;
  private boolean            hasRated = false;
  private List<Rating> articleRatings = new ArrayList();

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
  public String execute() throws Exception {
    Transaction   tx                 = null;
    PlosOneUser   user               =
      (PlosOneUser) ServletActionContext.getRequest().getSession().getAttribute(PLOS_ONE_USER_KEY);

    getPEP().checkAccess(RatingsPEP.GET_RATINGS, URI.create(articleURI));

    // fill in Article title if necessary
    // TODO, expose more of the Article metadata, need a articleOtmService.getArticleInfo(URI)
    Article article = null;
    if (articleTitle == null) {
      article = articleOtmService.getArticle(URI.create(articleURI));
      articleTitle = article.getTitle();
    }
    if (articleDescription == null) {
      if (article == null) {
        article = articleOtmService.getArticle(URI.create(articleURI));
      }
      articleDescription = article.getDescription();
    }

    try {
      tx = session.beginTransaction();

      // list of Ratings that annotate this article
      articleRatings = session
        .createCriteria(Rating.class)
        .add(Restrictions.eq("annotates", articleURI))
        .list();
      
      // create ArticleRatingSummary(s)

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

    if (articleRatings.size() > 0) {
      hasRated = true;
    }

    if (log.isDebugEnabled()) {
      log.debug("retried all ratings, " + articleRatings.size() + ", for: " + articleURI);
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
   * Gets the title of the article being rated.
   *
   * @return Returns the articleTitle.
   */
  public String getArticleTitle() {

    if (articleTitle != null) {
      return articleTitle;
    }

    articleTitle = "Article title place holder for testing, resolve " + articleURI;
    return articleTitle;
  }

  /**
   * Sets the title of the article being rated.
   *
   * @param articleTitle The article's title.
   */
  public void setArticleTitle(String articleTitle) {
    this.articleTitle = articleTitle;
  }

  /**
   * Gets the description of the Article being rated.
   *
   * @return Returns the articleDescription.
   */
  public String getArticleDescription() {

    if (articleDescription != null) {
      return articleDescription;
    }

    articleDescription = "Article Description place holder for testing, resolve " + articleURI;
    return articleDescription;
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
  public Collection<Rating> getArticleRatings() {

    // TODO: remove dubbing
    if (log.isDebugEnabled()) {
      log.debug("getArticleRatings(): (" + articleRatings.size() + ") " + articleURI + ", " + articleTitle);
    }
    return articleRatings;
  }
}
