/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.article.service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URI;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import javax.activation.DataHandler;
import javax.xml.rpc.ServiceException;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.plos.article.util.ArticleUtil;
import org.plos.article.util.IngestException;
import org.plos.article.util.Ingester;
import org.plos.article.util.DuplicateArticleIdException;
import org.plos.article.util.NoSuchArticleIdException;
import org.plos.article.util.NoSuchObjectIdException;
import org.plos.article.util.Zip;
import org.plos.configuration.OtmConfiguration;
import org.plos.models.Article;
import org.plos.service.BaseConfigurableService;
import org.plos.service.WSTopazContext;

import org.springframework.beans.factory.annotation.Required;

import org.topazproject.authentication.ProtectedService;
import org.topazproject.common.NoSuchIdException;
import org.topazproject.configuration.ConfigurationStore;
import org.topazproject.fedoragsearch.service.FgsOperationsServiceLocator;
import org.topazproject.fedoragsearch.service.FgsOperations;
import org.topazproject.feed.ArticleFeed;
import org.topazproject.otm.Criteria;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.Session;
import org.topazproject.otm.Transaction;
import org.topazproject.otm.criterion.Order;
import org.topazproject.otm.criterion.Restrictions;
import org.topazproject.otm.query.Results;
//import org.topazproject.ws.article.Article;
import org.topazproject.ws.article.ArticleClientFactory;
import org.topazproject.ws.article.ArticleInfo;
//import org.topazproject.ws.article.IngestException;
//import org.topazproject.ws.article.NoSuchArticleIdException;
//import org.topazproject.ws.article.NoSuchObjectIdException;
import org.topazproject.ws.article.ObjectInfo;

/**
 * Provide Article "services" via OTM.
 *
 * TODO: this is designed to provide the functionality of
 * {@link org.topazproject.ws.article.Article}.
 * It should be possible to refactor most of this "up" into
 * "org.plos.article.action".
 * Ideal solution is pure article.action, no article.service.
 */
public class ArticleOtmService extends BaseConfigurableService {

  // TODO: remove all refs to ws Article in favor of otm Article
  private org.topazproject.ws.article.Article delegateService;
  private String smallImageRep;
  private String largeImageRep;
  private String mediumImageRep;

  private ArticlePEP pep;
  private Session session;

  private static final Configuration CONF = ConfigurationStore.getInstance().getConfiguration();
  private static final Log log = LogFactory.getLog(ArticleOtmService.class);
  private static final List FGS_URLS = CONF.getList("topaz.fedoragsearch.urls.url");

  public void init() throws IOException, URISyntaxException, ServiceException {
    final ProtectedService permissionProtectedService = getProtectedService();
    // TODO: no refs to delegateService
    delegateService = null;  // ArticleClientFactory.create(permissionProtectedService);

    // create an XACML PEP for Articles
    try {
      pep = new ArticlePEP();
    } catch (Exception e) {
      throw new Error("Failed to create Article PEP", e);
    }
  }

  /**
   * Ingest an article.
   * @param dataHandler dataHandler
   * @return the uri of the article ingested
   * @throws RemoteException RemoteException
   * @throws ServiceException ServiceException
   * @throws IngestException IngestException
   * @throws DuplicateIdException DuplicateIdException
   */
  public String ingest(final DataHandler dataHandler)
          throws DuplicateArticleIdException, IngestException, RemoteException, ServiceException {

    // session housekeeping ...
    ensureInitGetsCalledWithUsersSessionAttributes();

    // ask PEP if ingest is allowed
    // logged in user is automatically resolved by the ServletActionContextAttribute
     pep.checkAccess(ArticlePEP.INGEST_ARTICLE, ArticlePEP.ANY_RESOURCE);

    // create an Ingester using the values from the WSTopazContext
    WSTopazContext ctx  = new WSTopazContext(getClass().getName());
    Ingester ingester =
      new Ingester(ctx.getItqlHelper(), ctx.getFedoraAPIM(), ctx.getFedoraUploader(), getFgsOperations());

    // let Ingester.ingest() do the real work
    return ingester.ingest(new Zip.DataSourceZip(dataHandler.getDataSource()));
  }

  /**
   * Ingest an article.
   * @param articleUrl articleUrl
   * @return the uri of the article ingested
   * @throws RemoteException RemoteException
   * @throws IngestException IngestException
   * @throws DuplicateIdException DuplicateIdException
   */
  public String ingest(final URL articleUrl)
          throws  DuplicateArticleIdException, IngestException, RemoteException, ServiceException {
    return ingest(new DataHandler(articleUrl));
  }

