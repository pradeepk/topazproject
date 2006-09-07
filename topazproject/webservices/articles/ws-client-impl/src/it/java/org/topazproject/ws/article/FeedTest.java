/* $HeadURL::                                                                                     $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.ws.article;

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

import org.apache.commons.io.IOUtils;
import junit.framework.TestCase;

public class FeedTest extends TestCase {
  private Article service;
  
  private static final Log log = LogFactory.getLog(FeedTest.class);
  
  protected static final String ARTICLES_SVC_URL =
    "http://localhost:9997/ws-articles-webapp-0.5-SNAPSHOT/services/ArticleServicePort";
  
  protected static final String[][] TEST_ARTICLES = {
    { "10.1371/journal.pbio.0020294", "/pbio.0020294.zip" },
    { "10.1371/journal.pbio.0020042", "/pbio.0020042.zip" },
    { "10.1371/journal.pbio.0020317", "/pbio.0020317.zip" },
    { "10.1371/journal.pbio.0020382", "/pbio.0020382.zip" },
  };

  public FeedTest(String testName) {
    super(testName);
  }

  protected void setUp() throws MalformedURLException, ServiceException, RemoteException {
    log.info("setUp");
    
    // Create articles service
    service = ArticleClientFactory.create(ARTICLES_SVC_URL);
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
    log.info("ingesting article " + doi + " : " + resource);
    deleteArticle(doi);
    URL article = getClass().getResource(resource);
    assertEquals("Wrong doi returned,", doi, service.ingest(new DataHandler(article)));
    log.info("ingested article " + doi);
  }

  protected void deleteArticle(String doi) throws RemoteException {
    try {
      service.delete(doi, true);
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
  public void testFeeds() throws Exception {
    boolean success = true;
    
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

  private boolean tstStartDate() throws Exception {
    return this.testFeed("2004-08-31", null, null, null, "start08-31.xml");
  }

  private boolean tstMultiCategory() throws RemoteException, IOException {
    String[] categories = new String[] { "Biotechnology", "Microbiology" };
    return this.testFeed(null, null, categories, null, "multicategory.xml");
  }

  private boolean tstDateRange() throws Exception {
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

  private boolean testFeed(String startDate, String endDate, String[] categories, String[] authors,
                           String resourceName) throws RemoteException, IOException {
    log.info("Running test: " + resourceName);
    
    String testResult = service.getArticles(startDate, endDate, categories, authors, false);

    // Write result in case we need it (UTF-8 issues to get expected result initally)
    //writeResult(testResult, "/tmp/" + resourceName);

    // Read the result we expect from a resource
    String desiredResult =
      IOUtils.toString(getClass().getResourceAsStream("/" + resourceName), "UTF-8");
    
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
      IOUtils.write(result.getBytes("UTF-8"), fw);
      fw.close();
    } catch (IOException ie) {
      log.warn("Unable to write " + fileName, ie);
    }
  }
}
