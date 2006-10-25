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
import org.topazproject.ws.article.NoSuchArticleIdException;
import org.topazproject.ws.article.NoSuchObjectIdException;
import org.topazproject.ws.article.ObjectInfo;
import org.topazproject.ws.article.RepresentationInfo;

import javax.activation.DataHandler;
import javax.xml.rpc.ServiceException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Wrapper around {@link org.topazproject.ws.article.Article} to reduce the confusion around
 * Article vs the service facet of it.
 * This provides a way to access the "Article" service.
 */
public class ArticleWebService extends BaseConfigurableService {
  private Article delegateService;
  private String smallImageRep;
  private String largeImageRep;
  private String mediumImageRep;

  public void init() throws IOException, URISyntaxException, ServiceException {
    final ProtectedService permissionProtectedService = getProtectedService();
    delegateService = ArticleClientFactory.create(permissionProtectedService);
  }

  /**
   * Ingest an article.
   * @param dataHandler dataHandler
   * @return the doi of the article ingested
   * @throws RemoteException RemoteException
   * @throws IngestException IngestException
   * @throws DuplicateIdException DuplicateIdException
   */
  public String ingest(final DataHandler dataHandler)
          throws RemoteException, IngestException, DuplicateIdException {
    ensureInitGetsCalledWithUsersSessionAttributes();
    return delegateService.ingest(dataHandler);
  }

  /**
   * Ingest an article.
   * @param articleUrl articleUrl
   * @return the doi of the article ingested
   * @throws RemoteException RemoteException
   * @throws IngestException IngestException
   * @throws DuplicateIdException DuplicateIdException
   */
  public String ingest(final URL articleUrl)
          throws RemoteException, IngestException, DuplicateIdException {
    return ingest(new DataHandler(articleUrl));
  }

  /**
   * Mark an article as superseded by another article
   * @param oldDoi oldDoi
   * @param newDoi newDoi
   * @throws RemoteException RemoteException
   * @throws NoSuchIdException NoSuchIdException
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
   * @throws RemoteException RemoteException
   * @throws NoSuchIdException NoSuchIdException
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
   * @throws RemoteException RemoteException
   * @throws NoSuchIdException NoSuchIdException
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
   * @throws RemoteException RemoteException
   * @throws NoSuchIdException NoSuchIdException
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

  /**
   * List the representations of a doi
   * @param doi doi
   * @return the representations of a doi
   * @throws org.topazproject.ws.article.NoSuchObjectIdException NoSuchObjectIdException
   * @throws java.rmi.RemoteException RemoteException
   */
  public RepresentationInfo[] listRepresentations(final String doi) throws RemoteException, NoSuchObjectIdException {
    ensureInitGetsCalledWithUsersSessionAttributes();
    return delegateService.listRepresentations(doi);
  }

  /**
   * Return the list of secondary objects
   * @param doi doi
   * @return the secondary objects of a doi
   * @throws java.rmi.RemoteException RemoteException
   * @throws org.topazproject.ws.article.NoSuchArticleIdException NoSuchArticleIdException
   */
  public SecondaryObject[] listSecondaryObjects(final String doi) throws RemoteException, NoSuchArticleIdException {
    ensureInitGetsCalledWithUsersSessionAttributes();
    return convert(delegateService.listSecondaryObjects(doi));
  }

  private SecondaryObject[] convert(final ObjectInfo[] objectInfos) {
    final Collection<SecondaryObject> convertedObjectInfos = new ArrayList<SecondaryObject>(objectInfos.length);
    for (final ObjectInfo objectInfo : objectInfos) {
      convertedObjectInfos.add(convert(objectInfo));
    }
    return convertedObjectInfos.toArray(new SecondaryObject[convertedObjectInfos.size()]);
  }

  private SecondaryObject convert(final ObjectInfo objectInfo) {
    return new SecondaryObject(objectInfo, smallImageRep, mediumImageRep, largeImageRep);
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
}
