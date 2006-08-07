/* $HeadURL::                                                                            $
* $Id$
*/
package org.plos.web;

import junit.framework.TestCase;
import org.topazproject.ws.article.service.Article;
import org.topazproject.ws.article.service.ArticleServiceLocator;
import org.topazproject.ws.article.service.DuplicateIdException;
import org.topazproject.ws.article.service.NoSuchIdException;
import org.plos.article.web.FetchArticleAction;
import org.plos.article.web.FetchArticleService;

import javax.activation.DataHandler;
import javax.xml.rpc.ServiceException;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.Properties;

public class FetchArticleActionTest extends TestCase {
  private Article service;
  private String serviceUrl = "http://localhost:8080/ws-articles-webapp-0.1/services/ArticleServicePort";

  private final String XML_SOURCE = "pbio.0000001-embedded-dtd.xml";
  private final String XSL_SOURCE = "viewnlm-v2.xsl";
  private final String CSS = "ViewNLM.css";
  private final String OUTPUT_FILENAME = "foo.html";

  public void testShouldReturnTransformedArticle() throws Exception, TransformerException, ServiceException {
//      final String resourceToIngest = "/test2.zip";
    final String resourceDOI = "10.1371/journal.pbio.0020294";

    FetchArticleAction fetchArticleAction = new FetchArticleAction();
    fetchArticleAction.setFetchArticleService(getFetchArticleService());
    fetchArticleAction.setArticleDOI(resourceDOI);
    assertEquals(FetchArticleAction.SUCCESS, fetchArticleAction.execute());
  }

  private FetchArticleService getFetchArticleService() throws MalformedURLException, ServiceException {
    final FetchArticleService fetchArticleService = new FetchArticleService();
    fetchArticleService.setArticleService(getArticleService());
    return fetchArticleService;
  }

  private Article getArticleService() throws MalformedURLException, ServiceException {
    URL url = new URL(serviceUrl);
    ArticleServiceLocator locator = new ArticleServiceLocator();
    return locator.getArticleServicePort(url);
  }

  public void testShouldInjestArticle() throws IOException, TransformerException {
    {
      final String resourceToIngest = "/pbio.0000001-embedded-dtd.zip";
      final String resourceDOI = "10.1371/journal.pbio.0000001";

//      final String resourceToIngest = "/test2.zip";
//      final String resourceDOI = "10.1371/journal.pbio.0020294";

      try {
        service.delete(resourceDOI, true);
      } catch (NoSuchIdException nsie) {
        // ignore - this just means there wasn't any stale stuff left
      }

      final URL article = getClass().getResource(resourceToIngest);
      String doi = service.ingest(new DataHandler(article));
      assertEquals(doi, resourceDOI);

      String objectURL = service.getObjectURL(doi, "XML");

      final StreamSource streamSource = new StreamSource(new InputStreamReader(new URL(objectURL).openStream()));
      final Transformer transformer = getXSLTransformer(XSL_SOURCE);

      TimeIt.run(new Command() {
        public void execute() {
          try {
            transformXML(transformer, streamSource, OUTPUT_FILENAME);
          } catch (Exception e) {
          }
        }
      });

      service.delete(doi, true);
    }

  }

  public void testXSLTransformation() throws TransformerException, FileNotFoundException {
    final Transformer transformer = getXSLTransformer(XSL_SOURCE);
    logTime();

    transformXML(transformer, new StreamSource(XML_SOURCE), OUTPUT_FILENAME);
    logTime();
  }

  private void transformXML(final Transformer transformer, final StreamSource xmlSource, final String outputFileName) throws TransformerException, FileNotFoundException {
    // 3. Use the Transformer to transform an XML_SOURCE Source and send the
    //    output to a Result object.
    transformer.transform(
            xmlSource,
            new StreamResult(new FileOutputStream(outputFileName)));
  }

  private Transformer getXSLTransformer(final String xslStyleSheet) throws TransformerConfigurationException {
    // 1. Instantiate a TransformerFactory.
    final TransformerFactory tFactory = TransformerFactory.newInstance();

    // 2. Use the TransformerFactory to process the stylesheet Source and
    //    generate a Transformer.
    return tFactory.newTransformer(new StreamSource(xslStyleSheet));
  }

  private void logTime() {
    System.out.println(System.currentTimeMillis());
  }

  public void testTranslet() throws TransformerException, FileNotFoundException {

// Set the TransformerFactory system property.
// Note: For more flexibility, load properties from a properties file.
    String key = "javax.xml.transform.TransformerFactory";
    String value = "org.apache.xalan.xsltc.trax.TransformerFactoryImpl";
    Properties props = System.getProperties();
    props.put(key, value);
    System.setProperties(props);

// Instantiate the TransformerFactory, and use it with a StreamSource
// XSL stylesheet to create a translet as a Templates object.      y
    TransformerFactory tFactory = TransformerFactory.newInstance();
    Templates translet = tFactory.newTemplates(new StreamSource(XSL_SOURCE));

// For each thread, instantiate a new Transformer, and perform the
// transformations on that thread from a StreamSource to a StreamResult;
    Transformer transformer = translet.newTransformer();
    final StreamSource streamSource = new StreamSource(XML_SOURCE);
    final StreamResult streamResult = new StreamResult(new FileOutputStream(OUTPUT_FILENAME));
    logTime();
    transformer.transform(streamSource,
            streamResult);
    logTime();
  }

  protected void setUp() throws MalformedURLException, ServiceException, RemoteException {
    service = getArticleService();
  }

  public void testBasicArticle() throws RemoteException, IOException {
    final String resourceToIngest = "/test_article.zip";
    final String resourceDOI = "10.1371/journal.pbio.0020294";

    try {
      service.delete(resourceDOI, true);
    } catch (NoSuchIdException nsie) {
      // ignore - this just means there wasn't any stale stuff left
    }

    final URL article = getClass().getResource(resourceToIngest);
    String doi = service.ingest(new DataHandler(article));
    assertEquals(doi, resourceDOI);

    try {
      doi = service.ingest(new DataHandler(article));
      fail("Failed to get expected duplicate-id exception");
    } catch (DuplicateIdException die) {
    }

    service.delete(doi, true);

    try {
      service.delete(doi, true);
      fail("Failed to get NoSuchIdException");
    } catch (NoSuchIdException nsie) {
    }
  }

}
