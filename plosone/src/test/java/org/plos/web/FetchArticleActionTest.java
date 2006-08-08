/* $HeadURL::                                                                            $
* $Id$
*/
package org.plos.web;

import org.plos.BasePlosoneTestCase;
import org.plos.article.web.FetchArticleAction;
import org.topazproject.ws.article.service.DuplicateIdException;
import org.topazproject.ws.article.service.NoSuchIdException;

import javax.activation.DataHandler;
import javax.xml.rpc.ServiceException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.net.URL;

public class FetchArticleActionTest extends BasePlosoneTestCase {
  public void testShouldReturnTransformedArticle() throws Exception {
    final String resourceToIngest = "/test2.zip";
    final String resourceDOI = "10.1371/journal.pbio.0020294";

    try {
      getArticleService().delete(resourceDOI, true);
    } catch (NoSuchIdException nsie) {
      // ignore - this just means there wasn't any stale stuff left
    }

    final URL article = getClass().getResource(resourceToIngest);
    String doi = getArticleService().ingest(new DataHandler(article));
    assertEquals(doi, resourceDOI);

    final FetchArticleAction fetchArticleAction = getFetchArticleAction();
    fetchArticleAction.setArticleDOI(resourceDOI);
    assertEquals(FetchArticleAction.SUCCESS, fetchArticleAction.execute());
    final String transformedArticle = fetchArticleAction.getTransformedArticle();
    assertNotNull(transformedArticle);
  }

  public void testShouldInjestArticle() throws IOException, TransformerException, ServiceException {
    // final String resourceToIngest = "/pbio.0000001-embedded-dtd.zip";
    // final String resourceDOI = "10.1371/journal.pbio.0000001";

    final String resourceToIngest = "/test2.zip";
    final String resourceDOI = "10.1371/journal.pbio.0020294";

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
