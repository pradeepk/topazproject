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
import org.plos.ApplicationException;
import org.plos.BasePlosoneTestCase;
import org.plos.annotation.action.BodyFetchAction;
import org.topazproject.common.DuplicateIdException;
import org.topazproject.common.NoSuchIdException;

import javax.activation.DataHandler;
import javax.xml.rpc.ServiceException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;

public class FetchArticleActionTest extends BasePlosoneTestCase {
  public static final Log log = LogFactory.getLog(FetchArticleActionTest.class);
  private String BASE_TEST_PATH = "webapp/src/test/resources/";

//  public void testSecondaryObjects() throws Exception {
//    // some NoSuchArticleIdException tests
//    boolean gotE = false;
//    try {
//      service.listSecondaryObjects("blah/foo");
//    } catch (NoSuchArticleIdException nsaie) {
//      assertEquals("Mismatched id in exception, ", "blah/foo", nsaie.getId());
//      gotE = true;
//    }
//    assertTrue("Failed to get expected no-such-object-id exception", gotE);
//
//    // ingest article and test listRepresentations()
//    URL article = getClass().getResource("/pbio.0020294.zip");
//    String doi = service.ingest(new DataHandler(article));
//    assertEquals("Wrong doi returned,", "10.1371/journal.pbio.0020294", doi);
//
//    ObjectInfo[] oi = service.listSecondaryObjects(doi);
//    assertEquals("wrong number of object-infos", 8, oi.length);
//
//    assertEquals("doi mismatch", "10.1371/journal.pbio.0020294.g001", oi[0].getDoi());
//    assertEquals("doi mismatch", "10.1371/journal.pbio.0020294.g002", oi[1].getDoi());
//    assertEquals("doi mismatch", "10.1371/journal.pbio.0020294.g003", oi[2].getDoi());
//    assertEquals("doi mismatch", "10.1371/journal.pbio.0020294.g004", oi[3].getDoi());
//    assertEquals("doi mismatch", "10.1371/journal.pbio.0020294.g005", oi[4].getDoi());
//    assertEquals("doi mismatch", "10.1371/journal.pbio.0020294.g006", oi[5].getDoi());
//    assertEquals("doi mismatch", "10.1371/journal.pbio.0020294.sv001", oi[6].getDoi());
//    assertEquals("doi mismatch", "10.1371/journal.pbio.0020294.sv002", oi[7].getDoi());
//
//    assertEquals("label mismatch", "Figure 1", oi[0].getTitle());
//    assertEquals("label mismatch", "Figure 2", oi[1].getTitle());
//    assertEquals("label mismatch", "Figure 3", oi[2].getTitle());
//    assertEquals("label mismatch", "Figure 4", oi[3].getTitle());
//    assertEquals("label mismatch", "Figure 5", oi[4].getTitle());
//    assertEquals("label mismatch", "Figure 6", oi[5].getTitle());
//    assertEquals("label mismatch", "Video S1", oi[6].getTitle());
//    assertEquals("label mismatch", "Video S2", oi[7].getTitle());
//
//    assertNotNull("missing description", oi[0].getDescription());
//    assertNotNull("missing description", oi[1].getDescription());
//    assertNotNull("missing description", oi[2].getDescription());
//    assertNotNull("missing description", oi[3].getDescription());
//    assertNotNull("missing description", oi[4].getDescription());
//    assertNotNull("missing description", oi[5].getDescription());
//    assertNotNull("missing description", oi[6].getDescription());
//    assertNotNull("missing description", oi[7].getDescription());
//
//    assertEquals("wrong number of rep-infos", 2, oi[0].getRepresentations().length);
//    assertEquals("wrong number of rep-infos", 1, oi[1].getRepresentations().length);
//    assertEquals("wrong number of rep-infos", 1, oi[2].getRepresentations().length);
//    assertEquals("wrong number of rep-infos", 1, oi[3].getRepresentations().length);
//    assertEquals("wrong number of rep-infos", 1, oi[4].getRepresentations().length);
//    assertEquals("wrong number of rep-infos", 1, oi[5].getRepresentations().length);
//    assertEquals("wrong number of rep-infos", 1, oi[6].getRepresentations().length);
//    assertEquals("wrong number of rep-infos", 1, oi[7].getRepresentations().length);
//
//    sort(oi[0].getRepresentations());
//
//    RepresentationInfo ri = oi[0].getRepresentations()[0];
//    assertEquals("ri-name mismatch", "PNG", ri.getName());
//    assertEquals("ri-cont-type mismatch", "image/png", ri.getContentType());
//    assertEquals("ri-size mismatch", 52422L, ri.getSize());
//
//    ri = oi[0].getRepresentations()[1];
//    assertEquals("ri-name mismatch", "TIF", ri.getName());
//    assertEquals("ri-cont-type mismatch", "image/tiff", ri.getContentType());
//    assertEquals("ri-size mismatch", 120432L, ri.getSize());
//
//    ri = oi[1].getRepresentations()[0];
//    assertEquals("ri-name mismatch", "TIF", ri.getName());
//    assertEquals("ri-cont-type mismatch", "image/tiff", ri.getContentType());
//    assertEquals("ri-size mismatch", 375480L, ri.getSize());
//
//    ri = oi[2].getRepresentations()[0];
//    assertEquals("ri-name mismatch", "TIF", ri.getName());
//    assertEquals("ri-cont-type mismatch", "image/tiff", ri.getContentType());
//    assertEquals("ri-size mismatch", 170324L, ri.getSize());
//
//    ri = oi[3].getRepresentations()[0];
//    assertEquals("ri-name mismatch", "TIF", ri.getName());
//    assertEquals("ri-cont-type mismatch", "image/tiff", ri.getContentType());
//    assertEquals("ri-size mismatch", 458812L, ri.getSize());
//
//    ri = oi[4].getRepresentations()[0];
//    assertEquals("ri-name mismatch", "TIF", ri.getName());
//    assertEquals("ri-cont-type mismatch", "image/tiff", ri.getContentType());
//    assertEquals("ri-size mismatch", 164130L, ri.getSize());
//
//    ri = oi[5].getRepresentations()[0];
//    assertEquals("ri-name mismatch", "TIF", ri.getName());
//    assertEquals("ri-cont-type mismatch", "image/tiff", ri.getContentType());
//    assertEquals("ri-size mismatch", 101566L, ri.getSize());
//
//    ri = oi[6].getRepresentations()[0];
//    assertEquals("ri-name mismatch", "MOV", ri.getName());
//    assertEquals("ri-cont-type mismatch", "video/quicktime", ri.getContentType());
//    assertEquals("ri-size mismatch", 0L, ri.getSize());
//
//    ri = oi[7].getRepresentations()[0];
//    assertEquals("ri-name mismatch", "MOV", ri.getName());
//    assertEquals("ri-cont-type mismatch", "video/quicktime", ri.getContentType());
//    assertEquals("ri-size mismatch", 0L, ri.getSize());
//
//    // clean up
//    service.delete(doi, true);
//  }

  public void testShouldReturnTransformedArticle() throws Exception {
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

    final URL article = getAsUrl(resourceToIngest);
    String doi = getArticleWebService().ingest(article);
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

  public void testListArticles() throws MalformedURLException, ServiceException, ApplicationException {
    Collection<String> articles = getFetchArticleService().getArticles(null, null);
    for (final String article : articles) {
      log.debug("article = " + article);
    }
  }

  private void doIngestTest(String resourceDOI, String resourceToIngest) throws Exception {
    try {
      getArticleWebService().delete(resourceDOI, true);
    } catch (NoSuchIdException nsie) {
      // ignore - this just means there wasn't any stale stuff left
    }

    final URL article = getAsUrl(resourceToIngest);

    String doi = getArticleWebService().ingest(article);
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
