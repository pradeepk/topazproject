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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.zip.ZipInputStream;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.xml.rpc.ServiceException;

import org.apache.commons.io.IOUtils;
import junit.framework.TestCase;

import org.topazproject.common.NoSuchIdException;

/**
 *
 */
public class ArticleServiceTest extends TestCase {
  private Article service;

  public ArticleServiceTest(String testName) {
    super(testName);
  }

  protected void setUp() throws MalformedURLException, ServiceException, RemoteException {
    String uri = "http://localhost:9997/ws-articles-webapp-0.5-SNAPSHOT/services/ArticleServicePort";
    service = ArticleClientFactory.create(uri);
  }

  public void testBasicArticle() throws Exception {
    try {
      service.delete("10.1371/journal.pbio.0020294", true);
    } catch (NoSuchArticleIdException nsaie) {
      assertEquals("Mismatched id in exception, ", "10.1371/journal.pbio.0020294", nsaie.getId());
      // ignore - this just means there wasn't any stale stuff left
    }

    URL article = getClass().getResource("/test_article.zip");
    String doi = service.ingest(new DataHandler(article));
    assertEquals("Wrong doi returned,", "10.1371/journal.pbio.0020294", doi);

    boolean gotE = false;
    try {
      doi = service.ingest(new DataHandler(article));
    } catch (DuplicateArticleIdException daie) {
      assertEquals("Mismatched id in exception, ", doi, daie.getId());
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
    } catch (NoSuchArticleIdException nsaie) {
      assertEquals("Mismatched id in exception, ", doi, nsaie.getId());
      gotE = true;
    }
    assertTrue("Failed to get expected no-such-id exception", gotE);

    // Try again, but this time test NoSuchIdException (from org.topazproject.common)
    gotE = false;
    try {
      service.delete(doi, true);
    } catch (NoSuchIdException nsie) {
      assertEquals("Mismatched id in exception, ", doi, nsie.getId());
      gotE = true;
    }
    assertTrue("Failed to get expected no-such-id exception", gotE);
  }

