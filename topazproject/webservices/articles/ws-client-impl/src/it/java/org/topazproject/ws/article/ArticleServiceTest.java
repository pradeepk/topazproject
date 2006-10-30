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
    // topaz format
    basicArticleTest("/pbio.0020294.zip", "10.1371/journal.pbio.0020294", "pmc.xml");
    // AP format
    basicArticleTest("/pone.0000010.zip", "10.1371/journal.pone.0000010", "pone.0000010.xml");
  }

  private void basicArticleTest(String zip, String doi, String pmc) throws Exception {
    try {
      service.delete(doi, true);
    } catch (NoSuchArticleIdException nsaie) {
      assertEquals("Mismatched id in exception, ", doi, nsaie.getId());
      // ignore - this just means there wasn't any stale stuff left
    }

    URL article = getClass().getResource(zip);
    String retDoi = service.ingest(new DataHandler(article));
    assertEquals("Wrong doi returned,", doi, retDoi);

    Throwable gotE = null;
    try {
      retDoi = service.ingest(new DataHandler(article));
    } catch (DuplicateArticleIdException daie) {
      assertEquals("Mismatched id in exception, ", doi, daie.getId());
      gotE = daie;
    }
    assertNotNull("Failed to get expected duplicate-id exception", gotE);

    /* TODO: this isn't true anymore, as ingest modifies the article.
     * Is there a way we can get the modified article?
    ZipInputStream zis = new ZipInputStream(article.openStream());
    while (!zis.getNextEntry().getName().equals(pmc))
      ;
    byte[] orig  = IOUtils.toByteArray(zis);
    byte[] saved = IOUtils.toByteArray(new URL(service.getObjectURL(retDoi, "XML")).openStream());
    assertTrue("Content mismatch: got '" + new String(saved, "UTF-8") + "'",
               Arrays.equals(orig, saved));
    */

    service.delete(retDoi, true);

    gotE = null;
    try {
      service.delete(retDoi, true);
    } catch (NoSuchArticleIdException nsaie) {
      assertEquals("Mismatched id in exception, ", retDoi, nsaie.getId());
      gotE = nsaie;
    }
    assertNotNull("Failed to get expected no-such-id exception", gotE);
    assertTrue("exception is not a subclass of NoSuchIdException: " + gotE.getClass(),
               gotE instanceof NoSuchIdException);
  }

  public void testIngestErrors() throws Exception {
    ingestErrorTest("/test.tpz.missing.file.zip",    "No entry found in zip file for doi");
    ingestErrorTest("/test.ap.missing.file.zip",     "No entry found in zip file for doi");
    ingestErrorTest("/test.tpz.unrefd.file.zip",     "Found unreferenced entry in zip file");
    ingestErrorTest("/test.ap.unrefd.file.zip",      "Found unreferenced entry in zip file");
    ingestErrorTest("/test.tpz.missing.article.zip", "Couldn't find article entry in zip file");
    ingestErrorTest("/test.ap.missing.article.zip",  "Couldn't find article entry in zip file");
    ingestErrorTest("/test.ap.invalid.file.zip",     "does not have same prefix as article");
  }

  private void ingestErrorTest(String zip, String expMsg) throws Exception {
    URL article = getClass().getResource(zip);

    Throwable gotE = null;
    String retDoi = null;
    try {
      retDoi = service.ingest(new DataHandler(article));
      service.delete(retDoi, true);     // clean up in case of accidental success
    } catch (IngestException ie) {
      gotE = ie;
    }
    assertNotNull("Failed to get expected ingest exception", gotE);
    assertTrue("Failed to get expected exception message - got '" + gotE.getMessage() + "'",
               gotE.getMessage().indexOf(expMsg) >= 0);
  }

  public void testObjectInfo() throws Exception {
    // some NoSuchObjectIdException tests
    boolean gotE = false;
    try {
      service.getObjectInfo("blah/foo");
    } catch (NoSuchObjectIdException nsoie) {
      assertEquals("Mismatched id in exception, ", "blah/foo", nsoie.getId());
      gotE = true;
    }
    assertTrue("Failed to get expected no-such-object-id exception", gotE);

    // ingest article and test getObjectInfo()
    URL article = getClass().getResource("/pbio.0020294.zip");
    String doi = service.ingest(new DataHandler(article));
    assertEquals("Wrong doi returned,", "10.1371/journal.pbio.0020294", doi);

    ObjectInfo oi = service.getObjectInfo(doi);
    assertEquals("wrong doi", doi, oi.getDoi());
    assertEquals("wrong title", "Regulation of Muscle Fiber Type and Running Endurance by PPAR ",
                 oi.getTitle());
    assertNotNull("missing description", oi.getDescription());
    assertNull("unexpected context-element", oi.getContextElement());

    RepresentationInfo[] ri = oi.getRepresentations();
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

    // test getObjectInfo on g001
    String secDoi = doi + ".g001";
    oi = service.getObjectInfo(secDoi);
    assertEquals("wrong doi", secDoi, oi.getDoi());
    assertEquals("wrong title", "Figure 1", oi.getTitle());
    assertNotNull("missing description", oi.getDescription());
    assertEquals("wrong context-element", "fig", oi.getContextElement());

    ri = oi.getRepresentations();
    assertEquals("wrong number of rep-infos", 2, ri.length);

    sort(ri);

    assertEquals("ri-name mismatch", "PNG", ri[0].getName());
    assertEquals("ri-cont-type mismatch", "image/png", ri[0].getContentType());
    assertEquals("ri-size mismatch", 52422L, ri[0].getSize());

    assertEquals("ri-name mismatch", "TIF", ri[1].getName());
    assertEquals("ri-cont-type mismatch", "image/tiff", ri[1].getContentType());
    assertEquals("ri-size mismatch", 120432L, ri[1].getSize());

    // test getObjectInfo on sv001
    secDoi = doi + ".sv001";
    oi = service.getObjectInfo(secDoi);
    assertEquals("wrong doi", secDoi, oi.getDoi());
    assertEquals("wrong title", "Video S1", oi.getTitle());
    assertNotNull("missing description", oi.getDescription());
    assertEquals("wrong context-element", "supplementary-material", oi.getContextElement());

    ri = oi.getRepresentations();
    assertEquals("wrong number of rep-infos", 1, ri.length);

    assertEquals("ri-name mismatch", "MOV", ri[0].getName());
    assertEquals("ri-cont-type mismatch", "video/quicktime", ri[0].getContentType());
    assertEquals("ri-size mismatch", 0L, ri[0].getSize());

    // clean up
    service.delete(doi, true);
  }

  public void testRepresentations() throws Exception {
    // some NoSuchObjectIdException tests
    boolean gotE = false;
    try {
      service.setRepresentation("blah/foo", "bar",
                                new DataHandler(new StringDataSource("Some random text")));
    } catch (NoSuchObjectIdException nsoie) {
      assertEquals("Mismatched id in exception, ", "blah/foo", nsoie.getId());
      gotE = true;
    }
    assertTrue("Failed to get expected no-such-object-id exception", gotE);

    // ingest article and test getObjectInfo()
    URL article = getClass().getResource("/pbio.0020294.zip");
    String doi = service.ingest(new DataHandler(article));
    assertEquals("Wrong doi returned,", "10.1371/journal.pbio.0020294", doi);

    ObjectInfo oi = service.getObjectInfo(doi);
    assertEquals("wrong doi", doi, oi.getDoi());
    assertEquals("wrong title", "Regulation of Muscle Fiber Type and Running Endurance by PPAR ",
                 oi.getTitle());
    assertNotNull("missing description", oi.getDescription());
    assertNull("unexpected context-element", oi.getContextElement());

    RepresentationInfo[] ri = oi.getRepresentations();
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

    oi = service.getObjectInfo(doi);
    ri = oi.getRepresentations();
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

    oi = service.getObjectInfo(doi);
    ri = oi.getRepresentations();
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

    oi = service.getObjectInfo(doi);
    ri = oi.getRepresentations();
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

    oi = service.getObjectInfo(doi);
    ri = oi.getRepresentations();
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

  public void testSecondaryObjects() throws Exception {
    // some NoSuchArticleIdException tests
    boolean gotE = false;
    try {
      service.listSecondaryObjects("blah/foo");
    } catch (NoSuchArticleIdException nsaie) {
      assertEquals("Mismatched id in exception, ", "blah/foo", nsaie.getId());
      gotE = true;
    }
    assertTrue("Failed to get expected no-such-object-id exception", gotE);

    // ingest article and test listRepresentations()
    URL article = getClass().getResource("/pbio.0020294.zip");
    String doi = service.ingest(new DataHandler(article));
    assertEquals("Wrong doi returned,", "10.1371/journal.pbio.0020294", doi);

    ObjectInfo[] oi = service.listSecondaryObjects(doi);
    assertEquals("wrong number of object-infos", 8, oi.length);

    assertEquals("doi mismatch", "10.1371/journal.pbio.0020294.g001", oi[0].getDoi());
    assertEquals("doi mismatch", "10.1371/journal.pbio.0020294.g002", oi[1].getDoi());
    assertEquals("doi mismatch", "10.1371/journal.pbio.0020294.g003", oi[2].getDoi());
    assertEquals("doi mismatch", "10.1371/journal.pbio.0020294.g004", oi[3].getDoi());
    assertEquals("doi mismatch", "10.1371/journal.pbio.0020294.g005", oi[4].getDoi());
    assertEquals("doi mismatch", "10.1371/journal.pbio.0020294.g006", oi[5].getDoi());
    assertEquals("doi mismatch", "10.1371/journal.pbio.0020294.sv001", oi[6].getDoi());
    assertEquals("doi mismatch", "10.1371/journal.pbio.0020294.sv002", oi[7].getDoi());

    assertEquals("label mismatch", "Figure 1", oi[0].getTitle());
    assertEquals("label mismatch", "Figure 2", oi[1].getTitle());
    assertEquals("label mismatch", "Figure 3", oi[2].getTitle());
    assertEquals("label mismatch", "Figure 4", oi[3].getTitle());
    assertEquals("label mismatch", "Figure 5", oi[4].getTitle());
    assertEquals("label mismatch", "Figure 6", oi[5].getTitle());
    assertEquals("label mismatch", "Video S1", oi[6].getTitle());
    assertEquals("label mismatch", "Video S2", oi[7].getTitle());

    assertNotNull("missing description", oi[0].getDescription());
    assertNotNull("missing description", oi[1].getDescription());
    assertNotNull("missing description", oi[2].getDescription());
    assertNotNull("missing description", oi[3].getDescription());
    assertNotNull("missing description", oi[4].getDescription());
    assertNotNull("missing description", oi[5].getDescription());
    assertNotNull("missing description", oi[6].getDescription());
    assertNotNull("missing description", oi[7].getDescription());

    assertEquals("wrong context-element", "fig", oi[0].getContextElement());
    assertEquals("wrong context-element", "fig", oi[1].getContextElement());
    assertEquals("wrong context-element", "fig", oi[2].getContextElement());
    assertEquals("wrong context-element", "fig", oi[3].getContextElement());
    assertEquals("wrong context-element", "fig", oi[4].getContextElement());
    assertEquals("wrong context-element", "fig", oi[5].getContextElement());
    assertEquals("wrong context-element", "supplementary-material", oi[6].getContextElement());
    assertEquals("wrong context-element", "supplementary-material", oi[7].getContextElement());

    assertEquals("wrong number of rep-infos", 2, oi[0].getRepresentations().length);
    assertEquals("wrong number of rep-infos", 1, oi[1].getRepresentations().length);
    assertEquals("wrong number of rep-infos", 1, oi[2].getRepresentations().length);
    assertEquals("wrong number of rep-infos", 1, oi[3].getRepresentations().length);
    assertEquals("wrong number of rep-infos", 1, oi[4].getRepresentations().length);
    assertEquals("wrong number of rep-infos", 1, oi[5].getRepresentations().length);
    assertEquals("wrong number of rep-infos", 1, oi[6].getRepresentations().length);
    assertEquals("wrong number of rep-infos", 1, oi[7].getRepresentations().length);

    sort(oi[0].getRepresentations());

    RepresentationInfo ri = oi[0].getRepresentations()[0];
    assertEquals("ri-name mismatch", "PNG", ri.getName());
    assertEquals("ri-cont-type mismatch", "image/png", ri.getContentType());
    assertEquals("ri-size mismatch", 52422L, ri.getSize());

    ri = oi[0].getRepresentations()[1];
    assertEquals("ri-name mismatch", "TIF", ri.getName());
    assertEquals("ri-cont-type mismatch", "image/tiff", ri.getContentType());
    assertEquals("ri-size mismatch", 120432L, ri.getSize());

    ri = oi[1].getRepresentations()[0];
    assertEquals("ri-name mismatch", "TIF", ri.getName());
    assertEquals("ri-cont-type mismatch", "image/tiff", ri.getContentType());
    assertEquals("ri-size mismatch", 375480L, ri.getSize());

    ri = oi[2].getRepresentations()[0];
    assertEquals("ri-name mismatch", "TIF", ri.getName());
    assertEquals("ri-cont-type mismatch", "image/tiff", ri.getContentType());
    assertEquals("ri-size mismatch", 170324L, ri.getSize());

    ri = oi[3].getRepresentations()[0];
    assertEquals("ri-name mismatch", "TIF", ri.getName());
    assertEquals("ri-cont-type mismatch", "image/tiff", ri.getContentType());
    assertEquals("ri-size mismatch", 458812L, ri.getSize());

    ri = oi[4].getRepresentations()[0];
    assertEquals("ri-name mismatch", "TIF", ri.getName());
    assertEquals("ri-cont-type mismatch", "image/tiff", ri.getContentType());
    assertEquals("ri-size mismatch", 164130L, ri.getSize());

    ri = oi[5].getRepresentations()[0];
    assertEquals("ri-name mismatch", "TIF", ri.getName());
    assertEquals("ri-cont-type mismatch", "image/tiff", ri.getContentType());
    assertEquals("ri-size mismatch", 101566L, ri.getSize());

    ri = oi[6].getRepresentations()[0];
    assertEquals("ri-name mismatch", "MOV", ri.getName());
    assertEquals("ri-cont-type mismatch", "video/quicktime", ri.getContentType());
    assertEquals("ri-size mismatch", 0L, ri.getSize());

    ri = oi[7].getRepresentations()[0];
    assertEquals("ri-name mismatch", "MOV", ri.getName());
    assertEquals("ri-cont-type mismatch", "video/quicktime", ri.getContentType());
    assertEquals("ri-size mismatch", 0L, ri.getSize());

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
