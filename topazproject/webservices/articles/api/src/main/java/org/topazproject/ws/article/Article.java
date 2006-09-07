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

import java.util.Date;
import java.rmi.RemoteException;
import javax.activation.DataHandler;

/** 
 * Article storage and retrieval.
 * 
 * @author Ronald Tschalär
 */
public interface Article {
  /** Article state of "Active" */
  public static final int ST_ACTIVE   = 0;
  /** Article state of "Disabled" */
  public static final int ST_DISABLED = 1;

  /** 
   * Add a new article.
   * 
   * @param zip    a zip archive containing the pmc.xml and associated objects
   * @return the DOI of the new article
   * @throws DuplicateIdException if the article already exists (as determined by its DOI)
   * @throws IngestException if there's a problem ingesting the archive
   * @throws RemoteException if some other error occured
   */
  public String ingest(DataHandler zip)
      throws DuplicateIdException, IngestException, RemoteException;

  /** 
   * Marks an article as superseded by another article.
   * 
   * @param oldDoi the doi of the article that has been superseded by <var>newDoi</var>
   * @param newDoi the doi of the article that supersedes <var>oldDoi</var>
   * @throws NoSuchIdException if the article does not exist
   * @throws RemoteException if some other error occured
   */
  public void markSuperseded(String oldDoi, String newDoi)
      throws NoSuchIdException, RemoteException;

  /** 
   * Change an articles state.
   * 
   * @param doi     the DOI of the article (e.g. "10.1371/journal.pbio.003811")
   * @param state   the new state
   * @throws NoSuchIdException if the article does not exist
   * @throws RemoteException if some other error occured
   */
  public void setState(String doi, int state) throws NoSuchIdException, RemoteException;

  /** 
   * Delete an article.
   * 
   * @param doi     the DOI of the article (e.g. "10.1371/journal.pbio.003811")
   * @param purge   if true, erase all traces; otherwise only the contents are deleted, leaving a
   *                "tombstone". Note that it may not be possible to find and therefore erase
   *                all traces from the ingest.
   * @throws NoSuchIdException if the article or version does not exist
   * @throws RemoteException if some other error occured
   */
  public void delete(String doi, boolean purge) throws NoSuchIdException, RemoteException;

  /** 
   * Get the URL from which the objects contents can retrieved via GET.
   * 
   * @param doi     the DOI of the article (e.g. "10.1371/journal.pbio.003811")
   * @param rep     the desired representation of the article
   * @return the URL, or null if this object doesn't exist in the desired version
   * @throws NoSuchIdException if the article or version does not exist
   * @throws RemoteException if some other error occured
   */
  public String getObjectURL(String doi, String rep) throws NoSuchIdException, RemoteException;

  /**
   * Get list of articles for a given set of categories or authors bracked by specified
   * times. List is returned as an XML string of the following format:
   * <pre>
   *   <articles>
   *     <article>
   *       <doi>...</doi>
   *       <title>...</title>
   *       <description>...</description>
   *       <date>YYY-MM-DD</date>
   *       <authors>
   *         <author>...</author>
   *         ...
   *       </authors>
   *       <categories>
   *         <category>...</category>
   *         ...
   * </pre>
   *
   * @param startDate is the date to start searching from. If empty, start from begining of time.
   *        Can be iso8601 formatted or string representation of Date object.
   * @param endDate is the date to search until. If empty, search until prsent date
   * @param categories is list of categories to search for articles within (all categories if empty)
   * @param authors is list of authors to search for articles within (all authors if empty)
   * @param ascending controls the sort order (by date). If used for RSS feeds, decending would
   *        be appropriate. For archive display, ascending would be appropriate.
   * @return the xml for the specified feed
   * @throws RemoteException if there was a problem talking to the alerts service
   */
  public String getArticles(String startDate, String endDate,
                            String[] categories, String[] authors,
                            boolean ascending) throws RemoteException;
}
