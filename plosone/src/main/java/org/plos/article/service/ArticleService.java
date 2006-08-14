/* $HeadURL::                                                                            $
 * $Id$
 */
package org.plos.article.service;

import org.topazproject.ws.article.Article;
import org.topazproject.ws.article.ArticleServiceLocator;
import org.topazproject.ws.article.IngestException;
import org.topazproject.ws.article.NoSuchIdException;
import org.topazproject.ws.article.DuplicateIdException;

import javax.activation.DataHandler;
import javax.xml.rpc.ServiceException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

/**
 * Wrapper around {@link org.topazproject.ws.article.Article} to reduce the confusion around Article vs the service facet of it.
 * This provides a way to access the "Article" service.
 */
public class ArticleService implements Article {
  private Article delegateService;

  public ArticleService() {
  }

  public String ingest(final DataHandler dataHandler) throws RemoteException, IngestException, DuplicateIdException {
    return delegateService.ingest(dataHandler);
  }

  public void markSuperseded(final String oldDoi, final String newDoi) throws RemoteException, NoSuchIdException {
    delegateService.markSuperseded(oldDoi, newDoi);
  }

  public String getObjectURL(final String doi, final String rep) throws RemoteException, NoSuchIdException {
    return delegateService.getObjectURL(doi, rep);
  }

  public void delete(final String doi, final boolean purge) throws RemoteException, NoSuchIdException {
    delegateService.delete(doi, purge);
  }

  public void setState(final String doi, final int state) throws RemoteException, NoSuchIdException {
    delegateService.setState(doi, state);
  }

  /**
   * Set the article service to delegate to.
   * @param serviceUrl serviceUrl
   * @throws ServiceException
   * @throws MalformedURLException
   */
  public void setServicePort(final String serviceUrl) throws ServiceException, MalformedURLException {
    delegateService = new ArticleServiceLocator()
            .getArticleServicePort(new URL(serviceUrl));
  }
}
