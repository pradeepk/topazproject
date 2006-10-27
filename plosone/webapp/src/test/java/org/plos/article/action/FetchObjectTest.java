/* $HeadURL::                                                                            $
 * $Id$
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
import org.plos.article.service.ArticleWebService;
import org.plos.article.service.SecondaryObject;
import org.topazproject.common.NoSuchIdException;
import org.topazproject.ws.article.RepresentationInfo;

import java.net.URL;

public class FetchObjectTest extends BasePlosoneTestCase {
  private static final Log log = LogFactory.getLog(FetchObjectTest.class);
  private String BASE_TEST_PATH = "webapp/src/test/resources/";

  public void testArticleRepresentations() throws Exception {
    final ArticleWebService service = getArticleWebService();

    final String resourceToIngest = BASE_TEST_PATH  + "pone.0000008.zip";
    final String doi = "10.1371/journal.pone.0000008";

    deleteAndIngestArticle(resourceToIngest, doi);

    final RepresentationInfo[] ri = service.listRepresentations(doi);
    assertEquals(2, ri.length);

    final FetchObjectAction fetchObjectAction = getFetchObjectAction();
    fetchObjectAction.setDoi(doi);
    fetchObjectAction.setRepresentation("XML");
    assertEquals(Action.SUCCESS, fetchObjectAction.execute());

    final SecondaryObject[] oi = service.listSecondaryObjects(doi);
    assertEquals(8, oi.length);

    final RepresentationInfo[] riForG001 = service.listRepresentations(doi + ".g001");
    assertEquals(1, riForG001.length);
  }

  public void testSecondaryDocInfo() throws Exception {
    final String resourceToIngest = BASE_TEST_PATH  + "pone.0000008.zip";
    final String doi = "10.1371/journal.pone.0000008";

    deleteAndIngestArticle(resourceToIngest, doi);

    final SecondaryObjectAction secondaryObjectAction = getSecondaryObjectAction();
    secondaryObjectAction.setDoi(doi);
    assertEquals(Action.SUCCESS, secondaryObjectAction.execute());

    final SecondaryObject[] oi = secondaryObjectAction.getSecondaryObjects();
    assertEquals(8, oi.length);

    for (final SecondaryObject objectInfo : oi) {
      assertNotNull(objectInfo.getDoi());
    }
  }

  private void deleteAndIngestArticle(final String resourceToIngest, final String doi) throws Exception {
    final URL article = getAsUrl(resourceToIngest);
    final ArticleWebService service = getArticleWebService();

    try {
      service.delete(doi, true);
    } catch(NoSuchIdException ex) {
      //means that this article is not ingested yet, so delete would fail
    }

    final String ingestedDoi = service.ingest(article);
    assertEquals(doi, ingestedDoi);
  }
}
