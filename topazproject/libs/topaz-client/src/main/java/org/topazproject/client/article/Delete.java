/* $HeadURL::                                                                                     $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.client.article;

import org.topazproject.ws.article.Article;
import org.topazproject.ws.article.ArticleClientFactory;

public class Delete {
  public static void main(String[] args) throws Exception {
    String serviceUrl = args[0];
    String doi = args[1];

    Article service = ArticleClientFactory.create(serviceUrl);
    service.delete(doi);
  }
}
