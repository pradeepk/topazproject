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
import java.net.URI;
import java.net.URL;
import java.text.ParseException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.activation.DataHandler;
import javax.xml.rpc.ServiceException;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.input.CountingInputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.time.DateUtils;

import org.plos.article.util.ArticleUtil;
import org.plos.article.util.IngestException;
import org.plos.article.util.Ingester;
import org.plos.article.util.DuplicateArticleIdException;
import org.plos.article.util.NoSuchArticleIdException;
import org.plos.article.util.NoSuchObjectIdException;
import org.plos.article.util.Zip;
import org.plos.models.Article;
import org.plos.models.Category;
import org.plos.models.ObjectInfo;
import org.plos.service.BaseConfigurableService;
import org.plos.service.WSTopazContext;
import org.plos.util.TransactionHelper;

import org.springframework.beans.factory.annotation.Required;

import org.plos.configuration.ConfigurationStore;
import org.topazproject.fedora.client.FedoraAPIM;
import org.topazproject.fedoragsearch.service.FgsOperationsServiceLocator;
import org.topazproject.fedoragsearch.service.FgsOperations;
import org.topazproject.mulgara.itql.ItqlHelper;
import org.topazproject.otm.Criteria;
import org.topazproject.otm.Session;
import org.topazproject.otm.Transaction;
import org.topazproject.otm.criterion.Disjunction;
import org.topazproject.otm.criterion.Order;
import org.topazproject.otm.criterion.Restrictions;
import org.topazproject.otm.mapping.Mapper;
import org.topazproject.otm.query.Results;
import org.topazproject.otm.stores.ItqlStore.ItqlStoreConnection;

/**
 * Provide Article "services" via OTM.
 */
public class ArticleOtmService extends BaseConfigurableService {

  private String smallImageRep;
  private String largeImageRep;
  private String mediumImageRep;

  private ArticlePEP pep;
  private Session session;

  private static final Configuration CONF = ConfigurationStore.getInstance().getConfiguration();
  private static final Log log = LogFactory.getLog(ArticleOtmService.class);
  private static final List FGS_URLS = CONF.getList("topaz.fedoragsearch.urls.url");

  private final WSTopazContext ctx = new WSTopazContext(getClass().getName());