  /**
   * Mark an article as superseded by another article
   * @param oldUri oldUri
   * @param newUri newUri
   * @throws RemoteException RemoteException
   * @throws NoSuchIdException NoSuchIdException
   */
  public void markSuperseded(final String oldUri, final String newUri)
          throws RemoteException, NoSuchIdException {
    ensureInitGetsCalledWithUsersSessionAttributes();
    delegateService.markSuperseded(oldUri, newUri);
  }

  /**
   * Get the URL from which the objects contents can be retrieved via GET.
   *
   * @param obj uri
   * @param rep rep
   * @return the URL, or null if this object doesn't exist in the desired version
   * @throws RemoteException RemoteException
   * @throws NoSuchIdException NoSuchIdException
   */
  public String getObjectURL(final String obj, final String rep)
          throws RemoteException, ServiceException, NoSuchObjectIdException {

    // session housekeeping
    ensureInitGetsCalledWithUsersSessionAttributes();

    // ask PEP if getting Object URL is allowed
    // logged in user is automatically resolved by the ServletActionContextAttribute
     pep.checkAccess(ArticlePEP.GET_OBJECT_URL, URI.create(obj));

    // let the Article utils do the real work
    ArticleUtil articleUtil = null;
    try {
      articleUtil = new ArticleUtil();  // no arg, utils use default config
    } catch (MalformedURLException ex) {
      throw new RuntimeException(ex);
    }
    return articleUtil.getObjectURL(obj, rep);
  }

  /**
   * Delete an article.
   *
   * @param article uri
   * @throws RemoteException RemoteException
   * @throws NoSuchIdException NoSuchIdException
   */
  public void delete(final String article)
    throws RemoteException, ServiceException, NoSuchArticleIdException {

    // session housekeeping
    ensureInitGetsCalledWithUsersSessionAttributes();

    // ask PEP if delete is allowed
    // logged in user is automatically resolved by the ServletActionContextAttribute
     pep.checkAccess(ArticlePEP.DELETE_ARTICLE, URI.create(article));

     // let the Article utils do the real work
     ArticleUtil articleUtil = null;
     try {
      articleUtil = new ArticleUtil();  // no arg, utils use default config
     } catch (MalformedURLException ex) {
       throw new RuntimeException(ex);
     }
     articleUtil.delete(article);
  }

  /**
   * Change an articles state.
   * @param article uri
   * @param state state
   * @throws RemoteException RemoteException
   * @throws NoSuchIdException NoSuchIdException
   */
  public void setState(final String article, final int state)
          throws RemoteException, NoSuchIdException {
    ensureInitGetsCalledWithUsersSessionAttributes();
    delegateService.setState(article, state);
  }

  /**
   * Get a list of all articles
   * @param startDate startDate
   * @param endDate endDate
   * @return list of article uri's
   * @throws java.rmi.RemoteException RemoteException
   */
  public String getArticles(final String startDate, final String endDate) throws RemoteException {
    ensureInitGetsCalledWithUsersSessionAttributes();

    return getArticles(
      startDate,
      endDate,
      null,   // convention for all states
      true);  // ascending
  }

  /*  * @param startDate  is the date to start searching from. If null, start from begining of time.
  *                   Can be iso8601 formatted or string representation of Date object.
  * @param endDate    is the date to search until. If null, search until present date
  * @param states     the list of article states to search for (all states if null or empty)
  * @param ascending  controls the sort order (by date). If used for RSS feeds, decending would
  *                   be appropriate. For archive display, ascending would be appropriate.
  * @return the xml for the specified feed
  * @throws RemoteException if there was a problem talking to the alerts service
  */
  public String getArticles(final String startDate, final String endDate,
                 final int[] states, boolean ascending) throws RemoteException {
    ensureInitGetsCalledWithUsersSessionAttributes();
    return delegateService.getArticles(startDate, endDate, null, null, states, true);
  }

