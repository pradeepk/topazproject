/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.ws.article.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.ref.SoftReference;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.topazproject.mulgara.itql.ItqlHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import org.topazproject.common.impl.TopazContext;
import org.topazproject.configuration.ConfigurationStore;
import org.topazproject.fedora.client.Uploader;
import org.topazproject.fedora.client.FedoraAPIM;
import org.topazproject.ws.article.DuplicateArticleIdException;
import org.topazproject.ws.article.IngestException;

import net.sf.saxon.Controller;
import net.sf.saxon.TransformerFactoryImpl;

/** 
 * The article ingestor.
 * 
 * @author Ronald Tschalär
 */
public class Ingester {
  private static final Log    log = LogFactory.getLog(Ingester.class);

  private static final String OUT_LOC_P    = "output-loc";

  private static final String OL_LOG_A     = "logMessage";
  private static final String OL_AID_A     = "articleId";
  private static final String OBJECT       = "Object";
  private static final String O_PID_A      = "pid";
  private static final String DATASTREAM   = "Datastream";
  private static final String DS_CONTLOC_A = "contLoc";
  private static final String DS_ID_A      = "id";
  private static final String DS_ST_A      = "state";
  private static final String DS_CGRP_A    = "controlGroup";
  private static final String DS_MIME_A    = "mimeType";
  private static final String DS_LBL_A     = "label";
  private static final String DS_ALTID_A   = "altIds";
  private static final String DS_FMT_A     = "formatUri";
  private static final String RDF          = "RDF";
  private static final String RDFNS        = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";

  private static final Configuration CONF  = ConfigurationStore.getInstance().getConfiguration();
  private static final String MODEL        = "<" + CONF.getString("topaz.models.articles") + ">";

  private final TransformerFactory tFactory;
  private final TopazContext       ctx;
  private final ArticlePEP         pep;

  /** 
   * Create a new ingester pointing to the given fedora server.
   * 
   * @param apim     the Fedora APIM client
   * @param uploader the Fedora uploader client
   * @param itql     the mulgara iTQL client
   * @param pep      the policy-enforcer to use for access-control
   */
  public Ingester(ArticlePEP pep, TopazContext ctx) {
    this.ctx     = ctx;
    this.pep      = pep;

    tFactory = new TransformerFactoryImpl();
    tFactory.setURIResolver(new URLResolver());
    tFactory.setAttribute("http://saxon.sf.net/feature/version-warning", Boolean.FALSE);
  }

  /** 
   * Ingest a new article. 
   * 
   * @param zip  the zip archive containing the article and it's related objects
   * @return the DOI of the new article
   * @throws DuplicateArticleIdException if an article or other object already exists with any
   *                              of the DOI's specified in the zip
   * @throws IngestException if there's any other problem ingesting the article
   */
  public String ingest(Zip zip) throws DuplicateArticleIdException, IngestException {
    File tmpDir = createTempDir();

    try {
      // get zip info
      String zipInfo = Zip2Xml.describeZip(zip);
      if (log.isDebugEnabled())
        log.debug("Extracted zip-description: " + zipInfo);

      // find ingest format handler
      String handler = findIngestHandler(zipInfo);
      if (log.isDebugEnabled())
        log.debug("Using ingest handler '" + handler + "'");

      // use handler to convert zip to fedora-object and RDF descriptions
      Document objInfo = zip2Obj(zip, zipInfo, handler, tmpDir);
      if (log.isDebugEnabled())
        log.debug("Got object-info '" + dom2String(objInfo) + "'");

      // get the article id
      Element objList = objInfo.getDocumentElement();
      String doi = objList.getAttribute(OL_AID_A);

      // do the access check, now that we have the doi
      pep.checkAccess(pep.INGEST_ARTICLE, URI.create(ArticleImpl.doi2URI(doi)));

      // add the stuff
      ItqlHelper itql   = ctx.getItqlHelper();
      FedoraAPIM apim   = ctx.getFedoraAPIM();
      Uploader uploader = ctx.getFedoraUploader();
      String txn = "ingest " + doi;
      try {
        itql.beginTxn(txn);

        /* put the RDF into the triple-store before ingesting into Fedora, because it's much
         * harder to properly roll back Fedora if an error occurs there.
         */
        mulgaraInsert(itql, objInfo);
        fedoraIngest(apim, uploader, zip, objInfo);

        itql.commitTxn(txn);
        txn = null;
      } finally {
        if (txn != null)
          itql.rollbackTxn(txn);
      }

      return doi;
    } catch (RemoteException re) {
      throw new IngestException("Error ingesting into fedora", re);
    } catch (IOException ioe) {
      throw new IngestException("Error talking to fedora", ioe);
    } catch (URISyntaxException use) {
      throw new IngestException("Zip format error", use);
    } catch (TransformerException te) {
      throw new IngestException("Zip format error", te);
    } finally {
      try {
        FileUtils.deleteDirectory(tmpDir);
      } catch (IOException ioe) {
        log.warn("Failed to delete tmp-dir '" + tmpDir + "'", ioe);
      }
    }
  }

