/* $HeadURL::                                                                            $
 * $Id$
 */
package org.plos.article.service;

import com.sun.org.apache.xpath.internal.XPathAPI;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.ApplicationException;
import org.plos.annotation.service.AnnotationWebService;
import org.plos.annotation.service.Annotator;
import org.plos.util.FileUtils;
import org.topazproject.common.NoSuchIdException;
import org.topazproject.ws.annotation.AnnotationInfo;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
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
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.Writer;
import java.lang.ref.SoftReference;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

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
  private Map<String, InputSource> entityResolvers = new HashMap<String, InputSource>();
  private AnnotationWebService annotationWebService;
  private String firstEntLocation;
  private String firstEnt;

  public void init() {
    // Set the TransformerFactory system property.
    for (Map.Entry<String, String> entry : xmlFactoryProperty.entrySet()) {
      System.setProperty(entry.getKey(), entry.getValue());
    }
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
    final DocumentBuilder builder = getDocBuilder();

    final Document doc = builder.parse(getAnnotatedContentAsInputStream(articleURI));

    // Prepare the DOM source
    return new DOMSource(doc);
  }

  /**
   * Return the annotated content as text for a given articleUri
   * @param articleURI articleURI
   * @return an instance of DOMSource
   * @throws ParserConfigurationException ParserConfigurationException
   * @throws SAXException SAXException
   * @throws IOException IOException
   * @throws URISyntaxException URISyntaxException
   * @throws org.plos.ApplicationException ApplicationException
   * @throws org.topazproject.common.NoSuchIdException NoSuchIdException
   */
  public String getAnnotatedContent(final String articleURI) throws ParserConfigurationException, SAXException, IOException, URISyntaxException, ApplicationException, NoSuchIdException {
    final InputStream annotatedContentAsInputStream = getAnnotatedContentAsInputStream(articleURI);
    return FileUtils.getTextFromCharStream(annotatedContentAsInputStream);
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
    final DocumentBuilder builder = getDocBuilder();

    Document doc;
    try {
      doc = builder.parse(getAsFile(xmlFileURL));
    } catch (Exception e) {
      doc = builder.parse(xmlFileURL);
    }

    // Prepare the DOM source
    return new DOMSource(doc);
  }

  private DocumentBuilder getDocBuilder() throws ParserConfigurationException {
    // Create a builder factory
    final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true);
    factory.setValidating(false);

    // Create the builder and parse the file
    final DocumentBuilder builder = factory.newDocumentBuilder();
