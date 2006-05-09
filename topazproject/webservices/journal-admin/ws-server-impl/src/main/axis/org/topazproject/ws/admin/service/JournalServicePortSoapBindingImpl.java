
package org.topazproject.ws.admin.service;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

/** 
 * The implementation of the journal administration. Just a dummy for now.
 * 
 * @author Ronald Tschalär
 */
public class JournalServicePortSoapBindingImpl implements Journal {
  private static final Map journals = new HashMap();

  /**
   * @see org.topazproject.ws.admin.Journal#createJournal
   */
  public void createJournal(String id) throws DuplicateIdException, RemoteException {
    synchronized (journals) {
      if (journals.containsKey(id))
        throw new DuplicateIdException(id);

      journals.put(id, new JournalInfo());
    }
  }

  /** 
   * @see org.topazproject.ws.admin.Journal#deleteJournal
   */
  public void deleteJournal(String id) throws NoSuchIdException, RemoteException {
    synchronized (journals) {
      if (journals.remove(id) == null)
        throw new NoSuchIdException(id);
    }
  }

  /**
   * @see org.topazproject.ws.admin.Journal#setJournalInfo
   */
  public void setJournalInfo(String id, String journalDef)
      throws NoSuchIdException, RemoteException {
    synchronized (journals) {
      JournalInfo ji = (JournalInfo) journals.get(id);
      if (ji == null)
        throw new NoSuchIdException(id);

      ji.info = journalDef;
    }
  }

  /**
   * @see org.topazproject.ws.admin.Journal#getJournalInfo
   */
  public String getJournalInfo(String id) throws NoSuchIdException, RemoteException {
    synchronized (journals) {
      JournalInfo ji = (JournalInfo) journals.get(id);
      if (ji == null)
        throw new NoSuchIdException(id);

      return ji.info;
    }
  }

  /**
   * @see org.topazproject.ws.admin.Journal#listJournals
   */
  public String[] listJournals() throws RemoteException {
    synchronized (journals) {
      return (String[]) journals.keySet().toArray(new String[0]);
    }
  }

  /**
   * @see org.topazproject.ws.admin.Journal#createIssue
   */
  public void createIssue(String journalId, String issueNum)
      throws DuplicateIdException, NoSuchIdException, RemoteException {
    synchronized (journals) {
      JournalInfo ji = (JournalInfo) journals.get(journalId);
      if (ji == null)
        throw new NoSuchIdException(journalId);

      if (ji.issues.containsKey(issueNum))
        throw new DuplicateIdException(issueNum);

      ji.issues.put(issueNum, new IssueInfo());
    }
  }

  /**
   * @see org.topazproject.ws.admin.Journal#deleteIssue
   */
  public void deleteIssue(String journalId, String issueNum)
      throws NoSuchIdException, RemoteException {
    synchronized (journals) {
      JournalInfo ji = (JournalInfo) journals.get(journalId);
      if (ji == null)
        throw new NoSuchIdException(journalId);

      if (ji.issues.remove(issueNum) == null)
        throw new NoSuchIdException(issueNum);
    }
  }

  /**
   * @see org.topazproject.ws.admin.Journal#setIssueInfo
   */
  public void setIssueInfo(String journalId, String issueNum, String issueDef)
      throws NoSuchIdException, RemoteException {
    synchronized (journals) {
      JournalInfo ji = (JournalInfo) journals.get(journalId);
      if (ji == null)
        throw new NoSuchIdException(journalId);

      IssueInfo ii = (IssueInfo) ji.issues.get(issueNum);
      if (ii == null)
        throw new NoSuchIdException(issueNum);

      ii.info = issueDef;
    }
  }

  /**
   * @see org.topazproject.ws.admin.Journal#getIssueInfo
   */
  public String getIssueInfo(String journalId, String issueNum)
      throws NoSuchIdException, RemoteException {
    synchronized (journals) {
      JournalInfo ji = (JournalInfo) journals.get(journalId);
      if (ji == null)
        throw new NoSuchIdException(journalId);

      IssueInfo ii = (IssueInfo) ji.issues.get(issueNum);
      if (ii == null)
        throw new NoSuchIdException(issueNum);

      return ii.info;
    }
  }

  /**
   * @see org.topazproject.ws.admin.Journal#listIssues
   */
  public String[] listIssues(String journalId) throws NoSuchIdException, RemoteException {
    synchronized (journals) {
      JournalInfo ji = (JournalInfo) journals.get(journalId);
      if (ji == null)
        throw new NoSuchIdException(journalId);

      return (String[]) ji.issues.keySet().toArray(new String[0]);
    }
  }

  private static class JournalInfo {
    final Map issues = new HashMap();
    String info = null;
  }

  private static class IssueInfo {
    String info = null;
  }
}
