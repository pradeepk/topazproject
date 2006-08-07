/* $HeadURL::                                                                            $
 * $Id$
 */
package org.plos.article.web;

import org.topazproject.ws.article.service.Article;

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
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.util.Properties;

/**
 * Fetch article service
 */
public class FetchArticleService {
  private Article articleService;

  private static final String ARTICLE_REP = "XML";
  private static final int TRANSFORMED_XML_FILE_SIZE = 1000000;

  private final String XSL_TEMPLATE = "viewnlm-v2.xsl";
  private static final String XML_SOURCE = "no needed";
  private static final String OUTPUT_FILENAME = "not needed";

  /**
   * Get the DOI transformed as HTML.
   * @param articleDOI articleDOI
   * @throws IOException
   * @throws TransformerException
   */
  public void getDOIAsHTML(final String articleDOI) throws IOException, TransformerException {
    final Writer writer = new StringWriter(TRANSFORMED_XML_FILE_SIZE);
    final String objectURL = articleService.getObjectURL(articleDOI, ARTICLE_REP);

    final StreamSource streamSource = new StreamSource(new InputStreamReader(new URL(objectURL).openStream()));
    final Transformer transformer = getXSLTransformer(XSL_TEMPLATE);

    transformXML(transformer, streamSource, writer);
  }

  /**
   * Set articleService
   * @param articleService articleService
   */
  public void setArticleService(final Article articleService) {
    this.articleService = articleService;
  }

  private void transformXML(final Transformer transformer, final StreamSource xmlSource, final Writer writer) throws TransformerException, FileNotFoundException {
    // Use the Transformer to transform an XML_SOURCE Source and send the output to a Result object.
    transformer.transform(
            xmlSource,
            new StreamResult(writer));
  }

  /**
   * Get the XSL transformer
   * @param xslStyleSheet xslStyleSheet
   * @return Transformer
   * @throws TransformerConfigurationException
   */
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

  /**
   * Get a translet - a compiled stylesheet.
   * @return translet
   * @throws TransformerException
   * @throws FileNotFoundException
   */
  public Transformer getTranslet() throws TransformerException, FileNotFoundException {
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
    Templates translet = tFactory.newTemplates(new StreamSource(XSL_TEMPLATE));

    // For each thread, instantiate a new Transformer, and perform the
    // transformations on that thread from a StreamSource to a StreamResult;
    return translet.newTransformer();
  }
}