  public void testRepresentations() throws Exception {
    // some NoSuchObjectIdException tests
    boolean gotE = false;
    try {
      service.listRepresentations("blah/foo");
    } catch (NoSuchObjectIdException nsoie) {
      assertEquals("Mismatched id in exception, ", "blah/foo", nsoie.getId());
      gotE = true;
    }
    assertTrue("Failed to get expected no-such-object-id exception", gotE);

    gotE = false;
    try {
      service.setRepresentation("blah/foo", "bar",
                                new DataHandler(new StringDataSource("Some random text")));
    } catch (NoSuchObjectIdException nsoie) {
      assertEquals("Mismatched id in exception, ", "blah/foo", nsoie.getId());
      gotE = true;
    }
    assertTrue("Failed to get expected no-such-object-id exception", gotE);

    // ingest article and test listRepresentations()
    URL article = getClass().getResource("/test_article.zip");
    String doi = service.ingest(new DataHandler(article));
    assertEquals("Wrong doi returned,", "10.1371/journal.pbio.0020294", doi);

    RepresentationInfo[] ri = service.listRepresentations(doi);
    assertEquals("wrong number of rep-infos", 3, ri.length);

    sort(ri);

    assertEquals("ri-name mismatch", "HTML", ri[0].getName());
    assertEquals("ri-cont-type mismatch", "text/html", ri[0].getContentType());
    assertEquals("ri-size mismatch", 51284L, ri[0].getSize());

    assertEquals("ri-name mismatch", "PDF", ri[1].getName());
    assertEquals("ri-cont-type mismatch", "application/pdf", ri[1].getContentType());
    assertEquals("ri-size mismatch", 81173L, ri[1].getSize());

    assertEquals("ri-name mismatch", "XML", ri[2].getName());
    assertEquals("ri-cont-type mismatch", "text/xml", ri[2].getContentType());
    assertEquals("ri-size mismatch", 65680L, ri[2].getSize());

    // create a new Representation
    service.setRepresentation(doi, "TXT",
                              new DataHandler(new StringDataSource("The plain text")));

    ri = service.listRepresentations(doi);
    assertEquals("wrong number of rep-infos", 4, ri.length);

    sort(ri);

    assertEquals("ri-name mismatch", "HTML", ri[0].getName());
    assertEquals("ri-cont-type mismatch", "text/html", ri[0].getContentType());
    assertEquals("ri-size mismatch", 51284L, ri[0].getSize());

    assertEquals("ri-name mismatch", "PDF", ri[1].getName());
    assertEquals("ri-cont-type mismatch", "application/pdf", ri[1].getContentType());
    assertEquals("ri-size mismatch", 81173L, ri[1].getSize());

    assertEquals("ri-name mismatch", "TXT", ri[2].getName());
    assertEquals("ri-cont-type mismatch", "text/plain", ri[2].getContentType());
    assertEquals("ri-size mismatch", 14L, ri[2].getSize());

    assertEquals("ri-name mismatch", "XML", ri[3].getName());
    assertEquals("ri-cont-type mismatch", "text/xml", ri[3].getContentType());
    assertEquals("ri-size mismatch", 65680L, ri[3].getSize());

    // update a Representation
    service.setRepresentation(doi, "TXT",
                          new DataHandler(new StringDataSource("The corrected text", "text/foo")));

    ri = service.listRepresentations(doi);
    assertEquals("wrong number of rep-infos", 4, ri.length);

    sort(ri);

    assertEquals("ri-name mismatch", "HTML", ri[0].getName());
    assertEquals("ri-cont-type mismatch", "text/html", ri[0].getContentType());
    assertEquals("ri-size mismatch", 51284L, ri[0].getSize());

    assertEquals("ri-name mismatch", "PDF", ri[1].getName());
    assertEquals("ri-cont-type mismatch", "application/pdf", ri[1].getContentType());
    assertEquals("ri-size mismatch", 81173L, ri[1].getSize());

    assertEquals("ri-name mismatch", "TXT", ri[2].getName());
    assertEquals("ri-cont-type mismatch", "text/foo", ri[2].getContentType());
    assertEquals("ri-size mismatch", 18L, ri[2].getSize());

    assertEquals("ri-name mismatch", "XML", ri[3].getName());
    assertEquals("ri-cont-type mismatch", "text/xml", ri[3].getContentType());
    assertEquals("ri-size mismatch", 65680L, ri[3].getSize());

    // remove a Representation
    service.setRepresentation(doi, "TXT", null);

    ri = service.listRepresentations(doi);
    assertEquals("wrong number of rep-infos", 3, ri.length);

    sort(ri);

    assertEquals("ri-name mismatch", "HTML", ri[0].getName());
    assertEquals("ri-cont-type mismatch", "text/html", ri[0].getContentType());
    assertEquals("ri-size mismatch", 51284L, ri[0].getSize());

    assertEquals("ri-name mismatch", "PDF", ri[1].getName());
    assertEquals("ri-cont-type mismatch", "application/pdf", ri[1].getContentType());
    assertEquals("ri-size mismatch", 81173L, ri[1].getSize());

    assertEquals("ri-name mismatch", "XML", ri[2].getName());
    assertEquals("ri-cont-type mismatch", "text/xml", ri[2].getContentType());
    assertEquals("ri-size mismatch", 65680L, ri[2].getSize());

    // remove a non-existent Representation
    service.setRepresentation(doi, "TXT", null);

    ri = service.listRepresentations(doi);
    assertEquals("wrong number of rep-infos", 3, ri.length);

    sort(ri);

    assertEquals("ri-name mismatch", "HTML", ri[0].getName());
    assertEquals("ri-cont-type mismatch", "text/html", ri[0].getContentType());
    assertEquals("ri-size mismatch", 51284L, ri[0].getSize());

    assertEquals("ri-name mismatch", "PDF", ri[1].getName());
    assertEquals("ri-cont-type mismatch", "application/pdf", ri[1].getContentType());
    assertEquals("ri-size mismatch", 81173L, ri[1].getSize());

    assertEquals("ri-name mismatch", "XML", ri[2].getName());
    assertEquals("ri-cont-type mismatch", "text/xml", ri[2].getContentType());
    assertEquals("ri-size mismatch", 65680L, ri[2].getSize());

    // clean up
    service.delete(doi, true);
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

  private static void sort(RepresentationInfo[] ri) {
    Arrays.sort(ri, new Comparator() {
      public int compare(Object o1, Object o2) {
        return ((RepresentationInfo) o1).getName().compareTo(((RepresentationInfo) o2).getName());
      }
    });
  }

  private static class StringDataSource implements DataSource {
    private final String src;
    private final String ct;

    public StringDataSource(String content) {
      this(content, "text/plain");
    }

    public StringDataSource(String content, String contType) {
      src = content;
      ct  = contType;
    }

    public InputStream getInputStream() throws IOException {
      return new ByteArrayInputStream(src.getBytes("UTF-8"));
    }

    public OutputStream getOutputStream() throws IOException {
      throw new IOException("Not supported");
    }

    public String getContentType() {
      return ct;
    }

    public String getName() {
      return "string";
    }
  }
}
