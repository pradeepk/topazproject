/* $HeadURL::                                                                            $
 * $Id$
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
package org.topazproject.ambra.article.action;

import com.opensymphony.xwork2.Action;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.ambra.BaseAmbraTestCase;
import org.topazproject.ambra.article.service.ArticleOtmService;
import org.topazproject.ambra.article.service.Ingester;
import org.topazproject.ambra.article.service.NoSuchArticleIdException;
import org.topazproject.ambra.article.service.SecondaryObject;
import org.topazproject.ambra.models.ObjectInfo;
import org.topazproject.ambra.models.Representation;

import java.net.URL;
import java.util.Set;

import javax.activation.URLDataSource;

public class FetchObjectTest extends BaseAmbraTestCase {
  private static final Log log = LogFactory.getLog(FetchObjectTest.class);
  private String BASE_TEST_PATH = "src/test/resources/";

  public void testArticleRepresentations() throws Exception {
    final ArticleOtmService service = getArticleOtmService();

    final String resourceToIngest = BASE_TEST_PATH  + "pone.0000008.zip";
    final String uri = "info:doi/10.1371/journal.pone.0000008";

    deleteAndIngestArticle(resourceToIngest, uri);

    final ObjectInfo oi = service.getObjectInfo(uri);
    final Set<Representation> ri = oi.getRepresentations();
    assertEquals(2, ri.size());

    final FetchObjectAction fetchObjectAction = getFetchObjectAction();
    fetchObjectAction.setUri(uri);
    fetchObjectAction.setRepresentation("XML");
    assertEquals(Action.SUCCESS, fetchObjectAction.execute());

    final SecondaryObject[] so = service.listSecondaryObjects(uri);
    assertEquals(8, so.length);

    final Set<Representation> riForG001 = service.getObjectInfo(uri + ".g001").getRepresentations();
    assertEquals(1, riForG001.size());
  }

  public void testSecondaryDocInfo() throws Exception {
    final String resourceToIngest = BASE_TEST_PATH  + "pone.0000008.zip";
    final String uri = "info:doi/10.1371/journal.pone.0000008";

    deleteAndIngestArticle(resourceToIngest, uri);

    final SlideshowAction slideshowAction = getSecondaryObjectAction();
    slideshowAction.setUri(uri);
    assertEquals(Action.SUCCESS, slideshowAction.execute());

    final SecondaryObject[] oi = slideshowAction.getSecondaryObjects();
    assertEquals(8, oi.length);

    for (final SecondaryObject objectInfo : oi) {
      assertNotNull(objectInfo.getUri());
    }
  }

  private void deleteAndIngestArticle(final String resourceToIngest, final String uri) throws Exception {
    final URL article = getAsUrl(resourceToIngest);
    final ArticleOtmService service = getArticleOtmService();

    try {
      service.delete(uri);
    } catch(NoSuchArticleIdException ex) {
      //means that this article is not ingested yet, so delete would fail
    }

    final String ingestedUri = service.ingest(new Ingester(new URLDataSource(article)), false).getId().toString();
    assertEquals(uri, ingestedUri);
  }
}
