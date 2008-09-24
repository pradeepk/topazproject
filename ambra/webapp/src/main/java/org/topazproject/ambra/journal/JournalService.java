/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.topazproject.ambra.journal;

import java.net.URI;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.ambra.models.Journal;
import org.topazproject.otm.Session;


import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;

/**
 * This service manages journal definitions and associated info. All retrievals and modifications
 * should go through here so it can keep the cache up-to-date.
 *
 * <p>There should be exactly one instance of this class per {@link
 * org.topazproject.otm.SessionFactory SessionFactory} instance. Also, the instance must be created
 * before any {@link org.topazproject.otm.Session Session} instance is created as it needs to
 * register the filter-definition with the session-factory.
 *
 * <p>This services does extensive caching of journal objects, the filters associated with each
 * journal, and the list of journals each object (article) belongs to (according to the filters).
 * For this reason it must be notified any time a journal or article is added, removed, or changed.
 *
 * @author Ronald Tschalär
 */
public class JournalService {
  private static final Log    log = LogFactory.getLog(JournalService.class);
  private static final String RI_MODEL = "ri";

  private JournalKeyService      journalKeyService;
  private JournalFilterService   journalFilterService;
  private JournalCarrierService  journalCarrierService;

  private Session                session;

  /**
   * Create a new journal-service instance. One and only one of these should be created for every
   * session-factory instance, and this must be done before the first session instance is created.
   */
  public JournalService() {
  }

  /**
   * Get the names of the {@link org.topazproject.otm.Filter session filters} associated with the
   * specified journal.
   *
   * @param jName the journal's name (key)
   * @return the list of filters (which may be empty), or null if no journal by the given name is
   *         known
   */
  @Transactional(readOnly = true)
  public Set<String> getFilters(String jName) {
    return journalFilterService.getFilters(jName, session);
  }

  /**
   * Get the specified journal.
   *
   * @param jName  the journal's name
   * @return the journal, or null if no found
   */
  @Transactional(readOnly = true)
  public Journal getJournal(String jName) {
    return journalKeyService.getJournal(jName, session);
  }

  /**
   * Get the current journal.
   *
   * @return the current journal
   */
  @Transactional(readOnly = true)
  public Journal getCurrentJournal() {
    return journalKeyService.getCurrentJournal(session);
  }

  /**
   * Get the set of all the known journals.
   *
   * @return all the journals, or the empty set if there are none
   */
  @Transactional(readOnly = true)
  public Set<Journal> getAllJournals() {
    return journalKeyService.getAllJournals(session);
  }

  /**
   * Get the names of all the known journals.
   *
   * @return the list of names; may be empty if there are no known journals
   */
  @Transactional(readOnly = true)
  public Set<String> getAllJournalNames() {
    return journalKeyService.getAllJournalKeys(session);
  }

  /**
   * Get the name of the current journal.
   *
   * @return the name of the current journal, or null if there is no current journal
   */
  public String getCurrentJournalName() {
    return journalKeyService.getCurrentJournalKey();
  }

  /**
   * Get the list of journals which carry the given object (e.g. article).
   *
   * @param oid the info:&lt;oid&gt; uri of the object
   * @return the list of journals which carry this object; will be empty if this object
   *         doesn't belong to any journal
   */
  @Transactional(readOnly = true)
  public Set<Journal> getJournalsForObject(URI oid) {
    return journalCarrierService.getJournalsForObject(oid, session);
  }

  /**
   * Set the OTM session. Called by spring's bean wiring.
   *
   * @param session the otm session
   */
  @Required
  public void setOtmSession(Session session) {
    this.session = session;
  }

  /**
   * Set the key service. Called by spring's bean wiring.
   *
   * @param journalKeyService key service
   */
  @Required
  public void setJournalKeyService(JournalKeyService journalKeyService) {
    this.journalKeyService = journalKeyService;
  }

  /**
   * Set the filter service. Called by spring's bean wiring.
   *
   * @param journalFilterService filter service
   */
  @Required
  public void setJournalFilterService(JournalFilterService journalFilterService) {
    this.journalFilterService = journalFilterService;
  }

  /**
   * Set the carrier service. Called by spring's bean wiring.
   *
   * @param journalCarrierService carrier service
   */
  @Required
  public void setJournalCarrierService(JournalCarrierService journalCarrierService) {
    this.journalCarrierService = journalCarrierService;
  }
}
