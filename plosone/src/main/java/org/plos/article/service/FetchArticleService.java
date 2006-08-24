/* $HeadURL::                                                                            $
 * $Id$
 */
package org.plos.article.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.util.FileUtils;
import org.topazproject.ws.article.NoSuchIdException;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

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
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.HashMap;

/**
 * Fetch article service
 */
public class FetchArticleService {
  private ArticleService articleService;
  private File xslTemplate;
  private String articleRep;
  private Templates translet;
  private boolean useTranslet = true;
  private Map<String, String> xmlFactoryProperty;

  private static final Log log = LogFactory.getLog(FetchArticleService.class);
  private Map<String, InputSource> entityResolvers = new HashMap<String, InputSource>();

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
   * @throws IOException
   * @throws TransformerException
   * @throws java.net.MalformedURLException
   * @throws java.io.FileNotFoundException
   * @throws java.rmi.RemoteException
   * @throws org.topazproject.ws.article.NoSuchIdException
   * @throws javax.xml.parsers.ParserConfigurationException
   * @throws org.xml.sax.SAXException
   * @throws java.net.URISyntaxException
   */
  public void getDOIAsHTML(final String articleDOI, final Writer writer) throws TransformerException, NoSuchIdException, IOException, RemoteException, MalformedURLException, FileNotFoundException, ParserConfigurationException, SAXException, URISyntaxException {
    final String objectURL = articleService.getObjectURL(articleDOI, articleRep);

    transform(objectURL, writer);
  }

  private void transform(final String objectURL, final Writer writer) throws TransformerException, ParserConfigurationException, SAXException, IOException, URISyntaxException {
    final Transformer transformer = getTransformer();

    transformer.transform(
                          getDOMSource(objectURL),
                          new StreamResult(writer));
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
  public void setArticleService(final ArticleService articleService) {
    this.articleService = articleService;
  }

  private TransformerFactory tFactory;
  private StreamSource source;

  /**
   * Get an XSL transformer.
   * @return Transformer
   * @throws TransformerConfigurationException
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
   * @throws TransformerException
   * @throws FileNotFoundException
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
   * @throws java.net.URISyntaxException
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
   * @throws URISyntaxException
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
   * @throws ParserConfigurationException
   * @throws SAXException
   * @throws IOException
   * @throws URISyntaxException
   */
  public Source getDOMSource(final String xmlFile) throws ParserConfigurationException, SAXException, IOException, URISyntaxException {
    // Create a builder factory
    final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true);
    factory.setValidating(false);

    // Create the builder and parse the file
    final DocumentBuilder builder = factory.newDocumentBuilder();
    builder.setEntityResolver(new EntityResolver() {
      public InputSource resolveEntity(final String publicId, final String systemId) throws SAXException, IOException {
        return getInputSource(systemId);
      }

    });

    Document doc;
    if (FileUtils.isHttpURL(xmlFile)) {
      doc = builder.parse(xmlFile);
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

}
