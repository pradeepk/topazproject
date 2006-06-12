
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

import org.apache.log4j.Logger;
import org.jrdf.graph.URIReference;
import org.topazproject.authentication.ProtectedService;
import org.topazproject.mulgara.itql.Answer;
import org.topazproject.mulgara.itql.AnswerException;
import org.topazproject.mulgara.itql.ItqlHelper;

import fedora.client.APIMStubFactory;
import fedora.client.Uploader;
import fedora.server.management.FedoraAPIM;

/** 
 * The default implementation of the article manager.
 * 
 * @author Ronald Tschalär
 */
public class ArticleImpl implements Article {
  private static final Logger log   = Logger.getLogger(ArticleImpl.class);
  private static final String MODEL = "<rmi://localhost/fedora#ri>";

  private final URI        fedoraServer;
  private final FedoraAPIM apim;
  private final Uploader   uploader;
  private final Ingester   ingester;
  private final ItqlHelper itql;

  /** 
   * Create a new article manager instance. 
   *
   * @param fedoraUri   the uri of the fedora server
   * @param username    the username to talk to fedora
   * @param password    the password to talk to fedora
   * @param mulgaraUri  the uri of the mulgara server
   * @param hostname    the hostname under which this service is visible; this is used to generate
   *                    the proper URL for {@link #getObjectURL getObjectURL}.
   */
  public ArticleImpl(URI fedoraUri, String username, String password, URI mulgaraUri, String hostname)
      throws IOException, ServiceException {
    fedoraServer = getRemoteFedoraURI(fedoraUri, hostname);

    apim     = APIMStubFactory.getStub(fedoraUri.getScheme(), fedoraUri.getHost(),
                                       fedoraUri.getPort(), username, password);
    uploader = new Uploader(fedoraUri.getScheme(), fedoraUri.getHost(), fedoraUri.getPort(),
                            username, password);
    itql     = new ItqlHelper(mulgaraUri);
    ingester = new Ingester(apim, uploader, itql);
  }

  /** 
   * Create a new article manager instance. 
   *
   * @param fedorSvc  the fedora management web-service
   * @param mulgarSvc the mulgara web-service
   * @param hostname  the hostname under which this service is visible; this is used to generate
   *                  the proper URL for {@link #getObjectURL getObjectURL}.
   */
  public ArticleImpl(ProtectedService fedoraSvc, ProtectedService mulgaraSvc, String hostname)
      throws URISyntaxException, IOException, ServiceException {
    URI fedoraURI = new URI(fedoraSvc.getServiceUri());
    fedoraServer = getRemoteFedoraURI(fedoraURI, hostname);

    if (fedoraSvc.requiresUserNamePassword()) {
      apim = APIMStubFactory.getStub(fedoraURI.getScheme(), fedoraURI.getHost(),
                                     fedoraURI.getPort(), fedoraSvc.getUserName(),
                                     fedoraSvc.getPassword());
      uploader = new Uploader(fedoraURI.getScheme(), fedoraURI.getHost(), fedoraURI.getPort(),
                              fedoraSvc.getUserName(), fedoraSvc.getPassword());
    } else {
      String pqf = fedoraURI.getPath();
      if (fedoraURI.getQuery() != null)
        pqf += "?" + fedoraURI.getQuery();
      if (fedoraURI.getFragment() != null)
        pqf += "#" + fedoraURI.getFragment();

      apim = APIMStubFactory.getStubAltPath(fedoraURI.getScheme(), fedoraURI.getHost(),
                                            fedoraURI.getPort(), pqf, null, null);
      // XXX: short of wholesale copying and modifying of Uploader, I see no way to get the
      // path in there.
      uploader = new Uploader(fedoraURI.getScheme(), fedoraURI.getHost(), fedoraURI.getPort(),
                              fedoraSvc.getUserName(), fedoraSvc.getPassword());
    }

    itql     = new ItqlHelper(mulgaraSvc);
    ingester = new Ingester(apim, uploader, itql);
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
    String old_subj = "<" + pid2URI(doi2PID(oldDoi)) + ">";
    String new_subj = "<" + pid2URI(doi2PID(newDoi)) + ">";

    itql.doUpdate("insert " + old_subj + " <topaz:supersededBy> " + new_subj +
                            new_subj + " <topaz:supersedes> " + old_subj +
                            " into " + MODEL + ";");
  }

  public void setState(String doi, int state) throws NoSuchIdException, RemoteException {
    try {
      apim.modifyObject(doi2PID(doi), state2Str(state), null, "Changed state");
    } catch (RemoteException re) {
      FedoraUtil.detectNoSuchIdException(re, doi);
    }
  }

  public void delete(String doi, boolean purge) throws NoSuchIdException, RemoteException {
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
    String path = "/fedora/get/" + doi2PID(doi) + "/" + rep;
    return fedoraServer.resolve(path).toString();
  }

  protected String state2Str(int state) {
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

  protected String doi2PID(String doi) {
    try {
      return "doi:" + URLEncoder.encode(doi, "UTF-8");
    } catch (UnsupportedEncodingException uee) {
      throw new RuntimeException(uee);          // shouldn't happen
    }
  }

  protected String pid2DOI(String pid) {
    try {
      return URLDecoder.decode(pid.substring(4), "UTF-8");
    } catch (UnsupportedEncodingException uee) {
      throw new RuntimeException(uee);          // shouldn't happen
    }
  }

  protected String pid2URI(String pid) {
    return "info:fedora/" + pid;
  }

  protected String uri2PID(String uri) {
    return uri.substring(12);
  }
}
