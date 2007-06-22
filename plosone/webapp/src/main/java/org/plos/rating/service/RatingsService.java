/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.rating.service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import java.net.URI;
import java.net.URISyntaxException;

import java.rmi.RemoteException;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.xml.rpc.ServiceException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import static org.plos.annotation.service.Annotation.DELETE_MASK;
import static org.plos.annotation.service.Annotation.FLAG_MASK;
import static org.plos.annotation.service.Annotation.PUBLIC_MASK;

import org.plos.user.PlosOneUser;

import org.plos.models.Rating;
import org.plos.models.RatingContent;
import org.plos.models.RatingSummary;
import org.plos.models.RatingSummaryContent;

import org.plos.permission.service.PermissionWebService;

import org.plos.util.FileUtils;
import org.plos.util.TransactionHelper;

import org.springframework.beans.factory.annotation.Required;

import org.topazproject.otm.OtmException;
import org.topazproject.otm.Criteria;
import org.topazproject.otm.Session;
import org.topazproject.otm.Transaction;
import org.topazproject.otm.criterion.Restrictions;

import com.opensymphony.oscache.base.NeedsRefreshException;
import com.opensymphony.oscache.general.GeneralCacheAdministrator;

/**
 * author: jonnie boldly copying several methods from someone else.
 */
public class RatingsService extends BaseRatingsService {
  private static final Log          log =
    LogFactory.getLog(RatingsService.class);

  private RatingsPEP                pep;
  private Session                   session;
  private PermissionWebService      permissions;

  /**
   * Initializes the service
   *
   * @throws IOException DOCUMENT ME!
   * @throws URISyntaxException DOCUMENT ME!
   * @throws ServiceException DOCUMENT ME!
   */
  public void init() throws IOException, URISyntaxException, ServiceException {
    try {
      if (pep == null)
        pep = new RatingsPEP();
    } catch (IOException e) {
      throw e;
    } catch (Exception e) {
      IOException ioe = new IOException("Failed to create PEP");
      ioe.initCause(e);
      throw ioe;
    }
  }

