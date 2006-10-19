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

import org.plos.service.BaseConfigurableService;
import org.topazproject.authentication.ProtectedService;
import org.topazproject.common.DuplicateIdException;
import org.topazproject.common.NoSuchIdException;
import org.topazproject.ws.article.Article;
import org.topazproject.ws.article.ArticleClientFactory;
import org.topazproject.ws.article.IngestException;

import javax.activation.DataHandler;
import javax.xml.rpc.ServiceException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.rmi.RemoteException;

/**
 * Wrapper around {@link org.topazproject.ws.article.Article} to reduce the confusion around
 * Article vs the service facet of it.
 * This provides a way to access the "Article" service.
 */
public class ArticleWebService extends BaseConfigurableService {
  private Article delegateService;

  public void init() throws IOException, URISyntaxException, ServiceException {
    final ProtectedService permissionProtectedService = getProtectedService();
    delegateService = ArticleClientFactory.create(permissionProtectedService);
  }

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
    ensureInitGetsCalledWithUsersSessionAttributes();
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
    ensureInitGetsCalledWithUsersSessionAttributes();
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
    ensureInitGetsCalledWithUsersSessionAttributes();
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
    ensureInitGetsCalledWithUsersSessionAttributes();
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
    ensureInitGetsCalledWithUsersSessionAttributes();
    delegateService.setState(doi, state);
  }

  /**
   * Get a list of all articles
   * @param startDate startDate
   * @param endDate endDate
   * @return list of article doi's
   * @throws java.rmi.RemoteException RemoteException
   */
  public String getArticles(final String startDate, final String endDate) throws RemoteException {
    ensureInitGetsCalledWithUsersSessionAttributes();
    return delegateService.getArticles(startDate, endDate, null, null, true);
  }
}
