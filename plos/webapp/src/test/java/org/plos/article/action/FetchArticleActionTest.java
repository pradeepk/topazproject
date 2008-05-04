/* $HeadURL::                                                                            $
 * $Id:FetchArticleActionTest.java 722 2006-10-02 16:42:45Z viru $
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
 package org.plos.article.action;

import com.opensymphony.xwork2.Action;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.ApplicationException;
import org.plos.BasePlosoneTestCase;
import org.plos.article.service.NoSuchArticleIdException;
import org.plos.article.service.DuplicateArticleIdException;
import org.plos.annotation.action.BodyFetchAction;
import org.plos.models.Article;

import javax.activation.URLDataSource;
import javax.xml.rpc.ServiceException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;

public class FetchArticleActionTest extends BasePlosoneTestCase {
  public static final Log log = LogFactory.getLog(FetchArticleActionTest.class);
  private String BASE_TEST_PATH = "src/test/resources/";
  private String testXmlTarget = "file:src/test/resources/test.xml";

  public void testShouldReturnTransformedArticle() throws Exception {
    final String resourceToIngest = BASE_TEST_PATH  + "pone.0000008.zip";
    String resourceURI = "info:doi/10.1371/journal.pone.0000008";

//    final String resourceToIngest = BASE_TEST_PATH  + "pone.0000011.zip";
//    String resourceURI = "info:doi/10.1371/journal.pone.0000011";

    try {
      getArticleOtmService().delete(resourceURI);
    } catch (NoSuchArticleIdException nsie) {
      // ignore - this just means there wasn't any stale stuff left
    }

    final URL article = getAsUrl(resourceToIngest);
    Article art = getArticleOtmService().ingest(new URLDataSource(article), false);
    assertEquals(art.getId().toString(), resourceURI);

    final FetchArticleAction fetchArticleAction = getFetchArticleAction();
    fetchArticleAction.setArticleURI(resourceURI);

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
    bodyFetchAction.setBodyUrl(testXmlTarget);
    assertEquals(Action.SUCCESS, bodyFetchAction.execute());
    final String articleBody = bodyFetchAction.getBody();
    log.debug(articleBody);
    assertTrue(articleBody.contains("href=\""));
  }

  public void testShouldInjestArticle() throws Exception {
    doIngestTest("info:doi/10.1371/journal.pone.0000008", BASE_TEST_PATH  + "pone.0000008.zip");
//    doIngestTest("info:doi/10.1371/journal.pone.0000011", BASE_TEST_PATH  + "pone.0000011.zip");
  }

  public void testListArticles() throws MalformedURLException, ServiceException, ApplicationException {
    Collection<String> articles = getFetchArticleService().getArticleIds(null, null, null);
    for (final String article : articles) {
      log.debug("article = " + article);
    }
  }

  private void doIngestTest(String resourceURI, String resourceToIngest) throws Exception {
    try {
      getArticleOtmService().delete(resourceURI);
    } catch (NoSuchArticleIdException nsie) {
      // ignore - this just means there wasn't any stale stuff left
    }

    final URL article = getAsUrl(resourceToIngest);

    Article art = getArticleOtmService().ingest(new URLDataSource(article), false);
    assertEquals(art.getId(), resourceURI);

    assertNotNull(art.getRepresentation("XML"));

    try {
      art = getArticleOtmService().ingest(new URLDataSource(article), false);
      fail("Failed to get expected duplicate-id exception");
    } catch (DuplicateArticleIdException die) {
    }

    getArticleOtmService().delete(art.getId().toString());

    try {
      getArticleOtmService().delete(art.getId().toString());
      fail("Failed to get NoSuchArticleIdException");
    } catch (NoSuchArticleIdException nsie) {
    }
  }
}