  /**
   * Create or update a Rating.
   *
   * @param mimeType mimeType
   * @param target target
   * @param context context
   * @param olderAnnotation olderAnnotation that the new one will supersede
   * @param title title
   * @param body body
   *
   * @return a the new annotation id
   *
   * @throws RemoteException RemoteException
   * @throws UnsupportedEncodingException UnsupportedEncodingException
   * @throws UnsupportedOperationException DOCUMENT ME!
   */
  public void saveOrUpdateRating(final PlosOneUser user,final String articleURIStr,final Rating values) throws RatingsServiceException {
    ensureInitGetsCalledWithUsersSessionAttributes();
    final Date now = Calendar.getInstance().getTime();

    try {
      final URI userURI = new URI(user.getUserId());
      final URI articleURI = new URI(articleURIStr);
      final Rating articleRating;
      final RatingSummary articleRatingSummary;
      final Transaction tx = session.beginTransaction();

      pep.checkObjectAccess(RatingsPEP.SET_RATINGS,userURI,articleURI);

      try {
        if (log.isDebugEnabled()) {
          log.debug("Retrieving user Ratings for article: " + articleURIStr +
                    " and user: " + user.getUserId());
        }

        // Ratings by this User for Article
        List<Rating> ratingsList = session.createCriteria(Rating.class)
                                          .add(Restrictions.eq("annotates",articleURIStr))
                                          .add(Restrictions.eq("creator",user.getUserId()))
                                          .list();

        final boolean newRating;

        if (ratingsList.size() == 0) {
          newRating = true;

          articleRating = new Rating();
          articleRating.setMediator(getApplicationId());
          articleRating.setAnnotates(articleURI);
          articleRating.setContext("");
          articleRating.setCreator(user.getUserId());
          articleRating.setCreated(now);
          articleRating.setBody(new RatingContent());
        } else {
          newRating = false;

          if (ratingsList.size() == 1) {
            articleRating = ratingsList.get(0);
          } else {
            // should never happen
            final String errorMessage = "Multiple Ratings for Article " + articleURIStr +
                                        " and user: " + user.getUserId();
            final Exception e = new RuntimeException(errorMessage);
            log.error(errorMessage);
            throw new RatingsServiceException(e);
          }
        }

        // RatingsSummary for Article
        List<RatingSummary> summaryList = session.createCriteria(RatingSummary.class)
                                                 .add(Restrictions.eq("annotates",articleURIStr))
                                                 .list();

        final boolean newRatingSummary;

        if (summaryList.size() == 0) {
          newRatingSummary = true;

          articleRatingSummary = new RatingSummary();
          articleRating.setMediator(getApplicationId());
          articleRatingSummary.setAnnotates(articleURI);
          articleRatingSummary.setContext("");
          articleRatingSummary.setCreated(now);
          articleRatingSummary.setBody(new RatingSummaryContent());
        } else {
          newRatingSummary = false;

          if (summaryList.size() == 1) {
            articleRatingSummary = summaryList.get(0);
          } else {
            // should never happen
            final String errorMessage = "Multiple RatingsSummary for Article " + articleURIStr +
                                        " and user: " + user.getUserId();
            final Exception e = new RuntimeException(errorMessage);
            log.error(errorMessage);
            throw new RatingsServiceException(e);
          }
        }

        if (!newRating && newRatingSummary) {
          // should never happen
          final String errorMessage = "No RatingsSummary exists for extant rating: " +
                                      "(Article: " + articleURIStr + ",User: " + user.getUserId();
          final Exception e = new RuntimeException(errorMessage);
          log.error(errorMessage);
          throw new RatingsServiceException(e);
        }

        if (newRating) {
          // if this is a new Rating, then the summary needs to be updated with
          // the number of users that rated the article.
          articleRatingSummary.getBody().setNumUsersThatRated(articleRatingSummary.getBody().getNumUsersThatRated() + 1);
        } else {
          // if this is a revised Rating, then the count remains the same, but
          // the old rating values must be removed.
          final int oldInsight = values.getBody().getInsightValue();
          final int oldReliability = values.getBody().getReliabilityValue();
          final int oldStyle = values.getBody().getStyleValue();

          if (oldInsight > 0) {
            articleRatingSummary.getBody().removeRating(Rating.INSIGHT_TYPE,oldInsight);
          }
          if (oldReliability > 0) {
            articleRatingSummary.getBody().removeRating(Rating.RELIABILITY_TYPE,oldReliability);
          }
          if (oldStyle > 0) {
            articleRatingSummary.getBody().removeRating(Rating.STYLE_TYPE,oldStyle);
          }
        }

        // update the Rating object with those values provided by the caller
        final int insight = values.getBody().getInsightValue();
        final int reliability = values.getBody().getReliabilityValue();
        final int style = values.getBody().getStyleValue();
        final String commentTitle = values.getBody().getCommentTitle();
        final String commentValue = values.getBody().getCommentValue();
        articleRating.getBody().setInsightValue(insight);
        articleRating.getBody().setReliabilityValue(reliability);
        articleRating.getBody().setStyleValue(style);
//      articleRating.setTitle(commentTitle);
        articleRating.getBody().setCommentTitle(commentTitle);
        articleRating.getBody().setCommentValue(commentValue);

        articleRatingSummary.getBody().addRating(Rating.INSIGHT_TYPE,insight);
        articleRatingSummary.getBody().addRating(Rating.RELIABILITY_TYPE,reliability);
        articleRatingSummary.getBody().addRating(Rating.STYLE_TYPE,style);

        session.saveOrUpdate(articleRating);
        session.saveOrUpdate(articleRatingSummary);
        tx.commit();
      } catch (OtmException e) {
        log.error("",e);
        tx.rollback();
      }
    } catch (OtmException e) {
      log.error("",e);
      throw new RatingsServiceException(e);
    } catch (URISyntaxException e) {
      log.error("",e);
      throw new RatingsServiceException(e);
    }
  }

  /**
   * Unflag a Rating
   *
   * @param ratingId the identifier of the Rating object to be unflagged
   *
   * @throws RatingServiceException
   */
  public void unflagRating(final String ratingId)
                  throws RatingsServiceException {
    setPublic(ratingId);
  }

