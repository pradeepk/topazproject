/* $HeadURL::                                                                             $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.ws.alerts.service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import javax.activation.DataHandler;
import javax.xml.rpc.ServiceException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.FileWriter;
import org.apache.avalon.excalibur.io.IOUtil;

import junit.framework.TestCase;

import org.topazproject.ws.article.service.NoSuchIdException;

import org.topazproject.ws.article.service.Article;
import org.topazproject.ws.article.service.ArticleServiceLocator;

/**
 *
 */
public class AlertsServiceTest extends TestCase {
  private Alerts service;
  private Article articleService;
  
  private static final Log log = LogFactory.getLog(AlertsServiceTest.class);
  
  protected static final String ALERTS_SVC_URL =
    "http://localhost:9997/ws-alerts-webapp-0.5-SNAPSHOT/services/AlertsServicePort";
  protected static final String ARTICLES_SVC_URL =
    "http://localhost:9997/ws-articles-webapp-0.1/services/ArticleServicePort";
  protected static final String PREF_SVC_URL =
    "http://localhost:9997/ws-pap-webapp-0.1-SNAPSHOT/services/PreferencesServicePort";
  protected static final String USER_SVC_URL =
    "http://localhost:9997/ws-users-webapp-0.1-SNAPSHOT/services/UserAccountsServicePort";
  protected static final String[][] TEST_ARTICLES = {
    { "10.1371/journal.pbio.0020294", "/pbio.0020294.zip" },
    { "10.1371/journal.pbio.0020042", "/pbio.0020042.zip" },
    { "10.1371/journal.pbio.0020317", "/pbio.0020317.zip" },
    { "10.1371/journal.pbio.0020382", "/pbio.0020382.zip" },
  };

  public AlertsServiceTest(String testName) {
    super(testName);
  }

  protected void setUp() throws MalformedURLException, ServiceException, RemoteException {
    log.info("setUp");
    
    // Create alerts service
    AlertsServiceLocator alertsLoc = new AlertsServiceLocator();
    alertsLoc.setMaintainSession(true);
    this.service = alertsLoc.getAlertsServicePort(new URL(ALERTS_SVC_URL));
    log.info("created alerts service");

    // Create articles service
    ArticleServiceLocator articleLoc = new ArticleServiceLocator();
    articleLoc.setMaintainSession(true);
    this.articleService = articleLoc.getArticleServicePort(new URL(ARTICLES_SVC_URL));
    log.info("ingesting article(s)");
    for (int i = 0; i < TEST_ARTICLES.length; i++)
      ingestArticle(TEST_ARTICLES[i][0], TEST_ARTICLES[i][1]);
  }

  protected void tearDown() throws RemoteException {
    log.info("tearDown");
    
    // Delete the articles we created
    for (int i = 0; i < TEST_ARTICLES.length; i++)
      deleteArticle(TEST_ARTICLES[i][0]);
  }

  protected void ingestArticle(String doi, String resource) throws RemoteException {
    deleteArticle(doi);
    URL article = getClass().getResource(resource);
    assertEquals("Wrong doi returned,", doi, articleService.ingest(new DataHandler(article)));
    log.info("ingested article " + doi);
  }

  protected void deleteArticle(String doi) throws RemoteException {
    try {
      articleService.delete(doi, true);
      log.info("deleted article " + doi);
    } catch (NoSuchIdException nsie) {
      // so what
    }
  }


  /**
   * Do all/most tests in one place to avoid running setUp tearDown multiple times.
   * They just take a long time to run.
   */
  public void testFeeds() throws RemoteException, IOException {
    this.tstEntireFeed();
    this.tstBiotechnology();
    this.tstStartDate();
    this.tstMultiCategory();
    this.tstDateRange();
    this.tstAuthors();
    this.tstNoResults();
  }
    
  
  private void tstEntireFeed() throws RemoteException, IOException {
    // Get all the articles we have
    this.testFeed(null, null, null, null, "entirefeed.xml");
  }

  private void tstBiotechnology() throws RemoteException, IOException {
    String[] categories = new String[] { "Biotechnology" };
    this.testFeed(null, null, categories, null, "biotechfeed.xml");
  }

  private void tstStartDate() throws RemoteException, IOException {
    this.testFeed("2004-08-31", null, null, null, "start08-31.xml");
  }

  private void tstMultiCategory() throws RemoteException, IOException {
    String[] categories = new String[] { "Biotechnology", "Microbiology" };
    this.testFeed(null, null, categories, null, "multicategory.xml");
  }

  private void tstDateRange() throws RemoteException, IOException {
    this.testFeed("2004-08-24", "2004-08-31", null, null, "daterange8-24_31.xml");
  }

  private void tstAuthors() throws RemoteException, IOException {
    String[] authors = new String[] { "Roberts, Richard J" };
    this.testFeed(null, null, null, authors, "author_roberts.xml");
  }

  private void tstNoResults() throws RemoteException, IOException {
    String [] categories = new String[] { "Bogus Categories" };
    this.testFeed(null, null, categories, null, "noresults.xml");
  }
  

  private void testFeed(String startDate, String endDate, String[] categories, String[] authors,
                        String resourceName) throws RemoteException, IOException {
    log.info("Running test: " + resourceName);
    
    String testResult = this.service.getFeed(startDate, endDate, categories, authors);

    // Write result in case we need it (UTF-8 issues to get expected result initally)
    //writeResult(testResult, "/tmp/" + resourceName);

    // Read the result we expect from a resource
    String desiredResult = IOUtil.toString(getClass().getResourceAsStream("/" + resourceName));
    
    String comment = resourceName + " did not match search from " + startDate +
      " to " + endDate + " on categories (";
    if (categories != null)
      for (int i = 0; i < categories.length; i++)
        comment += categories[i] + " ";
    comment += ") or authors (";
    if (authors != null)
      for (int i = 0; i < authors.length; i++)
        comment += authors[i];
    comment += ")";
    assertEquals(comment, testResult.trim(), desiredResult.trim());
  }

  /**
   * Write expected results to a file because if articles contain any UTF-8 characters,
   * copying the expected results to resources directory is easier than trying to type
   * in expected results.
   *
   * @param result is the xml string of the results we want
   * @param fileName is the name of the file to write (usually /tmp/something...)
   */
  private static void writeResult(String result, String fileName) throws IOException {
    FileWriter fw = new FileWriter(fileName);
    IOUtil.copy(result, fw);
    fw.close();
  }

}
