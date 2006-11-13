/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.ws.article.impl;

import java.io.FilterInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.activation.DataHandler;
import javax.xml.rpc.ServiceException;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.net.URLCodec;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.authentication.ProtectedService;
import org.topazproject.common.impl.TopazContext;
import org.topazproject.common.impl.SimpleTopazContext;
import org.topazproject.configuration.ConfigurationStore;

import org.topazproject.mulgara.itql.AnswerException;
import org.topazproject.mulgara.itql.StringAnswer;
import org.topazproject.mulgara.itql.AnswerSet;
import org.topazproject.mulgara.itql.ItqlHelper;

import org.topazproject.fedora.client.APIMStubFactory;
import org.topazproject.fedora.client.Uploader;
import org.topazproject.fedora.client.FedoraAPIM;

import org.topazproject.ws.article.Article;
import org.topazproject.ws.article.DuplicateArticleIdException;
import org.topazproject.ws.article.IngestException;
import org.topazproject.ws.article.NoSuchObjectIdException;
import org.topazproject.ws.article.NoSuchArticleIdException;
import org.topazproject.ws.article.ObjectInfo;
import org.topazproject.ws.article.RepresentationInfo;

import org.topazproject.fedoragsearch.service.FgsOperationsServiceLocator;
import org.topazproject.fedoragsearch.service.FgsOperations;

import org.topazproject.feed.ArticleFeed;

import org.topazproject.common.impl.DoiUtil;

/** 
 * The default implementation of the article manager.
 * 
 * @author Ronald Tschalär
 */
public class ArticleImpl implements Article {
  private static final Log           log   = LogFactory.getLog(ArticleImpl.class);
  private static final Configuration CONF  = ConfigurationStore.getInstance().getConfiguration();
  private static final String        MODEL = "<" + CONF.getString("topaz.models.articles") + ">";

  private static final String        FGS_URL   = CONF.getString("topaz.fedoragsearch.url");
  private static final String        FGS_REPO  = CONF.getString("topaz.fedoragsearch.repository");

  private static final String ITQL_DELETE_OBJ =
      ("delete select $s $p $o from ${MODEL} where $s $p $o and " +
       "  $s <tucana:is> <${subj}> from ${MODEL};").
      replaceAll("\\Q${MODEL}", MODEL);

  private static final String ITQL_DELETE_REP =
      ("delete select $s $p $o from ${MODEL} where $s $p $o and $s <tucana:is> <${subj}> and (" +
       "    $p <tucana:is> <topaz:hasRepresentation> and $o <tucana:is> '${rep}' or " +
       "    $p <tucana:is> <topaz:${rep}-objectSize> or " +
       "    $p <tucana:is> <topaz:${rep}-contentType> " +
       "  ) from ${MODEL};").
      replaceAll("\\Q${MODEL}", MODEL);

  private static final String ITQL_CREATE_REP =
      ("insert " +
       "    <${subj}> <topaz:hasRepresentation> '${rep}' " +
       "    <${subj}> <topaz:${rep}-objectSize> '${size}'^^<http://www.w3.org/2001/XMLSchema#int>" +
       "    <${subj}> <topaz:${rep}-contentType> '${type}' " +
       "  into ${MODEL};").
      replaceAll("\\Q${MODEL}", MODEL);

  private static final String ITQL_FIND_OBJS =
      ("select $obj from ${MODEL} where " +
          // ensure it's an article
       "  <${subj}> <rdf:type> <topaz:Article> and (" +
          // find all related objects
       "  <${subj}> <dc_terms:hasPart> $obj or <${subj}> <topaz:hasCategory> $obj " +
          // find the article itself
       "  or $obj <tucana:is> <${subj}> );").
      replaceAll("\\Q${MODEL}", MODEL);

  private static final String ITQL_GET_OBJ_INFO =
      ("select $p $o from ${MODEL} where <${subj}> $p $o;").
      replaceAll("\\Q${MODEL}", MODEL);

  private static final String ITQL_LIST_SEC_OBJS =
      ("select $prev $cur subquery(select $p $o from ${MODEL} where $cur $p $o) " +
       "  from ${MODEL} where " +
       "  <${art}> <rdf:type> <topaz:Article> and " +
       "  walk(<${art}> <topaz:nextObject> $cur and $prev <topaz:nextObject> $cur);").
      replaceAll("\\Q${MODEL}", MODEL);

  private static final String ITQL_TEST_ARTICLE =
      ("select $id from ${MODEL} where " +
       "  $id <rdf:type> <topaz:Article> and $id <tucana:is> <${art}>;").
      replaceAll("\\Q${MODEL}", MODEL);

