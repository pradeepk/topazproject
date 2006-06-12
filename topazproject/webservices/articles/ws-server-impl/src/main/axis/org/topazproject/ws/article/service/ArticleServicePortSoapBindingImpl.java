
package org.topazproject.ws.article.service;

import java.io.IOException;
import java.net.URI;
import java.rmi.RemoteException;
import javax.activation.DataHandler;
import javax.xml.rpc.ServiceException;

import org.topazproject.ws.article.ArticleImpl;
import org.apache.log4j.Logger;

public class ArticleServicePortSoapBindingImpl implements Article {
  private static final Logger log = Logger.getLogger(ArticleServicePortSoapBindingImpl.class);

  private final ArticleImpl impl;

  /** 
   * Create a new binding. 
   */
  public ArticleServicePortSoapBindingImpl() throws IOException, ServiceException {
    // FIXME: get from config
    URI fedora = URI.create("http://localhost:9090/fedora/");
    String username = "fedoraAdmin";
    String password = "fedoraAdmin";
    String hostname = "localhost";
    URI mulgara = URI.create("http://localhost:9090/fedora/services/ItqlBeanService");

    impl = new ArticleImpl(fedora, username, password, mulgara, hostname);
  }

  /**
   * @see org.topazproject.ws.article.Article#ingest
   */
  public String ingest(DataHandler zip)
      throws RemoteException, DuplicateIdException, IngestException {
    try {
      return impl.ingest(zip);
    } catch (org.topazproject.ws.article.DuplicateIdException die) {
      log.info(die);
      throw new DuplicateIdException(die.getId());
    } catch (org.topazproject.ws.article.IngestException ie) {
      log.info(ie);
      throw new IngestException(ie.getMessage());
    }
  }

  /**
   * @see org.topazproject.ws.article.Article#markSuperseded
   */
  public void markSuperseded(String oldDoi, String newDoi)
      throws RemoteException, NoSuchIdException {
    try {
      impl.markSuperseded(oldDoi, newDoi);
    } catch (org.topazproject.ws.article.NoSuchIdException nsie) {
      log.info(nsie);
      throw new NoSuchIdException(nsie.getId());
    }
  }

  /**
   * @see org.topazproject.ws.article.Article#delete
   */
  public void delete(String doi, boolean purge) throws RemoteException, NoSuchIdException {
    try {
      impl.delete(doi, purge);
    } catch (org.topazproject.ws.article.NoSuchIdException nsie) {
      log.info(nsie);
      throw new NoSuchIdException(nsie.getId());
    }
  }

  /**
   * @see org.topazproject.ws.article.Article#setState
   */
  public void setState(String doi, int state) throws RemoteException, NoSuchIdException {
    try {
      impl.setState(doi, state);
    } catch (org.topazproject.ws.article.NoSuchIdException nsie) {
      log.info(nsie);
      throw new NoSuchIdException(nsie.getId());
    }
  }

  /**
   * @see org.topazproject.ws.article.Article#getObjectURL
   */
  public String getObjectURL(String doi, String rep) throws RemoteException, NoSuchIdException {
    try {
      return impl.getObjectURL(doi, rep);
    } catch (org.topazproject.ws.article.NoSuchIdException nsie) {
      log.info(nsie);
      throw new NoSuchIdException(nsie.getId());
    }
  }
}
