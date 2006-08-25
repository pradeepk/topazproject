/* $HeadURL::                                                                             $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.ws.alerts;

import java.io.IOException;
import java.io.FileWriter;
import java.util.Iterator;
import java.util.Calendar;
import java.util.StringTokenizer;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import javax.activation.DataHandler;
import javax.xml.rpc.ServiceException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.codehaus.spice.salt.io.IOUtil;

import junit.framework.TestCase;

import org.topazproject.ws.article.NoSuchIdException;

import org.topazproject.ws.article.Article;
import org.topazproject.ws.article.ArticleServiceLocator;

import org.topazproject.ws.users.UserAccounts;
import org.topazproject.ws.users.UserAccountsServiceLocator;
import org.topazproject.ws.pap.UserPreference;
import org.topazproject.ws.pap.Preferences;
import org.topazproject.ws.pap.PreferencesServiceLocator;

import com.dumbster.smtp.SimpleSmtpServer;
import com.dumbster.smtp.SmtpMessage;

/*
 * TODO: Additional tests:
 * - Multiple users (should all get alerts)
 * - Auto-creation of user timestamp (don't call alerts.startUser)
 * - Are dates/date-ranges working properly (loose or get extra alerts
 *   for day(s) around which alerts are sent
 */

/**
 *
 */
public class AlertsServiceTest extends TestCase {
  private Alerts service;
  private Article articleService;
  
  private Preferences prefService;
  private UserAccounts userService;
  private String userId;
  
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
    
    // Create userid
    UserAccountsServiceLocator uaLoc = new UserAccountsServiceLocator();
    uaLoc.setMaintainSession(true);
    this.userService = uaLoc.getUserAccountsServicePort(new URL(USER_SVC_URL));
    this.userId = userService.createUser("alertTest");
    log.info("Created userid: " + this.userId);

    // Create preferences
    PreferencesServiceLocator prefLoc = new PreferencesServiceLocator();
    prefLoc.setMaintainSession(true);
    this.prefService = prefLoc.getPreferencesServicePort(new URL(PREF_SVC_URL));
    log.info("Created preferences service");
    