  private final URI        fedoraServer;
  private final Ingester   ingester;
  private final ArticlePEP pep;
  private final TopazContext ctx;
  private final FgsOperations fgs;

  /**
   * Creates a new ArticleImpl object.
   *
   * @param pep The xacml pep
   * @param ctx The topaz api context
   */
  public ArticleImpl(ArticlePEP pep, TopazContext ctx) throws ServiceException {
    this.pep      = pep;
    this.ctx      = ctx;
    this.fgs      = getFgsOperations();
    this.ingester = new Ingester(pep, ctx, fgs);
    this.fedoraServer = ctx.getFedoraBaseUri();
  }

  /** 
   * Create a new article manager instance. 
   *
   * @param fedoraSvc  the fedora management web-service
   * @param uploadSvc  the fedora management web-service
   * @param mulgaraSvc the mulgara web-service
   * @param hostname   the hostname under which this service is visible; this is used to generate
   *                   the proper URL for {@link #getObjectURL getObjectURL}.
   * @param pep        the policy-enforcer to use for access-control
   */
  public ArticleImpl(ProtectedService fedoraSvc, ProtectedService uploadSvc, 
                     ProtectedService mulgaraSvc, String hostname, ArticlePEP pep)
      throws URISyntaxException, IOException, ServiceException {
    URI fedoraURI = new URI(fedoraSvc.getServiceUri());
    fedoraServer = getRemoteFedoraURI(fedoraURI, hostname);

    this.fgs = getFgsOperations();

    Uploader uploader = new Uploader(uploadSvc);
    FedoraAPIM apim = APIMStubFactory.create(fedoraSvc);

    ItqlHelper itql     = new ItqlHelper(mulgaraSvc);

    ctx = new SimpleTopazContext(itql, apim, uploader);
    ingester = new Ingester(pep, ctx, fgs);

    this.pep = pep;
  }

  private static FgsOperations getFgsOperations() throws ServiceException {
    try {
      FgsOperations ops = new FgsOperationsServiceLocator().getOperations(new URL(FGS_URL));
      if (ops == null)
        throw new ServiceException("Unable to create fedoragsearch service at " + FGS_URL);
      return ops;
    } catch (MalformedURLException mue) {
      throw new ServiceException("Invalid fedoragsearch URL - " + FGS_URL, mue);
    }
  }

  private static URI getRemoteFedoraURI(URI fedoraURI, String hostname) {
    if (!fedoraURI.getHost().equals("localhost"))
      return fedoraURI;         // it's already remote

    try {
      return new URI(fedoraURI.getScheme(), null, hostname, fedoraURI.getPort(),
                     fedoraURI.getPath(), null, null);
    } catch (URISyntaxException use) {
      throw new Error(use);   // Can't happen
    }
  }


  public String ingest(DataHandler zip) throws DuplicateArticleIdException, IngestException {
    return ingester.ingest(new Zip.DataSourceZip(zip.getDataSource()));
  }

  public void markSuperseded(String oldArt, String newArt)
      throws NoSuchArticleIdException, RemoteException {
    // FIXME: should pass both URI's
    pep.checkAccess(pep.INGEST_ARTICLE, ItqlHelper.validateUri(newArt, "newArt"));
    ItqlHelper.validateUri(oldArt, "oldArt");

    ctx.getItqlHelper().doUpdate("insert <" + oldArt + "> <dc_terms:isReplacedBy> <" + newArt +
                                 "> <" + newArt + "> <dc_terms:replaces> <" + oldArt +
                                 "> into " + MODEL + ";", null);
  }

  public void setState(String article, int state) throws NoSuchArticleIdException, RemoteException {
    pep.checkAccess(pep.SET_ARTICLE_STATE, ItqlHelper.validateUri(article, "article"));

    try {
      ctx.getFedoraAPIM().modifyObject(DoiUtil.uri2PID(article), state2Str(state),
                                       null, "Changed state");
    } catch (RemoteException re) {
      FedoraUtil.detectNoSuchArticleIdException(re, article);
    }
  }

