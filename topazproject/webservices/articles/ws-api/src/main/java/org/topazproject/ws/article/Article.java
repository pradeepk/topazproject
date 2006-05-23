
package org.topazproject.ws.article;

import java.rmi.RemoteException;

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
   * @throws DuplicateIdException if the article already exists (as determined by its DOI)
   * @throws IngestException if there's a problem ingesting the archive
   * @throws RemoteException if some other error occured
   */
  public void ingestNew(byte[] zip) throws DuplicateIdException, IngestException, RemoteException;

  /** 
   * Update an article.
   * 
   * @param zip    a zip archive containing the pmc.xml and associated objects
   * @return the current (latest) version number of the article after the update
   * @throws NoSuchIdException if the article doesn't exist (as determined by its DOI)
   * @throws IngestException if there's a problem ingesting the archive
   * @throws RemoteException if some other error occured
   */
  public int ingestUpdate(byte[] zip) throws NoSuchIdException, IngestException, RemoteException;

  /** 
   * Change an articles state.
   * 
   * @param doi     the DOI of the article (e.g. "10.1371/journal.pbio.003811")
   * @param version the version of the article for which to change the state.
   * @param state   the new state
   * @throws NoSuchIdException if the article does not exist
   * @throws RemoteException if some other error occured
   */
  public void setState(String doi, int version, int state)
      throws NoSuchIdException, RemoteException;

  /** 
   * Delete an article.
   * 
   * @param doi     the DOI of the article (e.g. "10.1371/journal.pbio.003811")
   * @param version the version of the article to delete; or -1 for all versions
   * @param purge   if true, erase all traces; otherwise only the contents are deleted, leaving a
   *                "tombstone"
   * @throws NoSuchIdException if the article or version does not exist
   * @throws RemoteException if some other error occured
   */
  public void delete(String doi, int version, boolean purge)
      throws NoSuchIdException, RemoteException;

  /** 
   * Get the URL from which the objects contents can retrieved via GET.
   * 
   * @param doi     the DOI of the article (e.g. "10.1371/journal.pbio.003811")
   * @param version the version of the article to retrieve; or -1 for the latest version
   * @param rep     the desired representation of the article
   * @return the URL, or null if this object doesn't exist in the desired version
   * @throws NoSuchIdException if the article or version does not exist
   * @throws RemoteException if some other error occured
   */
  public String getObjectURL(String doi, int version, String rep)
      throws NoSuchIdException, RemoteException;
}
