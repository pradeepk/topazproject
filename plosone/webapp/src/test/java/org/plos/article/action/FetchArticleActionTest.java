/* $HeadURL::                                                                            $
 * $Id:FetchArticleActionTest.java 722 2006-10-02 16:42:45Z viru $
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
 package org.plos.article.action;

import com.opensymphony.xwork.Action;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.BasePlosoneTestCase;
import org.plos.annotation.action.BodyFetchAction;
import org.topazproject.common.DuplicateIdException;
import org.topazproject.common.NoSuchIdException;

import javax.activation.DataHandler;
import java.io.File;
import java.net.URL;

public class FetchArticleActionTest extends BasePlosoneTestCase {
  public static final Log log = LogFactory.getLog(FetchArticleActionTest.class);
  private String BASE_TEST_PATH = "src/test/resources/";

  public void testShouldReturnTransformedArticle() throws Exception {
//    final String resourceToIngest = BASE_TEST_PATH  + "pbio.0020042.zip";
//    final String resourceDOI = "10.1371/journal.pbio.0020042";

//    final String resourceToIngest = BASE_TEST_PATH  + "pone.0000008.zip";
//    String resourceDOI = "10.1371/journal.pone.0000008";
//
    final String resourceToIngest = BASE_TEST_PATH  + "pone.0000011.zip";
    String resourceDOI = "10.1371/journal.pone.0000011";


    try {
      getArticleWebService().delete(resourceDOI, true);
    } catch (NoSuchIdException nsie) {
      // ignore - this just means there wasn't any stale stuff left
    }

    URL article = getClass().getResource(resourceToIngest);
    if (null == article) {
      article = new File(resourceToIngest).toURL();
    }
    String doi = getArticleWebService().ingest(new DataHandler(article));
    assertEquals(doi, resourceDOI);

//    resourceDOI = "10.1371/journal.pone.0000011";

    final FetchArticleAction fetchArticleAction = getFetchArticleAction();
    fetchArticleAction.setArticleDOI(resourceDOI);

    String transformedArticle = "";
    for (int i = 0; i < 3; i++) {
      long t1 = System.currentTimeMillis();
      assertEquals(FetchArticleAction.SUCCESS, fetchArticleAction.execute());
      transformedArticle = fetchArticleAction.getTransformedArticle();
      log.info("Transformation time in secs:" + (System.currentTimeMillis() - t1)/1000.0);
    }
    assertNotNull(transformedArticle);
  }

  public void testFetchArticleReturnsLinkifiedText() throws Exception {
    final BodyFetchAction bodyFetchAction = getBodyFetchAction();
    bodyFetchAction.setBodyUrl("http://localhost:9080/existingArticle/test.xml");
    assertEquals(Action.SUCCESS, bodyFetchAction.execute());
    final String articleBody = bodyFetchAction.getBody();
    log.debug(articleBody);
    assertTrue(articleBody.contains("href=\""));
  }

  public void testShouldInjestArticle() throws Exception {
//    doIngestTest("10.1371/journal.pbio.0020294", BASE_TEST_PATH  + "test_article.zip");

    doIngestTest("10.1371/journal.pbio.0020042", BASE_TEST_PATH  + "pbio.0020042.zip");
//    doIngestTest("10.1371/journal.pone.0000008", BASE_TEST_PATH  + "pone.0000008.zip");
    
//    doIngestTest("10.1371/journal.pbio.0020294", BASE_TEST_PATH  + "pbio.0020294.zip");
//    doIngestTest("10.1371/journal.pbio.0020317", BASE_TEST_PATH  + "pbio.0020317.zip");
  }

  private void doIngestTest(String resourceDOI, String resourceToIngest) throws Exception {
    try {
      getArticleWebService().delete(resourceDOI, true);
    } catch (NoSuchIdException nsie) {
      // ignore - this just means there wasn't any stale stuff left
    }

    URL article = getClass().getResource(resourceToIngest);
    if (null == article) {
      article = new File(resourceToIngest).toURL();
    }

    String doi = getArticleWebService().ingest(new DataHandler(article));
    assertEquals(doi, resourceDOI);

    assertNotNull(getArticleWebService().getObjectURL(doi, "XML"));

    try {
      doi = getArticleWebService().ingest(new DataHandler(article));
      fail("Failed to get expected duplicate-id exception");
    } catch (DuplicateIdException die) {
    }

    getArticleWebService().delete(doi, true);

    try {
      getArticleWebService().delete(doi, true);
      fail("Failed to get NoSuchIdException");
    } catch (NoSuchIdException nsie) {
    }
  }

}
