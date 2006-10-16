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
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
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

import org.topazproject.feed.ArticleFeed;

/** 
 * The default implementation of the article manager.
 * 
 * @author Ronald Tschalär
 */
public class ArticleImpl implements Article {
  private static final Log           log   = LogFactory.getLog(ArticleImpl.class);
  private static final Configuration CONF  = ConfigurationStore.getInstance().getConfiguration();
  private static final String        MODEL = "<" + CONF.getString("topaz.models.articles") + ">";

  private static final String ITQL_DELETE_DOI =
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
      ("select $doi from ${MODEL} where " +
          // ensure it's an article
       "  <${subj}> <rdf:type> <topaz:Article> and (" +
          // find all related objects
       "  <${subj}> <dc_terms:hasPart> $doi or <${subj}> <topaz:hasCategory> $doi " +
          // find the article itself
       "  or $doi <tucana:is> <${subj}> );").
      replaceAll("\\Q${MODEL}", MODEL);

  private static final String ITQL_GET_OBJ_INFO =
      ("select $p $o from ${MODEL} where <${subj}> $p $o;").
      replaceAll("\\Q${MODEL}", MODEL);

  private static final String ITQL_LIST_SEC_OBJS =
      ("select $prev $cur subquery(select $p $o from ${MODEL} where $cur $p $o) " +
       "  from ${MODEL} where " +
       "  <${art}> <rdf:type> <topaz:Article> and " +
       "  walk(<${art}> <topaz:nextGraphic> $cur and $prev <topaz:nextGraphic> $cur);").
      replaceAll("\\Q${MODEL}", MODEL);

  private static final String ITQL_TEST_ARTICLE =
      ("select $id from ${MODEL} where " +
       "  $id <rdf:type> <topaz:Article> and $id <tucana:is> <${art}>;").
      replaceAll("\\Q${MODEL}", MODEL);

  private final URI        fedoraServer;
  private final Ingester   ingester;
  private final ArticlePEP pep;
  private final TopazContext ctx;