  public void delete(String article, boolean purge)
      throws NoSuchArticleIdException, RemoteException {
    pep.checkAccess(pep.DELETE_ARTICLE, ItqlHelper.validateUri(article, "article"));

    ItqlHelper itql = ctx.getItqlHelper();
    FedoraAPIM apim = ctx.getFedoraAPIM();
    String txn = purge ? "delete " + article : null;
    try {
      if (txn != null)
        itql.beginTxn(txn);

      String[] objList = findAllObjects(article);
      if (log.isDebugEnabled())
        log.debug("deleting all objects for uri '" + article + "'");

      for (int idx = 0; idx < objList.length; idx++) {
        if (log.isDebugEnabled())
          log.debug("deleting uri '" + objList[idx] + "'");

        if (purge) {
          apim.purgeObject(DoiUtil.uri2PID(objList[idx]), "Purged object", false);
          itql.doUpdate(ItqlHelper.bindValues(ITQL_DELETE_OBJ, "subj", objList[idx]), null);
        } else {
          apim.modifyObject(DoiUtil.uri2PID(objList[idx]), "D", null, "Deleted object");
        }

        // Remove article from full-text index first
        String pid = DoiUtil.uri2PID(objList[idx]);
        String result = fgs.updateIndex("deletePid", pid, FGS_REPO, null, null, null);
        if (log.isDebugEnabled())
          log.debug("Removed " + pid + " from full-text index:\n" + result);
      }

      if (txn != null) {
        itql.commitTxn(txn);
        txn = null;
      }
    } catch (AnswerException ae) {
      throw new RemoteException("Error querying RDF", ae);
    } finally {
      if (txn != null)
        itql.rollbackTxn(txn);
    }
  }

  public String getObjectURL(String obj, String rep)
      throws NoSuchObjectIdException, RemoteException {
    pep.checkAccess(pep.GET_OBJECT_URL, ItqlHelper.validateUri(obj, "object"));

    String path = "/fedora/get/" + DoiUtil.uri2PID(obj) + "/" + rep;
    return fedoraServer.resolve(path).toString();
  }

  public String getArticles(String startDate, String endDate,
                            String[] categories, String[] authors,
                            boolean ascending) throws RemoteException {
    Date start = ArticleFeed.parseDateParam(startDate);
    Date end = ArticleFeed.parseDateParam(endDate);

    ItqlHelper itql = ctx.getItqlHelper();
    try {
      String articlesQuery = ArticleFeed.getQuery(start, end, categories, authors, false);
      StringAnswer articlesAnswer = new StringAnswer(itql.doQuery(articlesQuery, null));
      Map articles = ArticleFeed.getArticlesSummary(articlesAnswer);

      for (Iterator it = articles.keySet().iterator(); it.hasNext(); ) {
        String uri = (String) it.next();
        try {
          pep.checkAccess(pep.READ_META_DATA, URI.create(uri));
        } catch (SecurityException se) {
          articles.remove(uri);
          if (log.isDebugEnabled())
            log.debug(uri, se);
        }
      }

      String detailsQuery = ArticleFeed.getDetailsQuery(articles.values());
      StringAnswer detailsAnswer = new StringAnswer(itql.doQuery(detailsQuery, null));
      ArticleFeed.addArticlesDetails(articles, detailsAnswer);

      return ArticleFeed.buildXml(articles.values());
    } catch (AnswerException ae) {
      throw new RemoteException("Error querying RDF", ae);
    }
  }

  public ObjectInfo[] listSecondaryObjects(String article)
      throws NoSuchArticleIdException, RemoteException {
    pep.checkAccess(pep.LIST_SEC_OBJECTS, ItqlHelper.validateUri(article, "article"));

    ItqlHelper itql = ctx.getItqlHelper();
    try {
      AnswerSet ans =
          new AnswerSet(itql.doQuery(ItqlHelper.bindValues(ITQL_LIST_SEC_OBJS, "art", article), null));
      ans.next();
      AnswerSet.QueryAnswerSet rows = ans.getQueryResults();

      if (!rows.next() && !articleExists(itql, article))
        throw new NoSuchArticleIdException(article);
      rows.beforeFirst();

      Map loc = new HashMap();
      while (rows.next()) {
        ObjectInfo info = new ObjectInfo();
        info.setUri(rows.getString("cur"));

        loc.put(rows.getString("prev"), info);

        AnswerSet.QueryAnswerSet sqa = rows.getSubQueryResults(2);
        parseObjectInfo(sqa, info);
      }

      List infos = new ArrayList();
      for (ObjectInfo info = (ObjectInfo) loc.get(article); info != null;
           info = (ObjectInfo) loc.get(info.getUri()))
        infos.add(info);

      return (ObjectInfo[]) infos.toArray(new ObjectInfo[infos.size()]);
    } catch (AnswerException ae) {
      throw new RemoteException("Error querying RDF", ae);
    }
  }

