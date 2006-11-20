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
   * @return the uri of the article ingested
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
   * @return the uri of the article ingested
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
   * @param oldUri oldUri
   * @param newUri newUri
   * @throws RemoteException RemoteException
   * @throws NoSuchIdException NoSuchIdException
   */
  public void markSuperseded(final String oldUri, final String newUri)
          throws RemoteException, NoSuchIdException {
    ensureInitGetsCalledWithUsersSessionAttributes();
    delegateService.markSuperseded(oldUri, newUri);
  }

  /**
   * Get the URL from which the objects contents can retrieved via GET.
   * @param obj uri
   * @param rep rep
   * @return the URL, or null if this object doesn't exist in the desired version
   * @throws RemoteException RemoteException
   * @throws NoSuchIdException NoSuchIdException
   */
  public String getObjectURL(final String obj, final String rep)
          throws RemoteException, NoSuchIdException {
    ensureInitGetsCalledWithUsersSessionAttributes();
    return delegateService.getObjectURL(obj, rep);
  }

  /**
   * Delete an article.
   * @param article uri
   * @throws RemoteException RemoteException
   * @throws NoSuchIdException NoSuchIdException
   */
  public void delete(final String article) throws RemoteException, NoSuchIdException {
    ensureInitGetsCalledWithUsersSessionAttributes();
    delegateService.delete(article);
  }

  /**
   * Change an articles state.
   * @param article uri
   * @param state state
   * @throws RemoteException RemoteException
   * @throws NoSuchIdException NoSuchIdException
   */
  public void setState(final String article, final int state)
          throws RemoteException, NoSuchIdException {
    ensureInitGetsCalledWithUsersSessionAttributes();
    delegateService.setState(article, state);
  }

  /**
   * Get a list of all articles
   * @param startDate startDate
   * @param endDate endDate
   * @return list of article uri's
   * @throws java.rmi.RemoteException RemoteException
   */
  public String getArticles(final String startDate, final String endDate) throws RemoteException {
    ensureInitGetsCalledWithUsersSessionAttributes();
    return delegateService.getArticles(startDate, endDate, null, null, null, true);
  }

  /*  * @param startDate  is the date to start searching from. If null, start from begining of time.
  *                   Can be iso8601 formatted or string representation of Date object.
  * @param endDate    is the date to search until. If null, search until present date
  * @param states     the list of article states to search for (all states if null or empty)
  * @param ascending  controls the sort order (by date). If used for RSS feeds, decending would
  *                   be appropriate. For archive display, ascending would be appropriate.
  * @return the xml for the specified feed
  * @throws RemoteException if there was a problem talking to the alerts service
  */
 public String getArticles(final String startDate, final String endDate, 
		             final int[] states, boolean ascending) throws RemoteException {
    ensureInitGetsCalledWithUsersSessionAttributes();
	return delegateService.getArticles(startDate, endDate, null, null, states, true);
	 
 }

  /**
   * Get the object-info of an object
   * @param obj uri
   * @return the object-info of the object
   * @throws org.topazproject.ws.article.NoSuchObjectIdException NoSuchObjectIdException
   * @throws java.rmi.RemoteException RemoteException
   */
  public ObjectInfo getObjectInfo(final String obj) throws RemoteException, NoSuchObjectIdException {
    ensureInitGetsCalledWithUsersSessionAttributes();
    return delegateService.getObjectInfo(obj);
  }

  /**
   * Return the list of secondary objects
   * @param article uri
   * @return the secondary objects of the article
   * @throws java.rmi.RemoteException RemoteException
   * @throws org.topazproject.ws.article.NoSuchArticleIdException NoSuchArticleIdException
   */
  public SecondaryObject[] listSecondaryObjects(final String article) throws RemoteException, NoSuchArticleIdException {
    ensureInitGetsCalledWithUsersSessionAttributes();
    return convert(delegateService.listSecondaryObjects(article));
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