  /**
   * Creates a new ArticleImpl object.
   *
   * @param pep The xacml pep
   * @param ctx The topaz api context
   */
  public ArticleImpl(ArticlePEP pep, TopazContext ctx) {
    this.pep      = pep;
    this.ctx      = ctx;
    this.ingester   = new Ingester(pep, ctx);
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

    Uploader uploader = new Uploader(uploadSvc);
    FedoraAPIM apim = APIMStubFactory.create(fedoraSvc);

    ItqlHelper itql     = new ItqlHelper(mulgaraSvc);

    ctx = new SimpleTopazContext(itql, apim, uploader);
    ingester = new Ingester(pep, ctx);

    this.pep = pep;
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

  public void markSuperseded(String oldDoi, String newDoi)
      throws NoSuchArticleIdException, RemoteException {
    checkAccess(pep.INGEST_ARTICLE, newDoi);    // FIXME: should pass both doi's

    String old_subj = "<" + pid2URI(doi2PID(oldDoi)) + ">";
    String new_subj = "<" + pid2URI(doi2PID(newDoi)) + ">";

    ctx.getItqlHelper().doUpdate("insert " + old_subj + " <dc_terms:isReplacedBy> " + new_subj +
                            new_subj + " <dc_terms:replaces> " + old_subj +
                            " into " + MODEL + ";");
  }

  public void setState(String doi, int state) throws NoSuchArticleIdException, RemoteException {
    checkAccess(pep.SET_ARTICLE_STATE, doi);

    try {
      ctx.getFedoraAPIM().modifyObject(doi2PID(doi), state2Str(state), null, "Changed state");
    } catch (RemoteException re) {
      FedoraUtil.detectNoSuchArticleIdException(re, doi);
    }
  }

  public void delete(String doi, boolean purge) throws NoSuchArticleIdException, RemoteException {
    checkAccess(pep.DELETE_ARTICLE, doi);

    ItqlHelper itql = ctx.getItqlHelper();
    FedoraAPIM apim = ctx.getFedoraAPIM();
    String txn = purge ? "delete " + doi : null;
    try {
      if (txn != null)
        itql.beginTxn(txn);

      String[] objList = findAllObjects(doi);
      if (log.isDebugEnabled())
        log.debug("deleting all objects for doi '" + doi + "'");

      for (int idx = 0; idx < objList.length; idx++) {
        if (log.isDebugEnabled())
          log.debug("deleting pid '" + objList[idx] + "'");

        if (purge) {
          apim.purgeObject(objList[idx], "Purged object", false);
          itql.doUpdate(ItqlHelper.bindValues(ITQL_DELETE_DOI, "subj", pid2URI(objList[idx])));
        } else {
          apim.modifyObject(objList[idx], "D", null, "Deleted object");
        }
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

  public String getObjectURL(String doi, String rep)
      throws NoSuchObjectIdException, RemoteException {
    checkAccess(pep.GET_OBJECT_URL, doi);

    String path = "/fedora/get/" + doi2PID(doi) + "/" + rep;
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
      StringAnswer articlesAnswer = new StringAnswer(itql.doQuery(articlesQuery));
      Map articles = ArticleFeed.getArticlesSummary(articlesAnswer);

      for (Iterator it = articles.keySet().iterator(); it.hasNext(); ) {
        String doi = (String)it.next();
        try {
          checkAccess(pep.READ_META_DATA, doi);
        } catch (SecurityException se) {
          articles.remove(doi);
          if (log.isDebugEnabled())
            log.debug(doi, se);
        }
      }

      String detailsQuery = ArticleFeed.getDetailsQuery(articles.values());
      StringAnswer detailsAnswer = new StringAnswer(itql.doQuery(detailsQuery));
      ArticleFeed.addArticlesDetails(articles, detailsAnswer);

      return ArticleFeed.buildXml(articles.values());
    } catch (AnswerException ae) {
      throw new RemoteException("Error querying RDF", ae);
    }
  }

  public ObjectInfo[] listSecondaryObjects(String doi)
      throws NoSuchArticleIdException, RemoteException {
    if (doi == null)
      throw new NullPointerException("doi may not be null");

    checkAccess(pep.LIST_SEC_OBJECTS, doi);

    String art = pid2URI(doi2PID(doi));
    ItqlHelper.validateUri(art, "doi");

    ItqlHelper itql = ctx.getItqlHelper();
    try {
      AnswerSet ans =
          new AnswerSet(itql.doQuery(ItqlHelper.bindValues(ITQL_LIST_SEC_OBJS, "art", art)));
      ans.next();
      AnswerSet.QueryAnswerSet rows = ans.getQueryResults();

      if (!rows.next() && !articleExists(itql, art))
        throw new NoSuchArticleIdException(doi);
      rows.beforeFirst();

      Map loc = new HashMap();
      while (rows.next()) {
        ObjectInfo info = new ObjectInfo();
        info.setDoi(pid2DOI(uri2PID(rows.getString("cur"))));

        loc.put(pid2DOI(uri2PID(rows.getString("prev"))), info);

        AnswerSet.QueryAnswerSet sqa = rows.getSubQueryResults(2);
        info.setRepresentations(parseRepresenations(sqa));

        sqa.beforeFirst();
        while (sqa.next()) {
          String pred = sqa.getString("p");
          if (pred.equals(ItqlHelper.DC_URI + "title"))
            info.setTitle(sqa.getString("o"));
          if (pred.equals(ItqlHelper.DC_URI + "description"))
            info.setDescription(sqa.getString("o"));
        }
      }

      List infos = new ArrayList();
      for (ObjectInfo info = (ObjectInfo) loc.get(doi); info != null;
           info = (ObjectInfo) loc.get(info.getDoi()))
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

  protected String[] findAllObjects(String doi)
      throws NoSuchArticleIdException, RemoteException, AnswerException {
    String subj = pid2URI(doi2PID(doi));
    ItqlHelper.validateUri(subj, "doi");

    ItqlHelper itql = ctx.getItqlHelper();
    StringAnswer ans =
        new StringAnswer(itql.doQuery(ItqlHelper.bindValues(ITQL_FIND_OBJS, "subj", subj)));
    List dois = ((StringAnswer.StringQueryAnswer) ans.getAnswers().get(0)).getRows();
    if (dois.size() == 0)
      throw new NoSuchArticleIdException(doi);

    String[] res = new String[dois.size()];
    for (int idx = 0; idx < res.length; idx++)
      res[idx] = uri2PID(((String[]) dois.get(idx))[0]);

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
          new StringAnswer(itql.doQuery(ITQL_TEST_ARTICLE.replaceAll("\\Q${art}", art)));
      List rows = ((StringAnswer.StringQueryAnswer) ans.getAnswers().get(0)).getRows();
      return rows.size() > 0;
    } catch (AnswerException ae) {
      throw new RemoteException("Error testing if article '" + art + "' exists", ae);
    }
  }

  public void setRepresentation(String doi, String rep, DataHandler content)
      throws NoSuchObjectIdException, RemoteException {
    if (doi == null)
      throw new NullPointerException("doi may not be null");
    if (rep == null)
      throw new NullPointerException("representation may not be null");

    checkAccess(pep.SET_REPRESENTATION, doi);   // FIXME: should pass 'rep' too

    String subj = pid2URI(doi2PID(doi));
    ItqlHelper.validateUri(subj, "doi");

    ItqlHelper itql = ctx.getItqlHelper();
    FedoraAPIM apim = ctx.getFedoraAPIM();
    String txn = "set-rep " + doi + "|" + rep;
    try {
      itql.beginTxn(txn);

      if (log.isDebugEnabled())
        log.debug("setting representation '" + rep + "' for '" + doi + "'");

      itql.doUpdate(ItqlHelper.bindValues(ITQL_DELETE_REP, "subj", subj, "rep", rep));

      if (content != null) {
        String ct = content.getContentType();
        if (ct == null)
          ct = "application/octet-stream";

        ByteCounterInputStream bcis = new ByteCounterInputStream(content.getInputStream());
        String reLoc = ctx.getFedoraUploader().upload(bcis);
        try {
          apim.modifyDatastreamByReference(doi2PID(doi), rep, null, null, false, ct, null, reLoc,
                                           "A", "Updated datastream", false);
        } catch (RemoteException re) {
          if (!isNoSuchDatastream(re))
            throw re;

          if (log.isDebugEnabled())
            log.debug("representation '" + rep + "' for '" + doi + "' doesn't exist yet - " +
                      "creating it", re);
          apim.addDatastream(doi2PID(doi), rep, new String[0], "Represention", false, ct, null,
                             reLoc, "M", "A", "New representation");
        }

        Map map = new HashMap();
        map.put("subj", subj);
        map.put("rep", rep);
        map.put("size", Long.toString(bcis.getLength()));
        map.put("type", ct);
        itql.doUpdate(ItqlHelper.bindValues(ITQL_CREATE_REP, map));
      } else {
        try {
          apim.purgeDatastream(doi2PID(doi), rep, null, "Purged datastream", false);
        } catch (RemoteException re) {
          if (!isNoSuchDatastream(re))
            throw re;

          if (log.isDebugEnabled())
            log.debug("representation '" + rep + "' for '" + doi + "' doesn't exist", re);
        }
      }

      itql.commitTxn(txn);
      txn = null;
    } catch (RemoteException re) {
      if (isNoSuchObject(re))
        throw (NoSuchObjectIdException) new NoSuchObjectIdException(doi).initCause(re);
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

  public RepresentationInfo[] listRepresentations(String doi)
      throws NoSuchObjectIdException, RemoteException {
    if (doi == null)
      throw new NullPointerException("doi may not be null");

    checkAccess(pep.LIST_REPRESENTATIONS, doi);

    String subj = pid2URI(doi2PID(doi));
    ItqlHelper.validateUri(subj, "doi");

    ItqlHelper itql = ctx.getItqlHelper();
    try {
      AnswerSet ans =
          new AnswerSet(itql.doQuery(ItqlHelper.bindValues(ITQL_GET_OBJ_INFO, "subj", subj)));
      ans.next();
      AnswerSet.QueryAnswerSet info = ans.getQueryResults();

      if (!info.next())
        throw new NoSuchObjectIdException(doi);
      info.beforeFirst();

      return parseRepresenations(info);
    } catch (AnswerException ae) {
      throw new RemoteException("Error querying RDF", ae);
    }
  }

  private RepresentationInfo[] parseRepresenations(AnswerSet.QueryAnswerSet info)
      throws AnswerException {
    int predCol = info.indexOf("p");
    int objCol  = info.indexOf("o");

    List reps = new ArrayList();
    while (info.next()) {
      String pred = info.getString(predCol);
      if (pred.equals(ItqlHelper.TOPAZ_URI + "hasRepresentation"))
        reps.add(info.getString(objCol));
    }

    RepresentationInfo[] res = new RepresentationInfo[reps.size()];
    for (int idx = 0; idx < reps.size(); idx++) {
      res[idx] = new RepresentationInfo();
      res[idx].setName((String) reps.get(idx));
      res[idx].setSize(-1);
    }

    info.beforeFirst();
    while (info.next()) {
      String pred = info.getString(predCol);

      if (pred.endsWith("-objectSize")) {
        int idx = reps.indexOf(pred.substring(ItqlHelper.TOPAZ_URI.length(), pred.length() - 11));
        if (idx >= 0)
          res[idx].setSize(Integer.parseInt(info.getString(objCol)));
      }

      if (pred.endsWith("-contentType")) {
        int idx = reps.indexOf(pred.substring(ItqlHelper.TOPAZ_URI.length(), pred.length() - 12));
        if (idx >= 0)
          res[idx].setContentType(info.getString(objCol));
      }
    }

    return res;
  }

  protected void checkAccess(String action, String doi) {
    pep.checkAccess(action, URI.create(pid2URI(doi2PID(doi))));
  }

  protected static String doi2PID(String doi) {
    try {
      return "doi:" + URLEncoder.encode(doi, "UTF-8");
    } catch (UnsupportedEncodingException uee) {
      throw new RuntimeException(uee);          // shouldn't happen
    }
  }

  protected static String pid2DOI(String pid) {
    try {
      return URLDecoder.decode(pid.substring(4), "UTF-8");
    } catch (UnsupportedEncodingException uee) {
      throw new RuntimeException(uee);          // shouldn't happen
    }
  }

  protected static String pid2URI(String pid) {
    return "info:fedora/" + pid;
  }

  protected static String uri2PID(String uri) {
    return uri.substring(12);
  }
}