  protected static String state2Str(int state) {
    switch (state) {
      case ST_ACTIVE:
        return "A";
      case ST_DISABLED:
        return "D";
      default:
        throw new IllegalArgumentException("Unknown state '" + state + "'");
    }
  }

  protected String[] findAllObjects(String subj)
      throws NoSuchArticleIdException, RemoteException, AnswerException {
    ItqlHelper.validateUri(subj, "subject");

    ItqlHelper itql = ctx.getItqlHelper();
    StringAnswer ans =
        new StringAnswer(itql.doQuery(ItqlHelper.bindValues(ITQL_FIND_OBJS, "subj", subj), null));
    List uris = ((StringAnswer.StringQueryAnswer) ans.getAnswers().get(0)).getRows();
    if (uris.size() == 0)
      throw new NoSuchArticleIdException(subj);

    String[] res = new String[uris.size()];
    for (int idx = 0; idx < res.length; idx++)
      res[idx] = ((String[]) uris.get(idx))[0];

    return res;
  }

  /**
   * Check if the given article exists.
   *
   * @param art the article's uri
   * @return true if the article exists
   * @throws RemoteException if an error occurred talking to the db
   */
  protected boolean articleExists(ItqlHelper itql, String art) throws RemoteException {
    try {
      StringAnswer ans =
          new StringAnswer(itql.doQuery(ItqlHelper.bindValues(ITQL_TEST_ARTICLE, "art", art), null));
      List rows = ((StringAnswer.StringQueryAnswer) ans.getAnswers().get(0)).getRows();
      return rows.size() > 0;
    } catch (AnswerException ae) {
      throw new RemoteException("Error testing if article '" + art + "' exists", ae);
    }
  }

  public void setRepresentation(String obj, String rep, DataHandler content)
      throws NoSuchObjectIdException, RemoteException {
    // FIXME: should pass 'rep' too
    pep.checkAccess(pep.SET_REPRESENTATION, ItqlHelper.validateUri(obj, "object"));

    if (rep == null)
      throw new NullPointerException("representation may not be null");

    ItqlHelper itql = ctx.getItqlHelper();
    FedoraAPIM apim = ctx.getFedoraAPIM();
    String txn = "set-rep " + obj + "|" + rep;
    try {
      itql.beginTxn(txn);

      if (log.isDebugEnabled())
        log.debug("setting representation '" + rep + "' for '" + obj + "'");

      itql.doUpdate(ItqlHelper.bindValues(ITQL_DELETE_REP, "subj", obj,
                                          "rep", ItqlHelper.escapeLiteral(rep)), null);

      if (content != null) {
        String ct = content.getContentType();
        if (ct == null)
          ct = "application/octet-stream";

        ByteCounterInputStream bcis = new ByteCounterInputStream(content.getInputStream());
        String reLoc = ctx.getFedoraUploader().upload(bcis);
        try {
          apim.modifyDatastreamByReference(DoiUtil.uri2PID(obj), rep, null, null, false, ct,
                                           null, reLoc, "A", "Updated datastream", false);
        } catch (RemoteException re) {
          if (!isNoSuchDatastream(re))
            throw re;

          if (log.isDebugEnabled())
            log.debug("representation '" + rep + "' for '" + obj + "' doesn't exist yet - " +
                      "creating it", re);
          apim.addDatastream(DoiUtil.uri2PID(obj), rep, new String[0], "Represention", false, ct,
                             null, reLoc, "M", "A", "New representation");
        }

        Map map = new HashMap();
        map.put("subj", obj);
        map.put("rep", ItqlHelper.escapeLiteral(rep));
        map.put("size", Long.toString(bcis.getLength()));
        map.put("type", ItqlHelper.escapeLiteral(ct));
        itql.doUpdate(ItqlHelper.bindValues(ITQL_CREATE_REP, map), null);
      } else {
        try {
          apim.purgeDatastream(DoiUtil.uri2PID(obj), rep, null, "Purged datastream", false);
        } catch (RemoteException re) {
          if (!isNoSuchDatastream(re))
            throw re;

          if (log.isDebugEnabled())
            log.debug("representation '" + rep + "' for '" + obj + "' doesn't exist", re);
        }
      }

      itql.commitTxn(txn);
      txn = null;
    } catch (RemoteException re) {
      if (isNoSuchObject(re))
        throw (NoSuchObjectIdException) new NoSuchObjectIdException(obj).initCause(re);
      else
        throw re;
    } catch (IOException ioe) {
      throw new RemoteException("Error uploading representation", ioe);
    } finally {
      if (txn != null)
        itql.rollbackTxn(txn);
    }
  }

