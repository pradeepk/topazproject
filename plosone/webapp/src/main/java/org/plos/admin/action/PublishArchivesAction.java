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
import java.rmi.RemoteException;
import java.util.List;
import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts2.util.ServletContextAware;

import org.plos.ApplicationException;
import org.plos.journal.JournalService;
import org.plos.models.Journal;

import org.springframework.beans.factory.annotation.Required;

import org.topazproject.otm.Session;
import org.topazproject.otm.Transaction;
import org.topazproject.otm.util.TransactionHelper;

public class PublishArchivesAction extends BaseAdminActionSupport implements ServletContextAware {

  private static final Log log = LogFactory.getLog(PublishArchivesAction.class);
  private String[] articlesToPublish;
  private String[] articlesInVirtualJournals;
  private String[] articlesToDelete;
  private ServletContext servletContext;
  private Session session;
  private JournalService journalService;

  /**
   * Deletes and publishes checked articles from the admin console.  Note that delete has priority
   * over publish.
   *
   */
  public String execute() throws RemoteException, ApplicationException  {
    deleteArticles();
    publishArticles();
    return base();
  }

  /**
   * publishes articles from the admin console
   *
   * @throws RemoteException
   * @throws ApplicationException
   */
  public void publishArticles () throws RemoteException, ApplicationException  {
    if (articlesToPublish != null){
      for (final String article : articlesToPublish) {
        try {
          getDocumentManagementService().publish(article,
                  article == articlesToPublish[articlesToPublish.length - 1]);
          if (log.isDebugEnabled()) {
            log.debug("published article: " + article);
          }
          addActionMessage("Published: " + article);

          // will this article be published in any Virtual Journals?
          if (articlesInVirtualJournals != null) {
            for (String articleInVirtualJournal : articlesInVirtualJournals) {
              // form builds checkbox value as "article" + "::" + "virtualJournal"
              if (!articleInVirtualJournal.startsWith(article + "::")) {
                continue;
              }

              final String virtualJournal = articleInVirtualJournal.split("::")[1];

              TransactionHelper.doInTx(session,
                new TransactionHelper.Action<Void>() {

                public Void run(Transaction tx) {

                  // get Journal by name
                  final Journal journal = journalService.getJournal(virtualJournal);
                  if (journal == null) {
                    final String errorMessage = "Error adding article " + article
                      + " to non-existent journal " + virtualJournal;
                    addActionMessage(errorMessage);
                    log.error(errorMessage);
                    return null;
                  }

                  // add Article to Journal
                  List<URI> articlesInJournal = journal.getSimpleCollection();
                  articlesInJournal.add(URI.create(article));
                  journal.setSimpleCollection(articlesInJournal);

                  // update Journal
                  session.saveOrUpdate(journal);
                  journalService.journalWasModified(journal);

                  final String message = "Article " + article
                      + " was published in the journal " + virtualJournal;
                  addActionMessage(message);
                  if (log.isDebugEnabled()) {
                    log.debug(message);
                  }

                  return null;
                }
              });
            }
          }
        } catch (Exception e) {
          addActionMessage("Error publishing: " + article + " - " + e.toString());
          log.warn ("Could not publish article: " + article, e);
        }
      }
    }
  }

  /**
   * Deletes the checked articles from the admin console.
   *
   * @throws RemoteException
   * @throws ApplicationException
   */
  public void deleteArticles() throws RemoteException, ApplicationException  {
    if (articlesToDelete != null) {
      for (String article : articlesToDelete) {
        try {
          getDocumentManagementService().delete(article, getServletContext());
          if (log.isDebugEnabled()) {
            log.debug("deleted article: " + article);
          }
          addActionMessage("Deleted: " + article);
        } catch (Exception e) {
          addActionMessage("Error deleting: " + article + " - " + e.toString());
          log.warn ("Could not delete article: " + article, e);
        }
      }
    }
  }

  /**
   *
   * @param articles array of articles to publish
   */
  public void setArticlesToPublish(String[] articles) {
    articlesToPublish = articles;
  }

  /**
   *
   * @param articlesInVirtualJournals array of ${virtualJournal} + "::" + ${article} to publish.
   */
  public void setArticlesInVirtualJournals(String[] articlesInVirtualJournals) {
    this.articlesInVirtualJournals = articlesInVirtualJournals;
  }

  /**
   *
   * @param articles array of articles to delete
   */
  public void setArticlesToDelete(String[] articles) {
    articlesToDelete= articles;
  }

  /**
   * Sets the servlet context.  Needed in order to clear the image cache
   *
   * @param context SerlvetContext to set
   */
  public void setServletContext (ServletContext context) {
    this.servletContext = context;
  }

  private ServletContext getServletContext () {
    return this.servletContext;
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
