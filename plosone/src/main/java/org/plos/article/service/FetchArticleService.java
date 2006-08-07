/* $HeadURL::                                                                            $
 * $Id$
 */
package org.plos.article.service;

import org.topazproject.ws.article.service.NoSuchIdException;

import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.Properties;
import java.net.URL;
import java.net.MalformedURLException;
import java.rmi.RemoteException;

/**
 * Fetch article service
 */
public class FetchArticleService {
  private ArticleService articleService;
  private String xslTemplate;
  private String articleRep;

  /**
   * Get the DOI transformed as HTML.
   * @param articleDOI articleDOI
   * @param writer writer
   * @throws IOException
   * @throws TransformerException
   * @throws java.net.MalformedURLException
   * @throws java.io.FileNotFoundException
   * @throws java.rmi.RemoteException
   * @throws org.topazproject.ws.article.service.NoSuchIdException
   */
  public void getDOIAsHTML(final String articleDOI, final Writer writer) throws TransformerException, NoSuchIdException, IOException, RemoteException, MalformedURLException, FileNotFoundException {
    final String objectURL = articleService.getObjectURL(articleDOI, articleRep);

    final StreamSource streamSource = new StreamSource(new InputStreamReader(new URL(objectURL).openStream()));
    final Transformer transformer = getXSLTransformer(xslTemplate);

    transformXML(transformer, streamSource, writer);
  }

  /**
   * Set articleService
   * @param articleService articleService
   */
  public void setArticleService(final ArticleService articleService) {
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
  private Transformer getXSLTransformer(final String xslStyleSheet) throws TransformerException {
    // 1. Instantiate a TransformerFactory.
    final TransformerFactory tFactory = TransformerFactory.newInstance();

    // 2. Use the TransformerFactory to process the stylesheet Source and
    //    generate a Transformer.
    return tFactory.newTransformer(new StreamSource(xslStyleSheet));
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
    final String key = "javax.xml.transform.TransformerFactory";
    final String value = "org.apache.xalan.xsltc.trax.TransformerFactoryImpl";
    final Properties props = System.getProperties();
    props.put(key, value);
    System.setProperties(props);

    // Instantiate the TransformerFactory, and use it with a StreamSource
    // XSL stylesheet to create a translet as a Templates object.      y
    final TransformerFactory tFactory = TransformerFactory.newInstance();
    final Templates translet = tFactory.newTemplates(new StreamSource(xslTemplate));

    // For each thread, instantiate a new Transformer, and perform the
    // transformations on that thread from a StreamSource to a StreamResult;
    return translet.newTransformer();
  }

  /** Set the XSL Template to be used for transformation
   * @param xslTemplate xslTemplate
   */
  public void setXslTemplate(final String xslTemplate) {
    this.xslTemplate = xslTemplate;
  }

  /**
   * Set the representation of the article that we want to work with
   * @param articleRep articleRep
   */
  public void setArticleRep(final String articleRep) {
    this.articleRep = articleRep;
  }
}