  private static final File createTempDir() throws IngestException {
    try {
      for (int idx = 0; idx < 10; idx++) {
        File f = File.createTempFile("ingest_", ".d");
        FileUtils.forceDelete(f);
        if (f.mkdir())
          return f;
      }
      throw new IngestException("Failed to create temporary directory - tried 10 times");
    } catch (IOException ioe) {
      throw new IngestException("Failed to create or delete temporary file", ioe);
    }
  }

  /**
   * Look up the appropriate stylesheet to handle the zip. This stylesheet is responsible
   * for converting a zip-info doc (zip.dtd) to a fedora-objects doc (fedora.dtd).
   *
   * @param zipInfo the zip archive description (zip.dtd)
   * @return the URL of the stylesheet
   */
  private String findIngestHandler(String zipInfo) {
    // FIXME: make this configurable, and allow for some sort of lookup
    return getClass().getResource("pmc2obj.xslt").toString();
  }

  /**
   * Get the stylesheet that generates the triples from an RDF/XML doc.
   *
   * @return the URL of the stylesheet
   */
  private String findRDFXML2TriplesConverter() {
    // FIXME: make this configurable
    return getClass().getResource("RdfXmlToTriples.xslt").toString();
  }

  /**
   * Get the stylesheet that generates the foxml doc from an object description (fedora.dtd).
   *
   * @return the URL of the stylesheet
   */
  private String findFoxmlGenerator() {
    // FIXME: make this configurable
    return getClass().getResource("obj2foxml.xslt").toString();
  }

  /** 
   * Run the main ingest script. 
   * 
   * @param zip     the zip archive containing the items to ingest
   * @param zipInfo the document describing the zip archive (adheres to zip.dtd)
   * @param handler the stylesheet to run on <var>zipInfo</var>; this is the main script
   * @param tmpDir  the temporary directory to use for storing files
   * @return a document describing the fedora objects to create (must adhere to fedora.dtd)
   * @throws TransformerException if an error occurs during the processing
   */
  private Document zip2Obj(Zip zip, String zipInfo, String handler, File tmpDir)
      throws TransformerException {
    Transformer t = tFactory.newTransformer(new StreamSource(handler));
    t.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
    t.setURIResolver(new ZipURIResolver(zip));
    ((Controller) t).setBaseOutputURI(tmpDir.toURI().toString());
    t.setParameter(OUT_LOC_P, tmpDir.toURI().toString());

    /* Note: it would be preferable (and correct according to latest JAXP specs) to use
     * t.setErrorListener(), but Saxon does not forward <xls:message>'s to the error listener.
     * Hence we need to use Saxon's API's in order to get at those messages.
     */
    StringWriter msgs = new StringWriter();
    ((Controller) t).makeMessageEmitter();
    ((Controller) t).getMessageEmitter().setWriter(msgs);

    Source    inp = new StreamSource(new StringReader(zipInfo), "zip:/");
    DOMResult res = new DOMResult();

    try {
      t.transform(inp, res);
    } catch (TransformerException te) {
      if (msgs.getBuffer().length() > 0)
        throw new TransformerException(msgs.toString(), te);
      else
        throw te;
    }
    if (msgs.getBuffer().length() > 0)
      throw new TransformerException(msgs.toString());

    return (Document) res.getNode();
  }

  /** 
   * Insert the RDF into the triple-store according to the given object-info doc. 
   * 
   * @param objInfo the document describing the RDF to insert
   */
  private void mulgaraInsert(ItqlHelper itql, Document objInfo)
      throws TransformerException, IOException, RemoteException {
    // set up the transformer to generate the triples
    Transformer t = tFactory.newTransformer(new StreamSource(findRDFXML2TriplesConverter()));

    // create the fedora objects
    Element objList = objInfo.getDocumentElement();
    NodeList objs = objList.getElementsByTagNameNS(RDFNS, RDF);
    for (int idx = 0; idx < objs.getLength(); idx++) {
      Element obj = (Element) objs.item(idx);
      mulgaraInsertOneRDF(itql, obj, t);
    }
  }

  private void mulgaraInsertOneRDF(ItqlHelper itql, Element obj, Transformer t)
      throws TransformerException, IOException, RemoteException {
    StringWriter sw = new StringWriter(500);
    sw.write("insert ");

    t.transform(new DOMSource(obj), new StreamResult(sw));

    sw.write(" into " + MODEL + ";");

    itql.doUpdate(sw.toString());
  }

