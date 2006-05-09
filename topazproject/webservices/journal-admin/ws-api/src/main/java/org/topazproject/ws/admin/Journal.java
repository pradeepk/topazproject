
package org.topazproject.ws.admin;

import java.rmi.RemoteException;

/** 
 * Journal related administrative operations. This includes methods for managing journals, issues,
 * and articles.
 * 
 * @author Ronald Tschalär
 */
public interface Journal {
  /** 
   * Create a new journal. 
   * 
   * @param id  a short name for this journal; used for all subsequent lookups and manipulations
   * @throws DuplicateIdException if a journal with the given id already exists
   * @throws RemoteException if some other error occured
   */
  public void createJournal(String id) throws DuplicateIdException, RemoteException;

  /** 
   * Delete a journal. This will remove all associated issues too, but won't touch any articles.
   * 
   * @param id  the id of the journal to remote
   * @throws NoSuchIdException if the journal does not exist
   * @throws RemoteException if some other error occured
   */
  public void deleteJournal(String id) throws NoSuchIdException, RemoteException;

  /** 
   * Set the given journal's meta-data. The new data completely replaces the old.
   * 
   * @param id          the id of the journal to update
   * @param journalDef  an xml document containing the new meta-data for this journal; it must
   *                    follow the ??? DTD.
   * @throws NoSuchIdException if the journal does not exist
   * @throws RemoteException if some other error occured
   */
  public void setJournalInfo(String id, String journalDef)
      throws NoSuchIdException, RemoteException;

  /** 
   * Retrieve the given journal's current meta-data.
   * 
   * @param id  the id of the journal for which to get the meta-data
   * @return an xml document containing the current meta-data for this journal, or null if none has
   *         been set yet; it follows the ???  DTD.
   * @throws NoSuchIdException if the journal does not exist
   * @throws RemoteException if some other error occured
   */
  public String getJournalInfo(String id) throws NoSuchIdException, RemoteException;

  /** 
   * Get the list of existing journals. 
   * 
   * @return an array of id's; if no journals have been defined, an empty array is returned
   * @throws RemoteException 
   */
  public String[] listJournals() throws RemoteException;

  /** 
   * Create a new issue. 
   * 
   * @param journalId the id of the journal to which this issue belongs
   * @param issueNum  the issue number
   * @throws NoSuchIdException if the journal does not exist
   * @throws DuplicateIdException if an issue with the given number already exists
   * @throws RemoteException if some other error occured
   */
  public void createIssue(String journalId, String issueNum)
      throws DuplicateIdException, NoSuchIdException, RemoteException;

  /** 
   * Delete an issue. This will not remove any articles associated with this issue.
   * 
   * @param journalId the id of the journal to which this issue belongs
   * @param issueNum  the issue number of the issue to delete
   * @throws NoSuchIdException if the journal or issue does not exist
   * @throws RemoteException if some other error occured
   */
  public void deleteIssue(String journalId, String issueNum)
      throws NoSuchIdException, RemoteException;

  /** 
   * Set the given issue's meta-data. The new data completely replaces the old.
   * 
   * @param journalId the id of the journal to which the issue belongs
   * @param issueNum  the number of the issue to update
   * @param issueDef  an xml document containing the new meta-data for this issue; it must
   *                  follow the ??? DTD.
   * @throws NoSuchIdException if the journal or issue-number does not exist
   * @throws RemoteException if some other error occured
   */
  public void setIssueInfo(String journalId, String issueNum, String issueDef)
      throws NoSuchIdException, RemoteException;

  /** 
   * Retrieve the given issue's current meta-data.
   * 
   * @param journalId the id of the journal to which the issue belongs
   * @param issueNum  the number of the issue for which to get the meta-data
   * @return an xml document containing the current meta-data for this issue, or null if none has
   *         been set yet; it follows the ???  DTD.
   * @throws NoSuchIdException if the journal or issue-number does not exist
   * @throws RemoteException if some other error occured
   */
  public String getIssueInfo(String journalId, String issueNum)
      throws NoSuchIdException, RemoteException;

  /** 
   * Get the list of existing issues for the given journal. 
   * 
   * @param journalId the id of the journal for which to get the issue list
   * @return an array of issue numbers; if no issues have been defined for the journal, an empty
   *         array is returned
   * @throws NoSuchIdException if the journal does not exist
   * @throws RemoteException if some other error occured
   */
  public String[] listIssues(String journalId) throws NoSuchIdException, RemoteException;
}
