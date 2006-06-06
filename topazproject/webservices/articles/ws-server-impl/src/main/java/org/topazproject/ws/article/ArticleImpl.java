
package org.topazproject.ws.article;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.rmi.RemoteException;

import javax.xml.rpc.ServiceException;

import org.apache.log4j.Logger;
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
  private static final Logger log = Logger.getLogger(ArticleImpl.class);

  private final URI        fedoraServer;
  private final FedoraAPIM apim;
  private final Uploader   uploader;
  private final Ingester   ingester;
  private final ItqlHelper itql;

  /** 
   * Create a new manager instance. 
   *
   * @param fedora   the uri of the fedora server
   * @param username the username to talk to fedora
   * @param password the password to talk to fedora
   * @param mulgara  the uri of the mulgara server
   * @param hostname the hostname under which this service is visible; this is used to generate the
   *                 proper URL for {@link #getObjectURL getObjectURL}.
   */
  public ArticleImpl(URI fedora, String username, String password, URI mulgara, String hostname)
      throws IOException, ServiceException {
    if (fedora.getHost().equals("localhost")) {
      try {
        this.fedoraServer = new URI(fedora.getScheme(), null, hostname, fedora.getPort(),
                                    fedora.getPath(), null, null);
      } catch (URISyntaxException use) {
        throw new Error(use);   // Can't happen
      }
    } else {
      this.fedoraServer = fedora;
    }

    apim = APIMStubFactory.getStub(fedora.getScheme(), fedora.getHost(), fedora.getPort(),
                                   username, password);
    uploader = new Uploader(fedora.getScheme(), fedora.getHost(), fedora.getPort(),
                            username, password);
    itql = new ItqlHelper(mulgara);

    ingester = new Ingester(apim, uploader, itql);
  }

  public void ingestNew(byte[] zip) throws DuplicateIdException, IngestException {
    ingester.ingestNew(new Zip.MemoryZip(zip));
  }

  public int ingestUpdate(byte[] zip) throws NoSuchIdException, IngestException {
    return ingester.ingestUpdate(new Zip.MemoryZip(zip));
  }

  public void setState(String doi, int version, int state)
      throws NoSuchIdException, RemoteException {
    try {
      apim.modifyObject(doi2PID(doi), state2Str(state), null, "Changed state");
    } catch (RemoteException re) {
      FedoraUtil.detectNoSuchIdException(re, doi);
    }
  }

  public void delete(String doi, int version, boolean purge)
      throws NoSuchIdException, RemoteException {
    // FIXME
    if (version != -1)
      throw new IllegalArgumentException("delete of individual versions not supported yet");

    String[] objList = findAllObjects(doi);
    for (int idx = 0; idx < objList.length; idx++) {
      if (purge) {
        apim.purgeObject(objList[idx], "Purged object", false);
      } else {
        apim.modifyObject(objList[idx], "D", null, "Deleted object");
      }
    }
  }

  public String getObjectURL(String doi, int version, String rep)
      throws NoSuchIdException, RemoteException {
    String date = findDateForVersion(doi, version);
    String path = "/fedora/get/" + doi2PID(doi) + "/" + rep;
    if (date != null)
      path += "/" + date;

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

  protected String[] findAllObjects(String doi) throws NoSuchIdException {
    // XXX
    return new String[] { doi };
  }

  protected String findDateForVersion(String doi, int version) throws NoSuchIdException {
    // XXX  format: YYYY-MM-DDTHH:MM:SS.SSSZ
    return null;
  }

  protected String doi2PID(String doi) {
    try {
      return URLEncoder.encode("doi:" + doi, "UTF-8");
    } catch (UnsupportedEncodingException uee) {
      throw new RuntimeException(uee);          // shouldn't happen
    }
  }
}