  /** 
   * Create the objects in Fedora according to the given object-info doc. 
   * 
   * @param zip     the zip archive containing the data for the objects to ingest
   * @param objInfo the document describing the objects and their datastreams to create
   */
  private void fedoraIngest(FedoraAPIM apim, Uploader uploader, Zip zip, Document objInfo)
      throws DuplicateArticleIdException, TransformerException, IOException, RemoteException,
             URISyntaxException {
    // set up the transformer to generate the foxml docs
    Transformer t = tFactory.newTransformer(new StreamSource(findFoxmlGenerator()));
    t.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

    // get the log message to use
    Element objList = objInfo.getDocumentElement();
    String logMsg = objList.getAttribute(OL_LOG_A);
    if (logMsg == null)
      logMsg = "Ingest";

    // create the fedora objects
    NodeList objs = objList.getElementsByTagName(OBJECT);
    boolean[] objCreated = new boolean[1];
    int idx = 0;
    try {
      for (idx = 0; idx < objs.getLength(); idx++) {
        Element obj = (Element) objs.item(idx);
        objCreated[0] = false;
        fedoraIngestOneObj(apim, uploader, obj, t, zip, logMsg, objCreated);
      }
    } catch (Exception e) {
      if (!objCreated[0])
        idx--;          // don't purge an existing object...

      // try to rollback the already completed stuff
      while (idx >= 0) {
        try {
          Element obj = (Element) objs.item(idx--);
          String pid = obj.getAttribute(O_PID_A);
          apim.purgeObject(pid, "Rolling back failed ingest", false);
        } catch (Exception ee) {
          log.error("Error while rolling back failed ingest; ingest-exception was '" + e + "'", ee);
        }
      }

      // rethrow the original exception
      if (e instanceof DuplicateArticleIdException)
        throw (DuplicateArticleIdException) e;
      if (e instanceof TransformerException)
        throw (TransformerException) e;
      if (e instanceof URISyntaxException)
        throw (URISyntaxException) e;
      if (e instanceof IOException)
        throw (IOException) e;
      if (e instanceof RuntimeException)
        throw (RuntimeException) e;
      throw new Error(e);       // how could this happen?
    }
  }

  private void fedoraIngestOneObj(FedoraAPIM apim, Uploader uploader, Element obj, Transformer t, 
                                  Zip zip, String logMsg, boolean[] objCreated)
      throws DuplicateArticleIdException, TransformerException, IOException, RemoteException,
             URISyntaxException {
    // create the foxml doc
    StringWriter sw = new StringWriter(200);
    t.transform(new DOMSource(obj), new StreamResult(sw));

    // create the fedora object
    String pid = obj.getAttribute(O_PID_A);
    fedoraCreateObject(apim, pid, sw.toString(), logMsg);
    objCreated[0] = true;

    // add all (non-DC/non-RDF) datastreams
    NodeList dss = obj.getElementsByTagName(DATASTREAM);
    for (int idx = 0; idx < dss.getLength(); idx++) {
      Element ds = (Element) dss.item(idx);
      fedoraAddDatastream(apim, uploader, pid, ds, zip, logMsg);
    }
  }

  private void fedoraCreateObject(FedoraAPIM apim, String pid, String foxml, String logMsg)
      throws DuplicateArticleIdException, IOException, RemoteException {
    if (log.isDebugEnabled())
      log.debug("Ingesting fedora object '" + pid + "'; foxml = " + foxml);

    try {
      apim.ingest(foxml.getBytes("UTF-8"), "foxml1.0", logMsg);
    } catch (RemoteException re) {
      FedoraUtil.detectDuplicateArticleIdException(re, ArticleImpl.pid2DOI(pid));
    }
  }

  private void fedoraAddDatastream(FedoraAPIM apim, Uploader uploader, String pid, Element ds, 
                                   Zip zip, String logMsg)
      throws IOException, RemoteException, URISyntaxException {
    long[] size = new long[1];
    InputStream is = getContent(ds, zip, size);
    String reLoc = (size[0] >= 0) ? uploader.upload(is, size[0]) : uploader.upload(is);

    if (log.isDebugEnabled())
      log.debug("Adding datastream for fedora object '" + pid + "', id = '" +
                ds.getAttribute(DS_ID_A) + "'");

    apim.addDatastream(pid, ds.getAttribute(DS_ID_A),
                       StringUtils.split(ds.getAttribute(DS_ALTID_A)),
                       ds.getAttribute(DS_LBL_A), true, ds.getAttribute(DS_MIME_A),
                       ds.getAttribute(DS_FMT_A), reLoc, ds.getAttribute(DS_CGRP_A).substring(0, 1),
                       ds.getAttribute(DS_ST_A).length() > 0 ?
                          ds.getAttribute(DS_ST_A).substring(0, 1) : "A",
                       logMsg);
  }

