/* $$HeadURL::                                                                            $$
 * $$Id$$
 *
 * Copyright (c) 2006-2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.plos.admin.action;

import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.plos.journal.JournalService;
import org.plos.models.Journal;

import org.springframework.beans.factory.annotation.Required;

import org.topazproject.otm.Session;
import org.topazproject.otm.Transaction;
import org.topazproject.otm.util.TransactionHelper;

/**
 * Allow Admin to View all virtual Journals.
 */
public class ViewVirtualJournalsAction extends BaseAdminActionSupport {

  private Set<Journal> journals;
  private Session session;
  private JournalService journalService;

  private static final Log log = LogFactory.getLog(ViewVirtualJournalsAction.class);

  /**
   * Gets all virtual Journals.
   */
  public String execute() throws Exception  {

    TransactionHelper.doInTx(session,
      new TransactionHelper.Action<Void>() {

        public Void run(Transaction tx) {
          // get Journals
          journals = journalService.getAllJournals();

          return null;
        }
      });

    // default action is just to display the template
    return SUCCESS;
  }

  /**
   * Gets all virtual Journals.
   *
   * @return All virtual Journals.
   */
  public Set<Journal> getJournals() {
    return journals;
  }

  /**
   * Sets the otm util.
   *
   * @param session The otm session to set.
   */
  @Required
  public void setOtmSession(Session session) {
    this.session = session;
  }

  /**
   * Sets the JournalService.
   *
   * @param journalService The JournalService to set.
   */
  @Required
  public void setJournalService(JournalService journalService) {
    this.journalService = journalService;
  }
}