  /**
   * Delete the Rating identified by ratingId and update the RatingSummary.
   *
   * @param ratingId the identifier of the Rating object to be deleted
   * @throws RatingsServiceException
   */
  private void deleteRating(final String ratingId)
                         throws RatingsServiceException {
    ensureInitGetsCalledWithUsersSessionAttributes();

    //pep.checkObjectAccess(RatingsPEP.SET_RATINGS,userURI,articleURI);

    try {
      final Transaction tx = session.beginTransaction();

      try {
        // Rating with this id
        List<Rating> ratingsList = session.createCriteria(Rating.class)
                                          .add(Restrictions.eq("id",ratingId))
                                          .list();

        if (ratingsList.size() <= 0) {
          final String errorMessage = "No Rating object found with id value of " + ratingId;
          log.error(errorMessage);
          throw new RatingsServiceException(errorMessage);
        }
        if (ratingsList.size() > 1) {
          final String errorMessage = "Multiple Rating objects found id value of " + ratingId;
          log.error(errorMessage);
          throw new RatingsServiceException(errorMessage);
        }

        final Rating articleRating = ratingsList.get(0);

        final URI articleURI = articleRating.getAnnotates();

        // RatingSummary for this article
        List<RatingSummary> summaryList = session.createCriteria(RatingSummary.class)
                                                 .add(Restrictions.eq("annotates",articleURI.toString()))
                                                 .list();

        if (summaryList.size() <= 0) {
          final String errorMessage = "No RatingSummary object found for article " +
                                      articleURI.toString();
          log.error(errorMessage);
          throw new RatingsServiceException(errorMessage);
        }
        if (summaryList.size() > 1) {
          final String errorMessage = "Multiple RatingSummary objects found found " +
                                      "for article " + articleURI.toString();
          log.error(errorMessage);
          throw new RatingsServiceException(errorMessage);
        }

        final RatingSummary articleRatingSummary = summaryList.get(0);

        articleRatingSummary.getBody().setNumUsersThatRated(articleRatingSummary.getBody().getNumUsersThatRated() - 1);
        final int insight = articleRating.getBody().getInsightValue();
        final int reliability = articleRating.getBody().getReliabilityValue();
        final int style = articleRating.getBody().getStyleValue();

        if (insight > 0) {
          articleRatingSummary.getBody().removeRating(Rating.INSIGHT_TYPE,insight);
        }
        if (reliability > 0) {
          articleRatingSummary.getBody().removeRating(Rating.RELIABILITY_TYPE,reliability);
        }
        if (style > 0) {
          articleRatingSummary.getBody().removeRating(Rating.STYLE_TYPE,style);
        }

        session.saveOrUpdate(articleRatingSummary);
        session.delete(articleRating);
        tx.commit();
      } catch (OtmException e) {
        log.error("",e);
        tx.rollback();
      }
    } catch (OtmException e) {
      log.error("",e);
    }
  }

  /**
   * Set the annotation as public.
   *
   * @param ratingId the id of the Rating
   * @throws RatingsServiceException
   */
  public void setPublic(final String ratingId) throws RatingsServiceException {
    ensureInitGetsCalledWithUsersSessionAttributes();

 // pep.checkAccess(pep.SET_ANNOTATION_STATE, URI.create(annotationDoi));

    final Rating result =
      TransactionHelper.doInTx(session,
                               new TransactionHelper.Action<Rating>() {
          public Rating run(Transaction tx) {
            Rating r = tx.getSession().get(Rating.class,ratingId);
            r.setState(PUBLIC_MASK);
            tx.getSession().saveOrUpdate(r);

            return r;
          }
        });
  }

  /**
   * Set the rating as flagged
   *
   * @param ratingId ratingId
   * @throws RatingsServiceException
   */
  public void setFlagged(final String ratingId) throws RatingsServiceException {
    ensureInitGetsCalledWithUsersSessionAttributes();

 // pep.checkAccess(pep.SET_ANNOTATION_STATE, URI.create(annotationId));

    final Rating result =
      TransactionHelper.doInTx(session,
                               new TransactionHelper.Action<Rating>() {
          public Rating run(Transaction tx) {
            Rating r = tx.getSession().get(Rating.class,ratingId);
            r.setState(PUBLIC_MASK | FLAG_MASK);
            tx.getSession().saveOrUpdate(r);

            return r;
          }
        });
  }

  /**
   * List the set of Ratings in a specific administrative state.
   *
   * @param mediator if present only those annotations that match this mediator are returned
   * @param state the state to filter the list of annotations by or 0 to return annotations in any
   *              administrative state
   *
   * @return an array of annotation metadata; if no matching annotations are found, an empty array
   *         is returned
   *
   * @throws RatingsServiceException if some error occurred
   */
  public RatingInfo[] listRatings(final String mediator,final int state)
                          throws RatingsServiceException {
    ensureInitGetsCalledWithUsersSessionAttributes();

//    pep.checkAccess(pep.LIST_ANNOTATIONS_IN_STATE, pep.ANY_RESOURCE);

    List<Rating> list =
      TransactionHelper.doInTx(session,
                               new TransactionHelper.Action<List<Rating>>() {
          public List<Rating> run(Transaction tx) {
            Criteria c = tx.getSession().createCriteria(Rating.class);

            if (mediator != null)
              c.add(Restrictions.eq("mediator",mediator));

            if (state == 0) {
              // this doesn't look elegant.
              c.add(Restrictions.ne("state", "0"));
            } else {
              c.add(Restrictions.eq("state", "" + state));
            }

            return c.list();
          }
        });

    final RatingInfo[] ratingInfos = new RatingInfo[list.size()];
    int position = 0;

    for (final Rating r : list)
      ratingInfos[position++] = new RatingInfo(r);

    return ratingInfos;
  }

  /**
   * Set the OTM session. Called by spring's bean wiring.
   *
   * @param session the otm session
   */
  @Required
  public void setOtmSession(Session session) {
    this.session = session;
  }

  /**
   * Set the PermissionWebService
   *
   * @param permissionWebService permissionWebService
   */
  @Required
  public void setPermissionWebService(final PermissionWebService permissionWebService) {
    this.permissions = permissionWebService;
  }
}
