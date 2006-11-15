/* $HeadURL::                                                                            $
 * $Id$
 */
package org.plos.article.service;

import com.sun.org.apache.xpath.internal.XPathAPI;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.ApplicationException;
import org.plos.annotation.service.AnnotationWebService;
import org.plos.annotation.service.Annotator;
import org.plos.util.FileUtils;
import org.plos.util.TextUtils;
import org.topazproject.common.NoSuchIdException;
import org.topazproject.ws.annotation.AnnotationInfo;
import org.topazproject.xml.transform.cache.CachedSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.activation.DataHandler;
import javax.activation.URLDataSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * Fetch article service
 */
public class FetchArticleService {
  private ArticleWebService articleService;
  private File xslTemplate;
  private String articleRep;
  private Templates translet;
  private boolean useTranslet = true;
  private Map<String, String> xmlFactoryProperty;
  private String encodingCharset;

  private static final Log log = LogFactory.getLog(FetchArticleService.class);
  private AnnotationWebService annotationWebService;
  private DocumentBuilderFactory factory;

  public void init() {
    // Set the TransformerFactory system property.
    for (Map.Entry<String, String> entry : xmlFactoryProperty.entrySet()) {
      System.setProperty(entry.getKey(), entry.getValue());
    }

    // Create a document builder factory and set the defaults
    factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true);
    factory.setValidating(false);
  }

  /**
   * Get the URI transformed as HTML.
   * @param articleURI articleURI
   * @param writer writer
   * @throws org.plos.ApplicationException ApplicationException
   * @throws java.rmi.RemoteException RemoteException
   * @throws org.topazproject.common.NoSuchIdException NoSuchIdException
   */
  public void getURIAsHTML(final String articleURI, final Writer writer) throws ApplicationException, RemoteException, NoSuchIdException {
    try {
      final Transformer transformer = getTransformer();

      final Source domSource = getAnnotatedContentAsDOMSource(articleURI);
      
      transformer.transform(domSource,
                            new StreamResult(writer));
    } catch (Exception e) {
      log.error("Transformation of article failed", e);
      throw new ApplicationException("Transformation of article failed", e);
    }
  }

  private Transformer getTransformer() throws FileNotFoundException, TransformerException {
    if (useTranslet) {
      return getTranslet();
    }
    return getXSLTransformer();
  }

  /**
   * Set articleService
   * @param articleService articleService
   */
  public void setArticleService(final ArticleWebService articleService) {
    this.articleService = articleService;
  }

  private TransformerFactory tFactory;
  private StreamSource source;

  /**
   * Get an XSL transformer.
   * @return Transformer
   * @throws TransformerException TransformerException
   */
  private Transformer getXSLTransformer() throws TransformerException {
    if (null == tFactory || null == source) {
      // 1. Instantiate a TransformerFactory.
      tFactory = TransformerFactory.newInstance();
      source = new StreamSource(xslTemplate);
    }

    // 2. Use the TransformerFactory to process the stylesheet Source and generate a Transformer.
    return tFactory.newTransformer(source);
  }

  /**
   * Get a translet - a compiled stylesheet.
   * @return translet
   * @throws TransformerException TransformerException
   * @throws FileNotFoundException FileNotFoundException
   */
  public Transformer getTranslet() throws TransformerException, FileNotFoundException {
    if (null == translet) {
      // Instantiate the TransformerFactory, and use it with a StreamSource
      // XSL stylesheet to create a translet as a Templates object.
      final TransformerFactory tFactory = TransformerFactory.newInstance();
      translet = tFactory.newTemplates(new StreamSource(xslTemplate));
    }

    // For each thread, instantiate a new Transformer, and perform the
    // transformations on that thread from a StreamSource to a StreamResult;
    return translet.newTransformer();
  }

  /**
   * Sets the xmlFactoryProperty
   * 
   * @param xmlFactoryProperty Map of xmlFactory Properties
   */
  public void setXmlFactoryProperty(final Map<String, String> xmlFactoryProperty) {
    this.xmlFactoryProperty = xmlFactoryProperty;
  }

  /** Set the XSL Template to be used for transformation
   * @param xslTemplate xslTemplate
   * @throws java.net.URISyntaxException URISyntaxException
   */
  public void setXslTemplate(final String xslTemplate) throws URISyntaxException {
    File file = getAsFile(xslTemplate);
    if (!file.exists()) {
      file = new File(xslTemplate);
    }
    log.debug("XSL template location = " + file.getAbsolutePath());
    this.xslTemplate = file;
  }

  /**
   * @param filenameOrURL filenameOrURL
   * @throws URISyntaxException URISyntaxException
   * @return the local or remote file or url as a java.io.File
   */
  public File getAsFile(final String filenameOrURL) throws URISyntaxException {
    final URL resource = getClass().getResource(filenameOrURL);
    if (null == resource) {
      //access it as a local file resource
      return new File(FileUtils.getFileName(filenameOrURL));
    } else {
      return new File(resource.toURI());
    }
  }

  /**
   * Set the representation of the article that we want to work with
   * @param articleRep articleRep
   */
  public void setArticleRep(final String articleRep) {
    this.articleRep = articleRep;
  }

  /**
   * Whether or not use the translet
   * @param useTranslet true if useTranslet, false otherwise
   */
  public void setUseTranslet(final boolean useTranslet) {
    this.useTranslet = useTranslet;
  }

  /**
   * Return the annotated content as a DOMSource for a given articleUri
   * @param articleURI articleURI
   * @return an instance of DOMSource
   * @throws ParserConfigurationException ParserConfigurationException
   * @throws SAXException SAXException
   * @throws IOException IOException
   * @throws URISyntaxException URISyntaxException
   * @throws org.plos.ApplicationException ApplicationException
   * @throws org.topazproject.common.NoSuchIdException NoSuchIdException
   */
  public Source getAnnotatedContentAsDOMSource(final String articleURI) throws ParserConfigurationException, SAXException, IOException, URISyntaxException, ApplicationException, NoSuchIdException {
    //final DocumentBuilder builder = createDocBuilder();

   // final Document doc = builder.parse(getAnnotatedContentAsInputStream(articleURI));

    // Prepare the DOM source
    return new DOMSource(getAnnotatedContentAsDocument(articleURI));
  }

  /**
   * Return the annotated content as a String
   * @param articleURI articleURI
   * @return an the annotated content as a String
   * @throws ParserConfigurationException ParserConfigurationException
   * @throws SAXException SAXException
   * @throws IOException IOException
   * @throws URISyntaxException URISyntaxException
   * @throws org.plos.ApplicationException ApplicationException
   * @throws org.topazproject.common.NoSuchIdException NoSuchIdException
   * @throws javax.xml.transform.TransformerException TransformerException
   */
  public String getAnnotatedContent(final String articleURI) throws ParserConfigurationException, 
                                    SAXException, IOException, URISyntaxException, 
                                    ApplicationException, NoSuchIdException,TransformerException{
    return TextUtils.getAsXMLString(getAnnotatedContentAsDocument(articleURI));
  }

  /**
   * Get the xmlFileURL as a DOMSource.
   * @param xmlFileURL xmlFileURL
   * @return an instance of DOMSource
   * @throws ParserConfigurationException ParserConfigurationException
   * @throws SAXException SAXException
   * @throws IOException IOException
   * @throws URISyntaxException URISyntaxException
   * @throws org.plos.ApplicationException ApplicationException
   * @throws org.topazproject.common.NoSuchIdException NoSuchIdException
   */
  public Source getDOMSource(final String xmlFileURL) throws ParserConfigurationException, SAXException, IOException, URISyntaxException, ApplicationException, NoSuchIdException {
    final DocumentBuilder builder = createDocBuilder();

    Document doc;
    try {
      doc = builder.parse(getAsFile(xmlFileURL));
    } catch (Exception e) {
      doc = builder.parse(xmlFileURL);
    }

    // Prepare the DOM source
    return new DOMSource(doc);
  }

  private DocumentBuilder createDocBuilder() throws ParserConfigurationException {
    // Create the builder and parse the file
    final DocumentBuilder builder = factory.newDocumentBuilder();
    builder.setEntityResolver(CachedSource.getResolver());
    return builder;
  }

  private Document getAnnotatedContentAsDocument(final String infoUri) throws IOException,
          NoSuchIdException, ParserConfigurationException, SAXException, ApplicationException {
    final String contentUrl = articleService.getObjectURL(infoUri, articleRep);
    return getAnnotatedContentAsDocument(contentUrl, infoUri);
  }
  
  
  private Document getAnnotatedContentAsDocument(final String contentUrl, final String infoUri)
          throws IOException, ParserConfigurationException, ApplicationException {
    final AnnotationInfo[] annotations = annotationWebService.listAnnotations(infoUri);
    return applyAnnotationsOnContentAsDocument (contentUrl, annotations);
  }

  private Document applyAnnotationsOnContentAsDocument (final String contentUrl, 
                                                        final AnnotationInfo[] annotations)
          throws IOException, ParserConfigurationException, ApplicationException {
    
    final DataHandler content = new DataHandler(new URLDataSource(new URL(contentUrl)));
    final DocumentBuilder builder = createDocBuilder();
    if (annotations.length != 0) {
      return Annotator.annotateAsDocument(content, annotations, builder);
    }
    try {
      return builder.parse(content.getInputStream());
    } catch (Exception e){
      log.error(e, e);
      throw new ApplicationException("Applying annotations failed for resource:" + contentUrl, e);
    }
  }
  
 
  /**
   * Getter for AnnotatationWebService
   * 
   * @return the annotationWebService
   */
  public AnnotationWebService getAnnotationWebService() {
    return annotationWebService;
  }

  /**
   * Setter for annotationWebService
   * 
   * @param annotationWebService annotationWebService
   */
  public void setAnnotationWebService(final AnnotationWebService annotationWebService) {
    this.annotationWebService = annotationWebService;
  }

  /**
   * Get a list of all articles
   * @param startDate startDate
   * @param endDate endDate
   * @return list of article uri's
   * @throws ApplicationException ApplicationException
   */
  public Collection<String> getArticles(final String startDate, final String endDate) throws ApplicationException {
    final Collection<String> articles = new ArrayList<String>();

    try {
      final String articlesDoc = articleService.getArticles(startDate, endDate);

      // Create the builder and parse the file
      final Document articleDom = factory.newDocumentBuilder().parse(new InputSource(new StringReader(articlesDoc)));

      // Get the matching elements
      final NodeList nodelist = XPathAPI.selectNodeList(articleDom, "/articles/article/uri");

      for (int i = 0; i < nodelist.getLength(); i++) {
        final Element elem = (Element) nodelist.item(i);
        final String uri = elem.getTextContent();
        final String decodedArticleUri = URLDecoder.decode(uri, encodingCharset);
        articles.add(decodedArticleUri);
      }

      return articles;
    } catch (Exception e) {
      throw new ApplicationException(e);
    }
  }

  /**
   * Set the encoding charset
   * @param encodingCharset encodingCharset
   */
  public void setEncodingCharset(final String encodingCharset) {
    this.encodingCharset = encodingCharset;
  }
}