//    builder.setEntityResolver(new CachedEntityResolver());
    builder.setEntityResolver(new EntityResolver() {
      public InputSource resolveEntity(final String publicId, final String systemId) throws SAXException, IOException {
        return getInputSource(systemId);
      }
    });
    return builder;
  }

  private InputSource getInputSource(final String systemId) throws IOException {
    final String entityFilename = FileUtils.getFileName(systemId);
    if (firstEntLocation == null) {
      firstEntLocation = entityFilename;
      firstEnt = systemId;
    }
    
    final String entityUri = firstEnt.replaceFirst(firstEntLocation, entityFilename);
    //Get an instance from the cache
    //TODO: A potential bottleneck place when multiple callers are trying to work with the same instance of the entity resolver
//    InputSource entityResolver = entityResolvers.get(entityFilename);
    InputSource entityResolver = null;
    if (null == entityResolver) {
//      log.debug("Creating entity resolver for " + systemId);
      //TODO: fix this constant value thingy and also fix for location under /xmlchars/
      final String entityFilePath = "/jp-dtd-2.0/" + entityFilename;
      URL resourceURL = FetchArticleService.class.getResource(entityFilePath);
      if (null == resourceURL) {
        createLocalCopyOfEntity(entityUri, FetchArticleService.class.getResource("/").getPath(), entityFilePath);
        resourceURL = FetchArticleService.class.getResource(entityFilePath);
      }

      entityResolver = new InputSource(new BufferedReader(new InputStreamReader(resourceURL.openStream())));
      entityResolvers.put(entityFilePath, entityResolver);
    }
    return entityResolver;
  }

  private static void createLocalCopyOfEntity(final String systemId, final String location, final String entityFilename) throws IOException {
    if (FileUtils.isHttpURL(systemId)) {
      try {
        FileUtils.createLocalCopyOfTextFile(systemId, location + entityFilename);
        log.debug("local entity created = " + entityFilename);
      } catch (IOException e) {
        log.error("Entity creation failed for " + entityFilename, e);
        throw e;
      }
    }
  }

  /**
   * Return the annotated content
   * @param infoUri infoUri against which the annotations are created
   * @param contentUrl contentUrl
   * @return the annotated content
   * @throws java.io.IOException IOException
   * @throws org.topazproject.common.NoSuchIdException NoSuchIdException
   */
  public String getAnnotatedContent(final String contentUrl, final String infoUri) throws IOException, NoSuchIdException {
    return FileUtils.getTextFromCharStream(getAnnotatedContentAsInputStream(contentUrl, infoUri));
  }

  /**
   * Return the annotated content
   * @param infoUri infoUri
   * @return the annotated content
   * @throws java.io.IOException IOException
   * @throws org.topazproject.common.NoSuchIdException NoSuchIdException
   */
  private InputStream getAnnotatedContentAsInputStream(final String infoUri) throws IOException, NoSuchIdException {
    final String contentUrl = articleService.getObjectURL(infoUri, articleRep);
    return getAnnotatedContentAsInputStream(contentUrl, infoUri);
  }

  /**
   * Return the annotated content as an InputStream. 
   * @param infoUri infoUri against which the annotations are created
   * @param contentUrl contentUrl
   * @return the annotated content
   * @throws java.io.IOException IOException
   */
  private InputStream getAnnotatedContentAsInputStream(final String contentUrl, final String infoUri) throws IOException {
    final AnnotationInfo[] annotations = annotationWebService.listAnnotations(infoUri);
    return applyAnnotationsOnContent(contentUrl, annotations);
  }

  private InputStream applyAnnotationsOnContent(final String contentUrl, final AnnotationInfo[] annotations) throws IOException {
    DataHandler content = new DataHandler(new URLDataSource(new URL(contentUrl)));
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

      // Create a builder factory
      final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setValidating(false);

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

  private static class CachedEntityResolver implements EntityResolver {
    private static final URLRetriever retriever;

    static {
      URLRetriever r = new NetworkURLRetriever(null);
      try {
        r = new ResourceURLRetriever(r);
      } catch (IOException ioe) {
        log.error("Error loading entity-cache map - continuing without it", ioe);
      }
      retriever = new MemoryCacheURLRetriever(r);
    }

    public InputSource resolveEntity(String publicId, String systemId) throws IOException {
      if (log.isDebugEnabled())
        log.debug("Resolving entity '" + systemId + "'");

      byte[] res = retriever.retrieve(systemId);
      if (log.isDebugEnabled())
        log.debug("Entity '" + systemId + "' " + (res != null ? "found" : "not found"));

      if (res == null)
        return null;

      InputSource is = new InputSource(new ByteArrayInputStream(res));
      is.setPublicId(publicId);
      is.setSystemId(systemId);

      return is;
    }
  }

  /**
   * Retrieve the content of a URL.
   */
  private static interface URLRetriever {
    /**
     * Retrieve the contents of a URL as a byte[].
     *
     * @param url the url to retrieve
     * @return the contents, or null if not found
     * @throws IOException if an error occurred retrieving the contents (other than not-found)
     */
    public byte[] retrieve(String url) throws IOException;
  }

  /**
   * Look up the URL in an in-memory cache.
   */
  private static class MemoryCacheURLRetriever implements URLRetriever {
    private final Map          cache = new HashMap();
    private final URLRetriever delegate;

    public MemoryCacheURLRetriever(URLRetriever delegate) {
      this.delegate = delegate;
    }

    public synchronized byte[] retrieve(String url) throws IOException {
      SoftReference ref = (SoftReference) cache.get(url);
      byte[] res = (ref != null) ? (byte[]) ref.get() : null;

      if (log.isDebugEnabled())
        log.debug("Memory cache('" + url + "'): " +
                  (res != null ? "found" : ref != null ? "expired" : "not found"));

      if (res != null || delegate == null)
        return res;

      res = delegate.retrieve(url);
      if (res == null)
        return null;

      if (log.isDebugEnabled())
        log.debug("Caching '" + url + "'");

      cache.put(url, new SoftReference(res));
      return res;
    }
  }

  /**
   * Look up the URL in a resource cache.
   */
  private static class ResourceURLRetriever implements URLRetriever {
    private final Properties urlMap;
    private final URLRetriever delegate;

    public ResourceURLRetriever(URLRetriever delegate) throws IOException {
      this.delegate = delegate;

      String map_file = "url_rsrc_map.properties";
      InputStream is = ResourceURLRetriever.class.getResourceAsStream(map_file);

      urlMap = new Properties();
      if (is != null)
        urlMap.load(is);
      else
        log.info("url-map resource '" + map_file + "' not found - no map loaded");
    }

    public byte[] retrieve(String url) throws IOException {
      String resource = urlMap.getProperty(url);

      if (log.isDebugEnabled())
        log.debug("Resource retriever ('" + url + "'): " +
                  (resource != null ? "found" : "not found"));

      if (resource == null)
        return (delegate != null) ? delegate.retrieve(url) : null;

      return IOUtils.toByteArray(ResourceURLRetriever.class.getResourceAsStream(resource));
    }
  }

  /**
   * Retrieve the URL over the network.
   */
  private static class NetworkURLRetriever implements URLRetriever {
    private final URLRetriever delegate;

    public NetworkURLRetriever(URLRetriever delegate) {
      this.delegate = delegate;
    }

    public byte[] retrieve(String url) throws IOException {
      if (log.isDebugEnabled())
        log.debug("Network retriever ('" + url + "')");

      return IOUtils.toByteArray(new URL(url).openStream());
    }
  }

}
