/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.ws.article;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.rmi.RemoteException;
import java.util.List;
import javax.activation.DataHandler;
import javax.xml.rpc.ServiceException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jrdf.graph.URIReference;
import org.topazproject.authentication.ProtectedService;
import org.topazproject.mulgara.itql.Answer;
import org.topazproject.mulgara.itql.AnswerException;
import org.topazproject.mulgara.itql.ItqlHelper;

import org.topazproject.fedora.client.APIMStubFactory;
import org.topazproject.fedora.client.Uploader;
import org.topazproject.fedora.client.FedoraAPIM;

/** 
 * The default implementation of the article manager.
 * 
 * @author Ronald Tschalär
 */
public class ArticleImpl implements Article {
  private static final Log    log   = LogFactory.getLog(ArticleImpl.class);
  private static final String MODEL = "<rmi://localhost/fedora#ri>";

  private final URI        fedoraServer;
  private final FedoraAPIM apim;
  private final Uploader   uploader;
  private final Ingester   ingester;
  private final ItqlHelper itql;
  private final ArticlePEP pep;

  /** 
   * Create a new article manager instance. 
   *
   * @param fedorSvc  the fedora management web-service
   * @param uploadSvc the fedora management web-service
   * @param mulgarSvc the mulgara web-service
   * @param hostname  the hostname under which this service is visible; this is used to generate
   *                  the proper URL for {@link #getObjectURL getObjectURL}.
   * @param pep       the policy-enforcer to use for access-control
   */
  public ArticleImpl(ProtectedService fedoraSvc, ProtectedService uploadSvc, 
                     ProtectedService mulgaraSvc, String hostname, ArticlePEP pep)
      throws URISyntaxException, IOException, ServiceException {
    URI fedoraURI = new URI(fedoraSvc.getServiceUri());
    fedoraServer = getRemoteFedoraURI(fedoraURI, hostname);

    uploader = new Uploader(uploadSvc);
    apim = APIMStubFactory.create(fedoraSvc);
    
    itql     = new ItqlHelper(mulgaraSvc);
    ingester = new Ingester(apim, uploader, itql, pep);

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


  public String ingest(DataHandler zip) throws DuplicateIdException, IngestException {
    return ingester.ingest(new Zip.DataSourceZip(zip.getDataSource()));
  }

  public void markSuperseded(String oldDoi, String newDoi)
      throws NoSuchIdException, RemoteException {
    checkAccess(pep.INGEST_ARTICLE, newDoi);

    String old_subj = "<" + pid2URI(doi2PID(oldDoi)) + ">";
    String new_subj = "<" + pid2URI(doi2PID(newDoi)) + ">";

    itql.doUpdate("insert " + old_subj + " <topaz:supersededBy> " + new_subj +
                            new_subj + " <topaz:supersedes> " + old_subj +
                            " into " + MODEL + ";");
  }

  public void setState(String doi, int state) throws NoSuchIdException, RemoteException {
    checkAccess(pep.SET_ARTICLE_STATE, doi);

    try {
      apim.modifyObject(doi2PID(doi), state2Str(state), null, "Changed state");
    } catch (RemoteException re) {
      FedoraUtil.detectNoSuchIdException(re, doi);
    }
  }

  public void delete(String doi, boolean purge) throws NoSuchIdException, RemoteException {
    checkAccess(pep.DELETE_ARTICLE, doi);

    try {
      String[] objList = findAllObjects(doi);
      if (log.isDebugEnabled())
        log.debug("deleting all objects for doi '" + doi + "'");

      for (int idx = 0; idx < objList.length; idx++) {
        if (log.isDebugEnabled())
          log.debug("deleting pid '" + objList[idx] + "'");

        if (purge) {
          apim.purgeObject(objList[idx], "Purged object", false);
        } else {
          apim.modifyObject(objList[idx], "D", null, "Deleted object");
        }
      }
    } catch (AnswerException ae) {
      throw new RemoteException("Error querying RDF", ae);
    }
  }

  public String getObjectURL(String doi, String rep) throws NoSuchIdException, RemoteException {
    checkAccess(pep.GET_OBJECT_URL, doi);

    String path = "/fedora/get/" + doi2PID(doi) + "/" + rep;
    return fedoraServer.resolve(path).toString();
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
      throws NoSuchIdException, RemoteException, AnswerException {
    String subj = "<" + pid2URI(doi2PID(doi)) + ">";
    Answer ans = new Answer(itql.doQuery("select $doi from " + MODEL + " where " +
                      // ensure it's an article
                      subj + " <fedora:fedora-system:def/model#contentModel> 'PlosArticle' and (" +
                      // find all related objects
                      subj + " <topaz:hasMember> $doi or " +
                      // find the article itself
                      "$doi <tucana:is> " + subj + ");"));

    List dois = ((Answer.QueryAnswer) ans.getAnswers().get(0)).getRows();
    if (dois.size() == 0)
      throw new NoSuchIdException(doi);

    String[] res = new String[dois.size()];
    for (int idx = 0; idx < res.length; idx++) {
      URIReference ref = (URIReference) ((Object[]) dois.get(idx))[0];
      res[idx] = uri2PID(ref.getURI().toString());
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
