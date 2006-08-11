/* $HeadURL::                                                                            $
* $Id$
*/
package org.plos.web;

import org.plos.BasePlosoneTestCase;
import org.plos.article.web.FetchArticleAction;
import org.topazproject.ws.article.service.DuplicateIdException;
import org.topazproject.ws.article.service.NoSuchIdException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.activation.DataHandler;
import javax.xml.rpc.ServiceException;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

public class FetchArticleActionTest extends BasePlosoneTestCase {
  public static final Log log = LogFactory.getLog(FetchArticleActionTest.class);

  public void testShouldReturnTransformedArticle() throws Exception {
//    final String resourceToIngest = "/test_article.zip";
//    final String resourceDOI = "10.1371/journal.pbio.0020294";

    final String resourceToIngest = "pbio.0020042.zip";
    final String resourceDOI = "10.1371/journal.pbio.0020042";

    try {
      getArticleService().delete(resourceDOI, true);
    } catch (NoSuchIdException nsie) {
      // ignore - this just means there wasn't any stale stuff left
    }

    URL article = getClass().getResource(resourceToIngest);
    if (null == article) {
      article = new File(resourceToIngest).toURL();
    }
    String doi = getArticleService().ingest(new DataHandler(article));
    assertEquals(doi, resourceDOI);

    final FetchArticleAction fetchArticleAction = getFetchArticleAction();
    fetchArticleAction.setArticleDOI(resourceDOI);

    String transformedArticle = "";
    for (int i = 0; i < 10; i++) {
      long t1 = System.currentTimeMillis();
      assertEquals(FetchArticleAction.SUCCESS, fetchArticleAction.execute());
      transformedArticle = fetchArticleAction.getTransformedArticle();
      log.info("Transformation time in secs:" + (System.currentTimeMillis() - t1)/1000.0);
    }
    assertNotNull(transformedArticle);
  }

  public void testShouldInjestArticle() throws IOException, TransformerException, ServiceException {
    // final String resourceToIngest = "/pbio.0000001-embedded-dtd.zip";
    // final String resourceDOI = "10.1371/journal.pbio.0000001";

    doIngestTest("10.1371/journal.pbio.0020294", "/test_article.zip");

    doIngestTest("10.1371/journal.pbio.0020042", "/pbio.0020042.zip");
//    doIngestTest("10.1371/journal.pbio.0020294", "/pbio.0020294.zip");
//    doIngestTest("10.1371/journal.pbio.0020317", "/pbio.0020317.zip");
//    doIngestTest("10.1371/journal.pbio.0020382", "/pbio.0020382.zip");
  }

  private void doIngestTest(String resourceDOI, String resourceToIngest) throws RemoteException, MalformedURLException, ServiceException {
    try {
      getArticleService().delete(resourceDOI, true);
    } catch (NoSuchIdException nsie) {
      // ignore - this just means there wasn't any stale stuff left
    }

    final URL article = getClass().getResource(resourceToIngest);
    String doi = getArticleService().ingest(new DataHandler(article));
    assertEquals(doi, resourceDOI);

    assertNotNull(getArticleService().getObjectURL(doi, "XML"));

    try {
      doi = getArticleService().ingest(new DataHandler(article));
      fail("Failed to get expected duplicate-id exception");
    } catch (DuplicateIdException die) {
    }

    getArticleService().delete(doi, true);

    try {
      getArticleService().delete(doi, true);
      fail("Failed to get NoSuchIdException");
    } catch (NoSuchIdException nsie) {
    }
  }

}
