/* $HeadURL::                                                                            $
 * $Id$
 */
package org.plos.article.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.ApplicationException;
import org.plos.annotation.service.AnnotationWebService;
import org.plos.annotation.service.Annotator;
import org.plos.util.FileUtils;
import org.topazproject.common.NoSuchIdException;
import org.topazproject.ws.annotation.AnnotationInfo;
import org.w3c.dom.Document;
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
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Writer;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.HashMap;
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

  private static final Log log = LogFactory.getLog(FetchArticleService.class);
  private Map<String, InputSource> entityResolvers = new HashMap<String, InputSource>();
  private AnnotationWebService annotationWebService;

  public void init() {
    // Set the TransformerFactory system property.
    for (Map.Entry<String, String> entry : xmlFactoryProperty.entrySet()) {
      System.setProperty(entry.getKey(), entry.getValue());
    }
  }

  /**
   * Get the DOI transformed as HTML.
   * @param articleDOI articleDOI
   * @param writer writer
   * @throws org.plos.ApplicationException ApplicationException
   * @throws java.rmi.RemoteException RemoteException
   * @throws org.topazproject.common.NoSuchIdException NoSuchIdException
   */
  public void getDOIAsHTML(final String articleDOI, final Writer writer) throws ApplicationException, RemoteException, NoSuchIdException {
    final String objectURL = articleService.getObjectURL(articleDOI, articleRep);

    transform(objectURL, writer);
  }

  private void transform(final String objectURL, final Writer writer) throws ApplicationException {
    try {
      final Transformer transformer = getTransformer();

      transformer.transform(getDOMSource(objectURL),
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
   * @throws TransformerConfigurationException TransformerConfigurationException
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
   * Get the xmlFile as a DOMSource.
   * @param xmlFile xmlFile
   * @return an instance of DOMSource
   * @throws ParserConfigurationException ParserConfigurationException
   * @throws SAXException SAXException
   * @throws IOException IOException
   * @throws URISyntaxException URISyntaxException
   * @throws org.plos.ApplicationException ApplicationException
   */
  public Source getDOMSource(final String xmlFile) throws ParserConfigurationException, SAXException, IOException, URISyntaxException, ApplicationException {
    // Create a builder factory
    final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true);
    factory.setValidating(false);

    // Create the builder and parse the file
    final DocumentBuilder builder = factory.newDocumentBuilder();
//    builder.setEntityResolver(new EntityResolver() {
//      public InputSource resolveEntity(final String publicId, final String systemId) throws SAXException, IOException {
//        return getInputSource(systemId);
//      }
//
//    });

    Document doc;
    if (FileUtils.isHttpURL(xmlFile)) {
      doc = builder.parse(getAnnotatedContentAsInputStream(xmlFile));
    } else {
      try {
        doc = builder.parse(getAsFile(xmlFile));
      } catch (Exception e) {
        doc = builder.parse(xmlFile);
      }
    }

    // Prepare the DOM source
    return new DOMSource(doc);
  }

  private InputSource getInputSource(final String systemId) throws IOException {
    final String entityFilename = FileUtils.getFileName(systemId);

    createLocalCopyOfEntity(systemId, entityFilename);
    final FileReader fileReader = new FileReader(entityFilename);

    //Get an instance from the cache
    //TODO: A potential bottleneck place when multiple callers are trying to work with the same instance of the entity resolver
    InputSource entityResolver = entityResolvers.get(systemId);
    if (null == entityResolver) {
      entityResolver = new InputSource(fileReader);
      entityResolvers.put(systemId, entityResolver);
    }
    return entityResolver;
  }

  private static void createLocalCopyOfEntity(String systemId, String entityFilename) throws IOException {
    //TODO: This keeps creating the "journalpublishing.dtd" again and again. Need to not create it if already existing.
    if (FileUtils.isHttpURL(systemId)) {
      try {
        FileUtils.createLocalCopyOfTextFile(systemId, entityFilename);
        log.debug("local dtd created = " + entityFilename);
      } catch (IOException e) {
        log.warn("Entity creation failed", e);
        throw e;
      }
    }
  }

  /**
   * Return the annotated content
   * @param target target
   * @return the annotated content
   * @throws java.io.IOException IOException
   */
  public String getAnnotatedContent(final String target) throws IOException {
    return FileUtils.getTextFromCharStream(getAnnotatedContentAsInputStream(target));
  }

  /**
   * Return the annotated content
   * @param target target
   * @return the annotated content
   * @throws java.io.IOException IOException
   */
  public InputStream getAnnotatedContentAsInputStream(final String target) throws IOException {
    DataHandler content = new DataHandler(new URLDataSource(new URL(target)));
    final AnnotationInfo[] annotations = annotationWebService.listAnnotations(target);

    if (annotations.length != 0) {
      content = Annotator.annotate(content, annotations);
    }

    return content.getInputStream();
  }

  public AnnotationWebService getAnnotationWebService() {
    return annotationWebService;
  }

  public void setAnnotationWebService(final AnnotationWebService annotationWebService) {
    this.annotationWebService = annotationWebService;
  }
}