  private static boolean isNoSuchObject(RemoteException re) {
    // Ugh! What a hack...
    String msg = re.getMessage();
    return (msg != null &&
            msg.startsWith("fedora.server.errors.ObjectNotInLowlevelStorageException:"));
  }

  private static boolean isNoSuchDatastream(RemoteException re) {
    // Ugh! What a hack...
    String msg = re.getMessage();

    // Fedora 2.1.0: return (msg != null && msg.startsWith("java.lang.NullPointerException:"));

    // Fedora 2.1.1:
    return (msg != null && msg.equals("java.lang.Exception: Uncaught exception from Fedora Server"));
  }

  private static class ByteCounterInputStream extends FilterInputStream {
    private long cnt = 0;
    private long cntMark = 0;

    public ByteCounterInputStream(InputStream is) {
      super(is);
    }

    public int read() throws IOException {
      int d = in.read();
      if (d >= 0)
        cnt++;
      return d;
    }

    public int read(byte[] b) throws IOException {
      int d = in.read(b);
      if (d >= 0)
        cnt += d;
      return d;
    }

    public int read(byte[] b, int off, int len) throws IOException {
      int d = in.read(b, off, len);
      if (d >= 0)
        cnt += d;
      return d;
    }

    public long skip(long n) throws IOException {
      long d = in.skip(n);
      cnt += d;
      return d;
    }

    public void mark(int readlimit) {
      in.mark(readlimit);
      cntMark = cnt;
    }

    public void reset() throws IOException {
      in.reset();
      cnt = cntMark;
    }

    public long getLength() {
      return cnt;
    }
  }

  public ObjectInfo getObjectInfo(String obj) throws NoSuchObjectIdException, RemoteException {
    pep.checkAccess(pep.GET_OBJECT_INFO, ItqlHelper.validateUri(obj, "object"));

    ItqlHelper itql = ctx.getItqlHelper();
    try {
      AnswerSet ans =
          new AnswerSet(itql.doQuery(ItqlHelper.bindValues(ITQL_GET_OBJ_INFO, "subj", obj), null));
      ans.next();
      AnswerSet.QueryAnswerSet info = ans.getQueryResults();

      if (!info.next())
        throw new NoSuchObjectIdException(obj);
      info.beforeFirst();

      ObjectInfo res = new ObjectInfo();
      res.setUri(obj);
      parseObjectInfo(info, res);

      return res;
    } catch (AnswerException ae) {
      throw new RemoteException("Error querying RDF", ae);
    }
  }

  private void parseObjectInfo(AnswerSet.QueryAnswerSet info, ObjectInfo oi)
      throws AnswerException {
    int predCol = info.indexOf("p");
    int objCol  = info.indexOf("o");

    List reps = new ArrayList();

    while (info.next()) {
      String pred = info.getString(predCol);
      if (pred.equals(ItqlHelper.TOPAZ_URI + "hasRepresentation"))
        reps.add(info.getString(objCol));
      else if (pred.equals(ItqlHelper.DC_URI + "title"))
        oi.setTitle(info.getString("o"));
      else if (pred.equals(ItqlHelper.DC_URI + "description"))
        oi.setDescription(info.getString("o"));
      else if (pred.equals(ItqlHelper.TOPAZ_URI + "contextElement"))
        oi.setContextElement(info.getString("o"));
    }

    RepresentationInfo[] ri = new RepresentationInfo[reps.size()];
    for (int idx = 0; idx < reps.size(); idx++) {
      ri[idx] = new RepresentationInfo();
      ri[idx].setName((String) reps.get(idx));
      ri[idx].setSize(-1);

      String path = "/fedora/get/" + DoiUtil.uri2PID(oi.getUri()) + "/" + ri[idx].getName();
      ri[idx].setURL(fedoraServer.resolve(path).toString());
    }

    info.beforeFirst();
    while (info.next()) {
      String pred = info.getString(predCol);

      if (pred.endsWith("-objectSize")) {
        int idx = reps.indexOf(pred.substring(ItqlHelper.TOPAZ_URI.length(), pred.length() - 11));
        if (idx >= 0)
          ri[idx].setSize(Integer.parseInt(info.getString(objCol)));
      }

      if (pred.endsWith("-contentType")) {
        int idx = reps.indexOf(pred.substring(ItqlHelper.TOPAZ_URI.length(), pred.length() - 12));
        if (idx >= 0)
          ri[idx].setContentType(info.getString(objCol));
      }
    }

    oi.setRepresentations(ri);
  }
}
