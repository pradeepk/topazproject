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

import org.topazproject.ws.article.Article;
import org.topazproject.ws.article.ArticleServiceLocator;
import org.topazproject.ws.article.DuplicateIdException;
import org.topazproject.ws.article.IngestException;
import org.topazproject.ws.article.NoSuchIdException;

import javax.activation.DataHandler;
import javax.xml.rpc.ServiceException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

/**
 * Wrapper around {@link org.topazproject.ws.article.Article} to reduce the confusion around
 * Article vs the service facet of it.
 * This provides a way to access the "Article" service.
 */
public class ArticleWebService {
  private Article delegateService;

  /**
   * Ingest an an article.
   * @param dataHandler dataHandler
   * @return the doi of the article ingested
   * @throws RemoteException
   * @throws IngestException
   * @throws DuplicateIdException
   */
  public String ingest(final DataHandler dataHandler)
          throws RemoteException, IngestException, DuplicateIdException {
    return delegateService.ingest(dataHandler);
  }

  /**
   * Mark an article as superseded by another article
   * @param oldDoi oldDoi
   * @param newDoi newDoi
   * @throws RemoteException
   * @throws NoSuchIdException
   */
  public void markSuperseded(final String oldDoi, final String newDoi)
          throws RemoteException, NoSuchIdException {
    delegateService.markSuperseded(oldDoi, newDoi);
  }

  /**
   * Get the URL from which the objects contents can retrieved via GET.
   * @param doi doi
   * @param rep rep
   * @return the URL, or null if this object doesn't exist in the desired version
   * @throws RemoteException
   * @throws NoSuchIdException
   */
  public String getObjectURL(final String doi, final String rep)
          throws RemoteException, NoSuchIdException {
    return delegateService.getObjectURL(doi, rep);
  }

  /**
   * Delete an article.
   * @param doi doi
   * @param purge purge
   * @throws RemoteException
   * @throws NoSuchIdException
   */
  public void delete(final String doi, final boolean purge)
          throws RemoteException, NoSuchIdException {
    delegateService.delete(doi, purge);
  }

  /**
   * Change an articles state.
   * @param doi doi
   * @param state state
   * @throws RemoteException
   * @throws NoSuchIdException
   */
  public void setState(final String doi, final int state)
          throws RemoteException, NoSuchIdException {
    delegateService.setState(doi, state);
  }

  /**
   * Set the article service to delegate to.
   * @param serviceUrl serviceUrl
   * @throws ServiceException
   * @throws MalformedURLException
   */
  public void setServicePort(final String serviceUrl)
          throws ServiceException, MalformedURLException {
    delegateService = new ArticleServiceLocator()
            .getArticleServicePort(new URL(serviceUrl));
  }
}