  @Override
  protected void init() throws IOException {
    if (pep == null)
      pep = new ArticlePEP();
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

    Transaction txn = null;
    try {
      ctx.activate();
      txn = session.beginTransaction();
      ItqlHelper itql = ((ItqlStoreConnection)txn.getConnection()).getItqlHelper();
      Ingester ingester = new Ingester(itql, ctx.getFedoraAPIM(), ctx.getFedoraUploader(),
                                       getFgsOperations());
      String ret = ingester.ingest(
        new Zip.DataSourceZip(
          new org.apache.axis.attachments.ManagedMemoryDataSource(dataHandler.getInputStream(),
                                                8192, "application/octet-stream", true)));
      txn.commit();
      txn = null;
      return ret;
    } catch(IOException ioe) {
      log.error("Ingestion failed: ", ioe);
      throw new IngestException("Ingestion failed: ", ioe);
    } finally {
      ctx.passivate();
      try {
        if (txn != null)
          txn.rollback();
      } catch (Throwable t) {
        log.debug("rollback failed", t);
      }
    }
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
   * @throws NoSuchArticleIdException NoSuchArticleIdException
   */
  public void markSuperseded(final String oldUri, final String newUri)
          throws NoSuchArticleIdException {
    ensureInitGetsCalledWithUsersSessionAttributes();

    // TODO
    throw new UnsupportedOperationException();
  }

  /**
   * Get the URL from which the objects contents can be retrieved via GET.
   *
   * @param obj uri
   * @param rep rep
   * @return the URL, or null if this object doesn't exist in the desired version
   * @throws RemoteException RemoteException
   * @throws NoSuchObjectIdException NoSuchObjectIdException
   */
  public String getObjectURL(final String obj, final String rep)
          throws RemoteException, NoSuchObjectIdException {
    // session housekeeping
    ensureInitGetsCalledWithUsersSessionAttributes();

    // ask PEP if getting Object URL is allowed
    // logged in user is automatically resolved by the ServletActionContextAttribute
    pep.checkAccess(ArticlePEP.GET_OBJECT_URL, URI.create(obj));

    return TransactionHelper.doInTxE(session,
                              new TransactionHelper.ActionE<String, NoSuchObjectIdException>() {
      public String run(Transaction tx) throws NoSuchObjectIdException {
        Article a = tx.getSession().get(Article.class, obj);
        if (a == null)
          throw new NoSuchObjectIdException(obj);

        return ArticleUtil.getFedoraDataStreamURL(a.getPid(), rep);
      }
    });
  }

  /**
   * Delete an article.
   *
   * @param article uri
   * @throws RemoteException RemoteException
   * @throws NoSuchArticleIdException NoSuchArticleIdException
   */
  public void delete(final String article)
    throws RemoteException, ServiceException, NoSuchArticleIdException {

    // session housekeeping
    ensureInitGetsCalledWithUsersSessionAttributes();

    // ask PEP if delete is allowed
    // logged in user is automatically resolved by the ServletActionContextAttribute
    pep.checkAccess(ArticlePEP.DELETE_ARTICLE, URI.create(article));

    Transaction txn = null;
    try {
      ctx.activate();
      txn = session.beginTransaction();
      ItqlHelper itql = ((ItqlStoreConnection)txn.getConnection()).getItqlHelper();
      ArticleUtil.delete(article, itql);
      txn = null;
    } finally {
      try {
        if (txn != null)
          txn.rollback();
      } catch (Throwable t) {
        log.debug("rollback failed", t);
      }
    }
  }

  /**
   * Change an articles state.
   *
   * @param article uri
   * @param state state
   * @throws NoSuchArticleIdException NoSuchArticleIdException
   */
  public void setState(final String article, final int state) throws NoSuchArticleIdException {
    ensureInitGetsCalledWithUsersSessionAttributes();

    pep.checkAccess(ArticlePEP.SET_ARTICLE_STATE, URI.create(article));

    TransactionHelper.doInTxE(session,
                              new TransactionHelper.ActionE<Void, NoSuchArticleIdException>() {
      public Void run(Transaction tx) throws NoSuchArticleIdException {
        Article a = tx.getSession().get(Article.class, article);
        if (a == null)
          throw new NoSuchArticleIdException(article);

        a.setState(state);
        for (ObjectInfo oi : a.getParts())
          oi.setState(state);
        for (Category c : a.getCategories())
          c.setState(state);

        tx.getSession().saveOrUpdate(a);
        return null;
      }
    });
  }

  /**
   * Get the ids of all articles satisfying the given criteria.
   *
   * @param startDate  is the date to start searching from. If null, start from begining of time.
   *                   Can be iso8601 formatted or string representation of Date object.
   * @param endDate    is the date to search until. If null, search until present date
   * @param states     the list of article states to search for (all states if null or empty)
   * @param ascending  controls the sort order (by date).
   * @return the (possibly empty) list of article ids.
   */
  public List<String> getArticleIds(String startDate, String endDate, int[] states,
                                    boolean ascending) throws Exception {
    ensureInitGetsCalledWithUsersSessionAttributes();

    final StringBuilder qry = new StringBuilder();
    qry.append("select art.id, art.date d from Article art where ");

    if (startDate != null)
      qry.append("ge(art.date, ").append(fmtLiteral(startDate, "date")).append(" and ");
    if (endDate != null)
      qry.append("le(art.date, ").append(fmtLiteral(endDate, "date")).append(" and ");

    if (states != null && states.length > 0) {
      qry.append("(");
      for (int state : states)
        qry.append("art.state = ").append(fmtLiteral(state, "state")).append(" or ");
      qry.setLength(qry.length() - 4);
      qry.append(")");
    }

    if (qry.indexOf(" and ", qry.length() - 5) > 0)
      qry.setLength(qry.length() - 4);

    qry.append("order by d ").append(ascending ? "asc" : "desc").append(";");

    List<URI> ids = TransactionHelper.doInTx(session, new TransactionHelper.Action<List<URI>>() {
      public List<URI> run(Transaction tx) {
        List<URI> uris = new ArrayList<URI>();
        Results r = tx.getSession().createQuery(qry.toString()).execute();
        while (r.next())
          uris.add(r.getURI(0));
        return uris;
      }
    });

    List<String> res = new ArrayList<String>();
    for (URI id : ids) {
      try {
        pep.checkAccess(ArticlePEP.READ_META_DATA, id);
        res.add(id.toString());
      } catch (SecurityException se) {
        if (log.isDebugEnabled())
          log.debug("Filtering URI " + id + " from Article list due to PEP SecurityException", se);
      }
    }

    return res;
  }

  private String fmtLiteral(Object val, String field) throws Exception {
    Mapper m = session.getSessionFactory().getClassMetadata(Article.class).getMapperByName(field);
    return "'" + m.getSerializer().serialize(val) + "'^^<" + m.getDataType() + ">";
  }

  /**
   * Get all articles satisfying the given criteria.
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
   * @throws ParseException if any of the dates could not be parsed
   */
  public Article[] getArticles(final String startDate, final String endDate,
                               final String[] categories, final String[] authors,
                               final int[] states, final boolean ascending)
      throws ParseException {
    ensureInitGetsCalledWithUsersSessionAttributes();

    return TransactionHelper.doInTxE(session,
                                     new TransactionHelper.ActionE<Article[], ParseException>() {
      public Article[] run(Transaction tx) throws ParseException {
        // get a list of Articles that meet the specified Criteria and Restrictions
        Map<String, Boolean> orderBy = new HashMap<String, Boolean>();
        orderBy.put("date", ascending);

        List<Article> articleList =
            findArticles(startDate, endDate, categories, authors, states, orderBy, 0, tx);
        return articleList.toArray(new Article[articleList.size()]);
      }
    });
  }

  /**
   * A full featured getArticles.
   *
   * @param startDate  is the date to start searching from. If null, start from begining of time.
   *                   Can be iso8601 formatted or string representation of Date object.
   * @param endDate    is the date to search until. If null, search until present date
   * @param categories is list of categories to search for articles within (all categories if null
   *                   or empty)
   * @param authors    is list of authors to search for articles within (all authors if null or
   *                   empty)
   * @param states     the list of article states to search for (all states if null or empty)
   * @param orderBy    controls the ordering. The keys specify the fields to be ordered, and the
   *                   values whether order is ascending (true) or descending (false).
   * @return the (possibly empty) list of articles.
   * @throws ParseException if any of the dates could not be parsed
   */
  public List<Article> getArticles(final String startDate, final String endDate,
                                   final String[] categories, final String[] authors,
                                   final int[] states, final Map<String, Boolean> orderBy,
                                   final int maxResults)
      throws ParseException {

    ensureInitGetsCalledWithUsersSessionAttributes();

    return TransactionHelper.doInTxE(session,
                               new TransactionHelper.ActionE<List<Article>, ParseException>() {
      public List<Article> run(Transaction tx) throws ParseException {
        // get a list of Articles that meet the specified Criteria and Restrictions
        List<Article> articleList =
            findArticles(startDate, endDate, categories, authors, states, orderBy, maxResults, tx);

        return articleList;
      }
    });
  }

  /**
   * Search for articles matching the specific criteria. Must be called with a transaction
   * active.
   */
  private List<Article> findArticles(String startDate, String endDate, String[] categories,
                                     String[] authors, int[] states, Map<String, Boolean> orderBy,
                                     int maxResults, Transaction tx)
      throws ParseException {
    ensureInitGetsCalledWithUsersSessionAttributes();

    // build up Criteria for the Articles
    Criteria articleCriteria = tx.getSession().createCriteria(Article.class);

    // normalize dates for query
    if (startDate != null) {
      articleCriteria.add(Restrictions.ge("date", parseDateParam(startDate)));
    }
    if (endDate != null) {
      articleCriteria.add(Restrictions.le("date", parseDateParam(endDate)));
    }

    // match all categories
    if (categories != null) {
      Criteria catCriteria = articleCriteria.createCriteria("categories");
      for (String category : categories) {
        catCriteria.add(Restrictions.eq("mainCategory", category));
      }
    }

    // match all authors
    if (authors != null) {
      for (String author : authors) {
        articleCriteria.add(Restrictions.eq("authors", author));
      }
    }

    // match all states
    if (states != null && states.length > 0) {
      Disjunction or = Restrictions.disjunction();
      for (int state : states) {
        or.add(Restrictions.eq("state", state));
      }
      articleCriteria.add(or);
    }

    // order by
    if (orderBy != null) {
      for (String field : orderBy.keySet()) {
        boolean ascending = (boolean) orderBy.get(field);
        articleCriteria.addOrder(ascending ? Order.asc(field) : Order.desc(field));
      }
    }

    // max results
    if (maxResults > 0) {
      articleCriteria.setMaxResults(maxResults);
    }

    // get a list of Articles that meet the specified Criteria and Restrictions
    List<Article> articleList = articleCriteria.list();

    // filter access by id with PEP
    // logged in user is automatically resolved by the ServletActionContextAttribute
    for (Iterator it = articleList.iterator(); it.hasNext(); ) {
      Article article = (Article) it.next();
      try {
        pep.checkAccess(ArticlePEP.READ_META_DATA, article.getId());
      } catch (SecurityException se) {
        it.remove();
        if (log.isDebugEnabled())
          log.debug("Filtering URI " + article.getId()
                    + " from Article list due to PEP SecurityException", se);
      }
    }

    return articleList;
  }

  /**
   * Get the Article's ObjectInfo by URI.
   *
   * @param uri uri
   * @return the object-info of the object
   * @throws NoSuchObjectIdException NoSuchObjectIdException
   */
  public ObjectInfo getObjectInfo(final String uri) throws NoSuchObjectIdException {

    // session housekeeping
    ensureInitGetsCalledWithUsersSessionAttributes();

    // sanity check parms
    if (uri == null) throw new IllegalArgumentException("URI == null");
    URI realURI = URI.create(uri);

    // filter access by id with PEP
    // logged in user is automatically resolved by the ServletActionContextAttribute
    try {
      pep.checkAccess(ArticlePEP.READ_META_DATA, realURI);
    } catch (SecurityException se) {
      if (log.isDebugEnabled()) {
        log.debug("Filtering URI "
          + uri
          + " from ObjectInfo list due to PEP SecurityException", se);
      }

      // it's still a SecurityException
      throw se;
    }

    return TransactionHelper.doInTxE(session,
                            new TransactionHelper.ActionE<ObjectInfo, NoSuchObjectIdException>() {
      public ObjectInfo run(Transaction tx) throws NoSuchObjectIdException {
        ObjectInfo objectInfo = tx.getSession().get(ObjectInfo.class, uri);
        if (objectInfo == null)
          throw new NoSuchObjectIdException(uri);
        return objectInfo;
      }
    });
  }

  /**
   * Get an Article by URI.
   *
   * @param uri URI of Article to get.
   * @return Article with specified URI or null if not found.
   * @throws NoSuchArticleIDException NoSuchArticleIdException
   */
  public Article getArticle(final URI uri) throws NoSuchArticleIdException {
    ensureInitGetsCalledWithUsersSessionAttributes();

    // sanity check parms
    if (uri == null) throw new IllegalArgumentException("URI == null");

    // filter access by id with PEP
    try {
      pep.checkAccess(ArticlePEP.READ_META_DATA, uri);
    } catch (SecurityException se) {
      if (log.isDebugEnabled()) {
        log.debug("Filtering URI "
          + uri
          + " due to PEP SecurityException", se);
      }

      // it's still a SecurityException
      throw se;
    }

    return TransactionHelper.doInTxE(session,
                            new TransactionHelper.ActionE<Article, NoSuchArticleIdException>() {
      public Article run(Transaction tx) throws NoSuchArticleIdException {
        Article article = tx.getSession().get(Article.class, uri.toString());
        if (article == null)
          throw new NoSuchArticleIdException(uri.toString());
        return article;
      }
    });
  }

  /**
   * Return the list of secondary objects
   * @param article uri
   * @return the secondary objects of the article
   * @throws NoSuchArticleIdException NoSuchArticleIdException
   */
  public SecondaryObject[] listSecondaryObjects(final String article)
      throws NoSuchArticleIdException {
    ensureInitGetsCalledWithUsersSessionAttributes();

    pep.checkAccess(ArticlePEP.LIST_SEC_OBJECTS, URI.create(article));

    return TransactionHelper.doInTxE(session,
                new TransactionHelper.ActionE<SecondaryObject[], NoSuchArticleIdException>() {
      public SecondaryObject[] run(Transaction tx) throws NoSuchArticleIdException {
        // get objects
        Article art = tx.getSession().get(Article.class, article);
        if (art == null)
          throw new NoSuchArticleIdException(article);

        /* sort the object list.
         *
         * TODO: the list should be stored using RdfSeq or RdfList instead of this next-object hack
         * so we can just use art.getParts()
         */
        List<ObjectInfo> sorted = new ArrayList<ObjectInfo>();
        for (ObjectInfo oi = art.getNextObject(); oi != null; oi = oi.getNextObject())
          sorted.add(oi);

        // convert to SecondaryObject's. TODO: can't we just return ObjectInfo?
        return convert(sorted.toArray(new ObjectInfo[sorted.size()]));
      }
    });
  }

  private SecondaryObject[] convert(final ObjectInfo[] objectInfos) {
    if (objectInfos == null) {
      return null;
    }
    SecondaryObject[] convertedObjectInfos = new SecondaryObject[objectInfos.length];
    for (int i = 0; i < objectInfos.length; i++) {
      convertedObjectInfos[i] = convert(objectInfos[i]);
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
  public Article[] getCommentedArticles(final int maxArticles) {

    // session housekeeping
    ensureInitGetsCalledWithUsersSessionAttributes();

    // sanity check args
    if (maxArticles < 0) {
      throw new IllegalArgumentException("Requesting a maximum # of commented articles < 0: " +  maxArticles);
    }
    if (maxArticles == 0) {
      return new Article[0];
    }

    final String oqlQuery =
      "select a, count(n) c from Article a, Annotation n where n.annotates = a order by c desc limit " + maxArticles;

    return TransactionHelper.doInTx(session, new TransactionHelper.Action<Article[]>() {
      public Article[] run(Transaction tx) {
        Results commentedArticles = tx.getSession().createQuery(oqlQuery).execute();

        // check access control on all Article results
        // logged in user is automatically resolved by the ServletActionContextAttribute
        ArrayList<Article> returnArticles = new ArrayList();

        commentedArticles.beforeFirst();
        while (commentedArticles.next()) {
          Article commentedArticle = (Article) commentedArticles.get("a");
          try {
            pep.checkAccess(ArticlePEP.READ_META_DATA, commentedArticle.getId());
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
    });
  }


  private SecondaryObject convert(final ObjectInfo objectInfo) {
    return new SecondaryObject(objectInfo, smallImageRep, mediumImageRep, largeImageRep);
  }

  /**
   * Create or update a representation of an object. The object itself must exist; if the specified
   * representation does not exist, it is created, otherwise the current one is replaced.
   *
   * @param objId    the URI of the object
   * @param rep      the name of this representation
   * @param content  the actual content that makes up this representation; if this contains a
   *                 content-type then that will be used; otherwise the content-type will be
   *                 set to <var>application/octet-stream</var>; may be null, in which case
   *                 the representation is removed.
   * @throws NoSuchObjectIdException if the object does not exist
   * @throws RemoteException if some other error occured
   * @throws NullPointerException if any of the parameters are null
   */
  public void setRepresentation(final String objId, final String rep, final DataHandler content)
      throws NoSuchObjectIdException, RemoteException {
    ensureInitGetsCalledWithUsersSessionAttributes();

    pep.checkAccess(ArticlePEP.SET_REPRESENTATION, URI.create(objId));

    try {
      TransactionHelper.doInTxE(session, new TransactionHelper.ActionE<Void, Exception>() {
        public Void run(Transaction tx) throws NoSuchObjectIdException, RemoteException {
          // get the object info
          ObjectInfo obj = tx.getSession().get(ObjectInfo.class, objId);
          if (obj == null)
            throw new NoSuchObjectIdException(objId);

          String ct = content.getContentType();
          if (ct == null)
            ct = "application/octet-stream";

          // update fedora
          long len = setDatastream(obj.getPid(), rep, ct, content);

          // update the rdf
          obj.getRepresentations().add(rep);

          obj.getData().put("topaz:" + rep + "-contentType", toList(ct));
          obj.getData().put("topaz:" + rep + "-objectSize", toList(Long.toString(len)));

          tx.getSession().saveOrUpdate(obj);

          // done
          return null;
        }
      });
    } catch (Exception e) {
      if (e instanceof RuntimeException)
        throw (RuntimeException) e;
      if (e instanceof NoSuchObjectIdException)
        throw (NoSuchObjectIdException) e;
      throw (RemoteException) e;
    }
  }

  private static <T> List<T> toList(T item) {
    List<T> res = new ArrayList<T>();
    res.add(item);
    return res;
  }

  private long setDatastream(String pid, String rep, String ct, DataHandler content)
      throws NoSuchObjectIdException, RemoteException {
    ctx.activate();
    try {
      FedoraAPIM apim = ctx.getFedoraAPIM();

      if (content != null) {
        CountingInputStream cis = new CountingInputStream(content.getInputStream());
        String reLoc = ctx.getFedoraUploader().upload(cis);
        try {
          apim.modifyDatastreamByReference(pid, rep, null, null, false, ct, null, reLoc, "A",
                                           "Updated datastream", false);
        } catch (RemoteException re) {
          if (!isNoSuchDatastream(re))
            throw re;

          if (log.isDebugEnabled())
            log.debug("representation '" + rep + "' for '" + pid + "' doesn't exist yet - " +
                      "creating it", re);

          apim.addDatastream(pid, rep, new String[0], "Represention", false, ct, null, reLoc, "M",
                             "A", "New representation");
        }

        return cis.getByteCount();
      } else {
        try {
          apim.purgeDatastream(pid, rep, null, "Purged datastream", false);
        } catch (RemoteException re) {
          if (!isNoSuchDatastream(re))
            throw re;

          if (log.isDebugEnabled())
            log.debug("representation '" + rep + "' for '" + pid + "' doesn't exist", re);
        }

        return 0;
      }
    } catch (RemoteException re) {
      if (isNoSuchObject(re))
        throw new NoSuchObjectIdException(pid, "object '" + pid + "' doesn't exist in fedora", re);
      else
        throw re;
    } catch (IOException ioe) {
      throw new RemoteException("Error uploading representation", ioe);
    } finally {
      ctx.passivate();
    }
  }

  private static boolean isNoSuchObject(RemoteException re) {
    // Ugh! What a hack...
    String msg = re.getMessage();
    return msg != null &&
           msg.startsWith("fedora.server.errors.ObjectNotInLowlevelStorageException:");
  }

  private static boolean isNoSuchDatastream(RemoteException re) {
    // Ugh! What a hack...
    String msg = re.getMessage();
    return msg != null && msg.equals("java.lang.Exception: Uncaught exception from Fedora Server");
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

  /**
   * Parse a kowari date into a java Date object.
   *
   * @param iso8601date is the date string to parse.
   * @return a java Date object.
   * @throws ParseException if there is a problem parsing the string
   */
  private static Date parseDate(String iso8601date) throws ParseException {
    // Obvious formats:
    final String[] defaultFormats = new String [] {
      "yyyy-MM-dd", "y-M-d", "y-M-d'T'H:m:s", "y-M-d'T'H:m:s.S",
      "y-M-d'T'H:m:s.Sz", "y-M-d'T'H:m:sz" };

    // TODO: Replace with fedora.server.utilities.DateUtility some how?
    // XXX: Deal with ' ' instead of 'T'
    // XXX: Deal with timezone in iso8601 format (not java's idea)

    return DateUtils.parseDate(iso8601date, defaultFormats);
  }

  /**
   * Convert a date passed in as a string to a Date object. Support both string representations
   * of the Date object and iso8601 formatted dates.
   *
   * @param date the string to convert to a Date object
   * @return a date object (or null if date is null)
   * @throws ParseException if unable to parse date
   */
  private static Date parseDateParam(String date) throws ParseException {
    if (date == null)
      return null;
    try {
      return new Date(date);
    } catch (IllegalArgumentException iae) {
      if (log.isDebugEnabled())
        log.debug("failed to parse date '" + date + "' use Date - trying iso8601 format", iae);
      return parseDate(date);
    }
  }
}