    UserPreference[] prefs = new UserPreference[2];
    prefs[0] = new UserPreference();
    prefs[0].setName("alertsCategories");
    prefs[0].setValues(new String[] { "Biotechnology", "Development" });
    prefs[1] = new UserPreference();
    prefs[1].setName("alertsEmailAddress");
    prefs[1].setValues(new String[] { "ebrown@topazproject.org" });
    this.prefService.setPreferences("alerts", this.userId, prefs);
    log.info("Created alerts preferences");
  }

  protected void tearDown() throws RemoteException {
    log.info("tearDown");
    
    // Delete the articles we created
    for (int i = 0; i < TEST_ARTICLES.length; i++)
      deleteArticle(TEST_ARTICLES[i][0]);
    
    // Delete our user
    try {
      this.userService.deleteUser(userId);
    } catch (NoSuchIdException nsie) {
      // already removed?
      log.warn(nsie);
    }

    // TODO: Do I need to delete preferences?
  }

  protected void ingestArticle(String doi, String resource) throws RemoteException {
    log.info("ingesting article " + doi + " : " + resource);
    deleteArticle(doi);
    URL article = getClass().getResource(resource);
    assertEquals("Wrong doi returned,", doi, articleService.ingest(new DataHandler(article)));
    log.info("ingested article " + doi);
  }

  protected void deleteArticle(String doi) throws RemoteException {
    try {
      articleService.delete(doi, true);
      log.info("deleted article " + doi);
//    } catch (NoSuchIdException nsie) {
    } catch (Exception nsie) {
      // so what
      //log.debug(nsie);
    }
  }


  /**
   * Do all/most tests in one place to avoid running setUp tearDown multiple times.
   * They just take a long time to run.
   */
  public void testFeeds() throws RemoteException, IOException {
    boolean success = true;
    
    success &= this.tstEmail0();
    success &= this.tstEmail1();
    success &= this.tstEmail2();

    // Test the xml for the RSS feeds
    success &= this.tstEntireFeed();
    success &= this.tstBiotechnology();
    success &= this.tstStartDate();
    success &= this.tstMultiCategory();
    success &= this.tstDateRange();
    success &= this.tstAuthors();
    success &= this.tstNoResults();

    // We do it this way because if something underneath changes, it is possible that
    // there is a good reason all these tests fail. Thus, we'd like them still all to
    // run so that their test files can be copied over the expected files.
    assertTrue(success);
  }
  
  private boolean tstEntireFeed() throws RemoteException, IOException {
    // Get all the articles we have
    return this.testFeed(null, null, null, null, "entirefeed.xml");
  }

  private boolean tstBiotechnology() throws RemoteException, IOException {
    String[] categories = new String[] { "Biotechnology" };
    return this.testFeed(null, null, categories, null, "biotechfeed.xml");
  }

  private boolean tstStartDate() throws RemoteException, IOException {
    return this.testFeed("2004-08-31", null, null, null, "start08-31.xml");
  }

  private boolean tstMultiCategory() throws RemoteException, IOException {
    String[] categories = new String[] { "Biotechnology", "Microbiology" };
    return this.testFeed(null, null, categories, null, "multicategory.xml");
  }

  private boolean tstDateRange() throws RemoteException, IOException {
    return this.testFeed("2004-08-24", "2004-08-31", null, null, "daterange8-24_31.xml");
  }

  private boolean tstAuthors() throws RemoteException, IOException {
    String[] authors = new String[] { "Richard J Roberts" };
    return this.testFeed(null, null, null, authors, "author_roberts.xml");
  }

  private boolean tstNoResults() throws RemoteException, IOException {
    String [] categories = new String[] { "Bogus Categories" };
    return this.testFeed(null, null, categories, null, "noresults.xml");
  }

  private boolean tstEmail0() throws RemoteException, IOException {
    Calendar c = Calendar.getInstance();
    
    this.service.clearUser(userId);
    c.set(2004, 2, 10);
    this.service.startUser(userId, c);

    c.set(2004, 3, 17);
    return this.testEmail(c, "tst0", 0, 0);
  }

  private boolean tstEmail1() throws RemoteException, IOException {
    Calendar c = Calendar.getInstance();
    
    this.service.clearUser(userId);
    c.set(2004, 2, 10);
    this.service.startUser(userId, c);

    c.set(2004, 8, 26);
    return this.testEmail(c, "tst1", 1, 1);
  }

  private boolean tstEmail2() throws RemoteException {
    Calendar c = Calendar.getInstance();
    
    this.service.clearUser(userId);
    c.set(2004, 2, 10);
    this.service.startUser(userId, c);

    c.set(2004, 10, 13);
    return this.testEmail(c, "tst2", 1, 2);
  }
  
  private boolean testEmail(Calendar alertCal, String tstName,
                            int expectedMsgs, int expectedArticles) throws RemoteException {
    SimpleSmtpServer server = startSmtpServer(2525);

    boolean success = this.service.sendAlerts(alertCal, 10);
    assertTrue(success);

    server.stop();
    
    int msgCnt = server.getReceivedEmailSize();
    log.info("Email '" + tstName + "' at " + alertCal.getTime() + " got " +
             msgCnt + " message(s)");
    
    int cnt = 0;
    for (Iterator emailIt = server.getReceivedEmail(); emailIt.hasNext(); ) {
      SmtpMessage email = (SmtpMessage)emailIt.next();
      String articles = email.getHeaderValue("X-Topaz-Articles");
      int articleCnt = new StringTokenizer(articles, ",").countTokens(); // hack, but works
      log.info(tstName + "_" + cnt + ": " + articleCnt + " article(s): " + articles);
      writeResult(email.getBody(), "/tmp/" + tstName + "_" + cnt + ".msg");
      cnt++;

      // See if we got the number of articles we expected
      // (Should never have 0 articles or will not have an email)
      if (expectedArticles >= 0) {
        if (expectedArticles != articleCnt)
        {
          log.warn("Expected " + expectedArticles + ", God " + articleCnt);
          success = false;
        }
      }
    }
    
    assertTrue(success);
    if (msgCnt != expectedMsgs) {
      log.warn("Expected " + expectedMsgs + ", Got " + msgCnt);
      success = false;
    }

    return success;
  }

  private boolean testFeed(String startDate, String endDate, String[] categories, String[] authors,
                           String resourceName) throws RemoteException, IOException {
    log.info("Running test: " + resourceName);
    
    String testResult = this.service.getFeed(startDate, endDate, categories, authors);

    // Write result in case we need it (UTF-8 issues to get expected result initally)
    //writeResult(testResult, "/tmp/" + resourceName);

    // Read the result we expect from a resource
    String desiredResult =
      IOUtil.toString(getClass().getResourceAsStream("/" + resourceName), "UTF-8");
    
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
    
    if (!testResult.trim().equals(desiredResult.trim())) {
      log.warn("Comparison failed on " + resourceName);
      writeResult(testResult, "/tmp/" + resourceName);
    }

    boolean success = desiredResult.trim().equals(testResult.trim());
    if (!success)
      log.warn(comment);

    return success;
  }

  /**
   * Fix SimpleSmtpServer dead-lock.
   *
   * Basically apply patch 1310992 for bug 1179454 to version 1.6 of dumbster.
   */
  private static SimpleSmtpServer startSmtpServer(int port) {
    SimpleSmtpServer server = new SimpleSmtpServer(port);
    Thread t = new Thread(server);

    synchronized (server) {
      t.start();

      // Block until the server socket is created
      try {
        server.wait();
      } catch (InterruptedException e) {
        log.info("SimpleSmptServer interrupted", e);
      }
      return server;
    }
  }
  
  /**
   * Write expected results to a file because if articles contain any UTF-8 characters,
   * copying the expected results to resources directory is easier than trying to type
   * in expected results.
   *
   * @param result is the xml string of the results we want
   * @param fileName is the name of the file to write (usually /tmp/something...)
   */
  private static void writeResult(String result, String fileName) {
    try {
      FileWriter fw = new FileWriter(fileName);
      IOUtil.copy(result.getBytes("UTF-8"), fw);
      fw.close();
    } catch (IOException ie) {
      log.warn("Unable to write " + fileName, ie);
    }
  }
}
