/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.ws.article;

import java.io.InputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.zip.ZipInputStream;
import javax.activation.DataHandler;
import javax.xml.rpc.ServiceException;

import org.apache.commons.io.IOUtils;
import junit.framework.TestCase;

/**
 *
 */
public class ArticleServiceTest extends TestCase {
  private Article service;

  public ArticleServiceTest(String testName) {
    super(testName);
  }

  protected void setUp() throws MalformedURLException, ServiceException, RemoteException {
    URL url =
        new URL("http://localhost:9997/ws-articles-webapp-0.1/services/ArticleServicePort");
    ArticleServiceLocator locator = new ArticleServiceLocator();
    service = locator.getArticleServicePort(url);
  }

  public void testAll() throws Exception {
    basicArticleTest();
  }

  private void basicArticleTest() throws Exception {
    try {
      service.delete("10.1371/journal.pbio.0020294", true);
    } catch (NoSuchIdException nsie) {
      // ignore - this just means there wasn't any stale stuff left
    }

    URL article = getClass().getResource("/test_article.zip");
    String doi = service.ingest(new DataHandler(article));
    assertEquals("Wrong doi returned,", doi, "10.1371/journal.pbio.0020294");

    boolean gotE = false;
    try {
      doi = service.ingest(new DataHandler(article));
    } catch (DuplicateIdException die) {
      gotE = true;
    }
    assertTrue("Failed to get expected duplicate-id exception", gotE);

    ZipInputStream zis = new ZipInputStream(article.openStream());
    while (!zis.getNextEntry().getName().equals("pmc.xml"))
      ;
    byte[] orig  = IOUtils.toByteArray(zis);
    byte[] saved = IOUtils.toByteArray(new URL(service.getObjectURL(doi, "XML")).openStream());
    assertTrue("Content mismatch: got '" + new String(saved, "UTF-8") + "'",
               Arrays.equals(orig, saved));

    service.delete(doi, true);

    gotE = false;
    try {
      service.delete(doi, true);
    } catch (NoSuchIdException nsie) {
      gotE = true;
    }
    assertTrue("Failed to get expected no-such-id exception", gotE);
  }

  private static byte[] loadURL(URL url) throws IOException {
    URLConnection con = url.openConnection();
    con.connect();
    byte[] res = new byte[con.getContentLength()];

    InputStream is = con.getInputStream();
    is.read(res);
    is.close();

    return res;
  }
}
