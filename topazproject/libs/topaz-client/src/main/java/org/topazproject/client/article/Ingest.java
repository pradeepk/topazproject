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
import javax.activation.DataHandler;
import java.io.File;

public class Ingest {
  public static void main(String[] args) throws Exception {
    String serviceUrl = args[0];
    String zipFileName = args[1];

    Article service = ArticleClientFactory.create(serviceUrl);
    service.ingest(new DataHandler(new File(zipFileName).toURL()));
    
    System.out.println("Importing " + zipFileName + " into " + serviceUrl);
  }
}