  /**
   * Get list of articles for a given set of categories, authors and states bracked by specified
   * times.
   *
   * @param startDate  is the date to start searching from. If null, start from begining of time.
   *                   Can be iso8601 formatted or string representation of Date object.
   * @param endDate    is the date to search until. If null, search until present date
   * @param categories is list of categories to search for articles within (all categories if null
   *                   or empty)
   * @param authors    is list of authors to search for articles within (all authors if null or
   *                   empty)
   * @param states     the list of article states to search for (all states if null or empty)
   * @param ascending  controls the sort order (by date). If used for RSS feeds, decending would
   *                   be appropriate. For archive display, ascending would be appropriate.
   * @return the (possibly empty) list of articles.
   * @throws RemoteException if there was a problem talking to any service
   */
  public Article[] getArticles(String startDate,String endDate,
                                String[] categories, String[] authors, int[] states,
                                boolean ascending)
    throws RemoteException {

    // session housekeeping
    ensureInitGetsCalledWithUsersSessionAttributes();

    // build up Criteria for the Articles
    Criteria articleCriteria = session.createCriteria(Article.class);

    // normalize dates for query
    articleCriteria = articleCriteria.add(Restrictions.ge("date", ArticleFeed.parseDateParam(startDate)));
    articleCriteria = articleCriteria.add(Restrictions.le("date", ArticleFeed.parseDateParam(endDate)));

    // match all categories
    if (categories != null) {
      for (String category : categories) {
        articleCriteria = articleCriteria.add(Restrictions.eq("mainCategory", category));
      }
    }

    // match all authors
    if (authors != null) {
      for (String author : authors) {
        articleCriteria = articleCriteria.add(Restrictions.eq("creator", author));
      }
    }

    // match all states
    if (states != null) {
      for (int state : states) {
        articleCriteria = articleCriteria.add(Restrictions.eq("articleState", state));
      }
    }

    // order by date
    if (ascending) {
        articleCriteria = articleCriteria.addOrder(Order.asc("date"));
    } else {
        articleCriteria = articleCriteria.addOrder(Order.desc("date"));
    }

    // get a list of Articles that meet the specified Criteria and Restrictions
    List<Article> articleList = articleCriteria.list();

    // filter access by id with PEP
    for (Iterator it = articleList.iterator(); it.hasNext(); ) {
      Article article = (Article) it.next();
      try {
        pep.checkAccess(pep.READ_META_DATA, article.getId());
      } catch (SecurityException se) {
        it.remove();
        if (log.isDebugEnabled()) {
          log.debug("Filtering URI "
            + article.getId()
            + " from Article list due to PEP SecurityException", se);
        }
      }
    }

    return articleList.toArray(new Article[articleList.size()]);
  }

  /**
   * Get the Article's ObjectInfo by URI.
   *
   * @param uri uri
   * @return the object-info of the object
   * @throws org.topazproject.ws.article.NoSuchObjectIdException NoSuchObjectIdException
   * @throws java.rmi.RemoteException RemoteException
   */
  // TODO: remove FQ package name when no more ws ObjectInfo
  public org.plos.models.ObjectInfo getObjectInfo(final String uri)
    throws RemoteException, NoSuchObjectIdException {

    // session housekeeping
    ensureInitGetsCalledWithUsersSessionAttributes();

    // sanity check parms
    if (uri == null) throw new IllegalArgumentException("URI == null");
    URI realURI = URI.create(uri);

    // filter access by id with PEP
    try {
      pep.checkAccess(pep.READ_META_DATA, realURI);
    } catch (SecurityException se) {
      if (log.isDebugEnabled()) {
        log.debug("Filtering URI "
          + uri
          + " from ObjectInfo list due to PEP SecurityException", se);
        // it's still a SecurityException
        throw se;
      }
    }

    // build up Criteria for the ObjectInfo
    Criteria objectInfoCriteria = session.createCriteria(org.plos.models.ObjectInfo.class);

    // match on URI
    objectInfoCriteria = objectInfoCriteria.add(Restrictions.eq("id", realURI));

    // get a list of ObjectInfos that meet the specified Criteria and Restrictions
    List<org.plos.models.ObjectInfo> objectInfoList = objectInfoCriteria.list();

    // should be 1 & only 1 result
    if (objectInfoList.size() == 0) {
      throw new NoSuchObjectIdException("no ObjectInfo for URI: \"" + uri + "\"");
    }
    if (objectInfoList.size() > 1) {
      throw new RuntimeException("multiple," +  objectInfoList.size() + ", ObjectInfos for URI: \"" + uri + "\"");
    }

    return objectInfoList.get(0);
  }

  /**
   * Return the list of secondary objects
   * @param article uri
   * @return the secondary objects of the article
   * @throws java.rmi.RemoteException RemoteException
   * @throws org.topazproject.ws.article.NoSuchArticleIdException NoSuchArticleIdException
   */
  public SecondaryObject[] listSecondaryObjects(final String article)
    throws RemoteException, org.topazproject.ws.article.NoSuchArticleIdException {

    ensureInitGetsCalledWithUsersSessionAttributes();
    return convert(delegateService.listSecondaryObjects(article));
  }