  private InputStream getContent(Element ds, Zip zip, long[] size) throws IOException, URISyntaxException {
    InputStream is;

    if (ds.hasAttribute(DS_CONTLOC_A)) {
      URI contLoc = new URI(ds.getAttribute(DS_CONTLOC_A));
      if (contLoc.getScheme() == null || contLoc.getScheme().equals("zip")) {
        is = zip.getStream(contLoc.getSchemeSpecificPart(), size);
      } else {
        URLConnection con = contLoc.toURL().openConnection();

        size[0] = con.getContentLength();
        is      = con.getInputStream();
      }
    } else {
      StringBuffer sb = new StringBuffer(500);
      for (Node n = ds.getFirstChild(); n != null; n = n.getNextSibling())
        sb.append(dom2String(n));

      byte[] cont = sb.toString().getBytes("UTF-8");

      size[0] = cont.length;
      is      = new ByteArrayInputStream(cont);
    }

    return is;
  }

  private String dom2String(Node dom) {
    try {
      StringWriter sw = new StringWriter(500);
      Transformer t = tFactory.newTransformer();
      t.transform(new DOMSource(dom), new StreamResult(sw));
      return sw.toString();
    } catch (TransformerException te) {
      log.error("Error converting dom to string", te);
      return "";
    }
  }

  /**
   * Saxon's default resolver uses URI.resolve() to resolve the relative URI's. However, that
   * doesn't understand 'jar' URL's and treats them as opaque (because of the "jar:file:"
   * prefix). That in turn prevents us from using relative URI's for things like xsl:include
   * and xsl:import . Hence this class here, which just uses URL to do the resolution.
   */
  private static class URLResolver implements URIResolver {
    public Source resolve(String href, String base) throws TransformerException {
      if (href.length() == 0)
        return null;  // URL doesn't handle this case properly, so let default resolver handle it

      try {
        URL url = new URL(new URL(base), href);
        return new StreamSource(url.toString());
      } catch (MalformedURLException mue) {
        log.warn("Failed to resolve '" + href + "' relative to '" + base + "' - falling back to " +
                 "default URIResolver", mue);
        return null;
      }
    }
  }

  /**
   * This allows the stylesheets to access XML docs (such as pmc.xml) in the zip archive.
   */
  private static class ZipURIResolver extends URLResolver {
    private static final String xmlReaderCName;
    private final Zip zip;

    static {
      /* Remember the name of the XMLReader class so we can use it in the XMLReaderFactory
       * call. Note that we don't set the org.xml.sax.driver property because there seem to
       * be cases where that property is removed again.
       */
      String cname = null;
      try {
        cname =
            SAXParserFactory.newInstance().newSAXParser().getXMLReader().getClass().getName();
      } catch (Exception e) {
        log.warn("Failed to get the XMLReader class", e);
      }
      xmlReaderCName = cname;
    }

    /** 
     * Create a new resolver that returns documents from the given zip.
     * 
     * @param zip the zip archive to return documents from
     */
    public ZipURIResolver(Zip zip) {
      this.zip = zip;
    }

    public Source resolve(String href, String base) throws TransformerException {
      if (log.isDebugEnabled())
        log.debug("resolving: base='" + base + "', href='" + href + "'");

      if (!base.startsWith("zip:"))
        return super.resolve(href, base);

      try {
        URI uri = URI.create(base).resolve(href);
        InputStream is = zip.getStream(uri.getPath().substring(1), new long[1]);

        if (log.isDebugEnabled())
          log.debug("resolved: uri='" + uri + "', found=" + (is != null));

        if (is == null)
          return null;

        InputSource src = new InputSource(is);
        src.setSystemId(uri.toString());

        XMLReader rdr = XMLReaderFactory.createXMLReader(xmlReaderCName);
        rdr.setEntityResolver(new CachedEntityResolver());

        return new SAXSource(rdr, src);

      } catch (IOException ioe) {
        throw new TransformerException(ioe);
      } catch (SAXException se) {
        throw new TransformerException(se);
      }
    }
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
    private static final String CONF_KEY = "topaz.articles.url_rsrc_map";
    private static final String DEF_MAP  = "url_rsrc_map.properties";

    private final Properties   urlMap;
    private final URLRetriever delegate;

    public ResourceURLRetriever(URLRetriever delegate) throws IOException {
      this.delegate = delegate;

      Configuration conf = ConfigurationStore.getInstance().getConfiguration();
      String map_file = conf.getString(CONF_KEY, DEF_MAP);
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
