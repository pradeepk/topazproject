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

import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.plos.article.service.BrowseService;
import org.plos.journal.JournalService;
import org.plos.models.DublinCore;
import org.plos.models.Issue;
import org.plos.models.Journal;
import org.plos.models.Volume;

import org.springframework.beans.factory.annotation.Required;

import org.topazproject.otm.Session;
import org.topazproject.otm.Transaction;
import org.topazproject.otm.criterion.Restrictions;
import org.topazproject.otm.util.TransactionHelper;

/**
 * Allow Admin to Manage Volumes/ Issues.
 *
 * @author Jeff Suttor
 */
public class ManageVolumesIssuesAction extends BaseAdminActionSupport {

  public static final String CREATE_VOLUME = "CREATE_VOLUME";

  private String journalKey;
  private String journalEIssn;
  private String manageVolumesIssuesAction;
  private Journal journal;
  private List<Volume> volumes;
  private List<Issue> issues;
  private URI doi;
  private String displayName;
  private URI image;
  private URI prev;
  private URI next;
  private String aggregation;

  private Session session;
  private JournalService journalService;

  private static final Log log = LogFactory.getLog(ManageVolumesIssuesAction.class);

  /**
   * Manage Volumes/Issues.  Display Volumes/Issues and processes all adds/modifications/deletes.
   */
  public String execute() throws Exception  {

    if (log.isDebugEnabled()) {
      log.debug("journalKey: " + journalKey);
    }

    if (manageVolumesIssuesAction != null) {
      if (manageVolumesIssuesAction.equals(CREATE_VOLUME)) {
        createVolume();
      }
    }

    // JournalService, OTM usage wants to be in a Transaction
    TransactionHelper.doInTx(session,
      new TransactionHelper.Action<Void>() {

        public Void run(Transaction tx) {

          // get the Journal
          journal = journalService.getJournal(journalKey);
          if (journal == null) {
            final String errorMessage = "Error getting journal: " + journalKey;
            addActionMessage(errorMessage);
            log.error(errorMessage);
            return null;
          }

          // get Issues for this Journal
          issues = session.createCriteria(Issue.class)
                      .add(Restrictions.eq("journal", journal.getEIssn()))
                      .list();
          if (log.isDebugEnabled()) {
            log.debug(issues.size() + " Issue(s) for Journal " + journal.getEIssn());
          }

          // get Volumes for this Journal
          volumes = session.createCriteria(Volume.class)
                      .add(Restrictions.eq("journal", journal.getEIssn()))
                      .list();
          if (log.isDebugEnabled()) {
            log.debug(volumes.size() + " Volume(s) for Journal " + journal.getEIssn());
          }

          return null;
        }
      });

    // default action is just to display the template
    return SUCCESS;
  }

  /**
   * Create a Volume.
   *
   * Volume values taken from Struts Form.
   */
  private String createVolume() {

    // OTM usage wants to be in a Transaction
    TransactionHelper.doInTx(session,
      new TransactionHelper.Action<String>() {

        public String run(Transaction tx) {

          // the DOI must be unique
          if (session.get(Volume.class, doi.toString()) != null) {
            addActionMessage("Duplicate DOI, Volume, " + doi + ", already exists.");
            return ERROR;
          }

          Volume newVolume = new Volume();
          newVolume.setId(doi);
          DublinCore newDublinCore = new DublinCore();
          newDublinCore.setCreated(new Date());
          newVolume.setDublinCore(newDublinCore);
          newVolume.setJournal(journalEIssn);
          newVolume.setDisplayName(displayName);
          newVolume.setImage(image);
          newVolume.setPrevVolume(prev);
          newVolume.setNextVolume(next);

          // process Issues
          if (aggregation != null && aggregation.length() != 0) {
            List<URI> issues = new ArrayList();
            for (final String issueToAdd : aggregation.split(",")) {
              issues.add(URI.create(issueToAdd.trim()));
            }
            newVolume.setSimpleCollection(issues);
          }

          session.saveOrUpdate(newVolume);

          addActionMessage("Created Volume: (" + doi + ") " + newVolume.toString());

          return null;
        }
      });

    return SUCCESS;
  }

  /**
   * Gets all Volumes for the Journal.
   *
   * @return all Volumes for the Journal.
   */
  public List<Volume> getVolumes() {

    return volumes;
  }

  /**
   * Gets all Issues for a Journal.
   *
   * @return all Issues for the Journal.
   */
  public List<Issue> getIssues() {

    return issues;
  }

  /**
   * Set key of Journal.
   *
   * Enable Struts Form to set the Journal key from URI param and Form.
   *
   * @param journalKey of Journal.
   */
  public void setJournalKey(String journalKey) {
    this.journalKey = journalKey;
  }

  /**
   * Set eIssn of Journal.
   *
   * Enable Struts Form to set the Journal eIssn from URI param and Form.
   *
   * @param journalEIssn of Journal.
   */
  public void setJournalEIssn(String journalEIssn) {
    this.journalEIssn = journalEIssn;
  }

  /**
   * Set manageVolumesIssuesAction of Form.
   *
   * Enable Struts Form to set the manageVolumesIssuesAction.
   *
   * @param manageVolumesIssuesAction form action.
   */
  public void setManageVolumesIssuesAction(String manageVolumesIssuesAction) {
    this.manageVolumesIssuesAction = manageVolumesIssuesAction;
  }

  /**
   * Get the Journal.
   *
   * @return the Journal.
   */
  public Journal getJournal() {
    return journal;
  }

  /**
   * Set DOI.
   *
   * Enable Struts Form to set the DOI as a String.
   * The DOI is arbitrary, treated as opaque and encouraged to be human friendly.
   *
   * @param doi DOI.
   */
  public void setDoi(String doi) {
    this.doi = URI.create(doi);
  }

  /**
   * Set display name.
   *
   * Enable Struts Form to set the display name.
   *
   * @param displayName display name.
   */
  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  /**
   * Set image.
   *
   * Enable Struts Form to set the image URI as a String.
   *
   * @param image the image for this journal.
   */
  public void setImage(String image) {

    if (image == null || image.length() == 0) {
      this.image = null;
    } else {
      this.image = URI.create(image);
    }
  }

  /**
   * Set DOI of previous.
   *
   * Enable Struts Form to set the previous DOI as a String.
   *
   * @param doi DOI of previous.
   */
  public void setPrev(String prevDoi) {

    if (prevDoi == null || prevDoi.length() == 0) {
      this.prev = null;
    } else {
      this.prev = URI.create(prevDoi);
    }
  }

  /**
   * Set DOI of next.
   *
   * Enable Struts Form to set the next DOI as a String.
   *
   * @param doi DOI of next.
   */
  public void setNext(String nextDoi) {

    if (nextDoi == null || nextDoi.length() == 0) {
      this.next = null;
    } else {
      this.next = URI.create(nextDoi);
    }
  }

  /**
   * Set aggregation, comma separated list of Issue DOIs.
   *
   * Enable Struts Form to set the aggregation as a String.
   *
   * @param aggregation comma separated list of Issue DOIs.
   */
  public void setAggregation(String aggregation) {
    this.aggregation = aggregation;
  }

  /**
   * Set the OTM Session.
   *
   * @param session The OTM Session to set.
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