  private SecondaryObject[] convert(final ObjectInfo[] objectInfos) {
    if (objectInfos == null) {
      return null;
    }
    SecondaryObject[] convertedObjectInfos = new SecondaryObject[objectInfos.length];
    for (int i = 0; i < objectInfos.length; i++) {
      convertedObjectInfos[i] = convert (objectInfos[i]);
    }
    return convertedObjectInfos;
  }

  /**
   * Get the most commented Articles.
   * The actual # of Articles returned maybe < maxArticles
   * as PEP filtering is done on the results.
   *
   * @param maxArticles Maximum # of Articles to retrieve.
   * @returns Article[] of most commented Articles.
   */
  public Article[] getCommentedArticles(int maxArticles) throws RemoteException {

    // session housekeeping
    ensureInitGetsCalledWithUsersSessionAttributes();

    // sanity check args
    if (maxArticles < 0) {
      throw new IllegalArgumentException("Requesting a maximum # of commented articles < 0: " +  maxArticles);
    }
    if (maxArticles == 0) {
      return new Article[0];
    }

    String oqlQuery =
      "select a, count(n) c from Article a, Annotation n where n.annotates = a order by c desc limit " + maxArticles;

    Results commentedArticles = session.doQuery(oqlQuery);

    // check access control on all Article results
    ArrayList<Article> returnArticles = new ArrayList();
    commentedArticles.beforeFirst();
    while(commentedArticles.next()) {
      Article commentedArticle = (Article) commentedArticles.get("a");
      try {
        pep.checkAccess(pep.READ_META_DATA, commentedArticle.getId());
        returnArticles.add(commentedArticle);
      } catch (SecurityException se) {
         if (log.isDebugEnabled())
          log.debug("Filtering URI "
            + commentedArticle.getId()
            + " from commented Article list due to PEP SecurityException", se);
      }
    }

    return returnArticles.toArray(new Article[returnArticles.size()]);
  }


  private SecondaryObject convert(final ObjectInfo objectInfo) {
    return new SecondaryObject(objectInfo, smallImageRep, mediumImageRep, largeImageRep);
  }

  /**
   * Create or update a representation of an object. The object itself must exist; if the specified
   * representation does not exist, it is created, otherwise the current one is replaced.
   *
   * @param obj      the URI of the object
   * @param rep      the name of this representation
   * @param content  the actual content that makes up this representation; if this contains a
   *                 content-type then that will be used; otherwise the content-type will be
   *                 set to <var>application/octet-stream</var>; may be null, in which case
   *                 the representation is removed.
   * @throws NoSuchObjectIdException if the object does not exist
   * @throws RemoteException if some other error occured
   * @throws NullPointerException if any of the parameters are null
   */
  public void setRepresentation(String obj, String rep, DataHandler content)
      throws org.topazproject.ws.article.NoSuchObjectIdException, RemoteException
  {
          ensureInitGetsCalledWithUsersSessionAttributes();
          delegateService.setRepresentation(obj, rep, content);
  }

  /**
   * Set the small image representation
   * @param smallImageRep smallImageRep
   */
  public void setSmallImageRep(final String smallImageRep) {
    this.smallImageRep = smallImageRep;
  }

  /**
   * Set the medium image representation
   * @param mediumImageRep mediumImageRep
   */
  public void setMediumImageRep(final String mediumImageRep) {
    this.mediumImageRep = mediumImageRep;
  }

  /**
   * Set the large image representation
   * @param largeImageRep largeImageRep
   */
  public void setLargeImageRep(final String largeImageRep) {
    this.largeImageRep = largeImageRep;
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

  // methods & functionality that were "pulled up" from org.topazproject.ws.article.impl.ArticleImpl

  private static FgsOperations[] getFgsOperations() throws ServiceException {

    FgsOperations ops[] = new FgsOperations[FGS_URLS.size()];

    for (int i = 0; i < ops.length; i++) {
      String url = FGS_URLS.get(i).toString();
      try {
        ops[i] = new FgsOperationsServiceLocator().getOperations(new URL(url));
      } catch (MalformedURLException mue) {
        throw new ServiceException("Invalid fedoragsearch URL '" + url + "'", mue);
      }
      if (ops[i] == null)
        throw new ServiceException(
          "Unable to create fedoragsearch service at '" + url + "'");
    }

    return ops;
  }
}
