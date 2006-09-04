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
import java.io.InputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.ref.SoftReference;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
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

import org.topazproject.configuration.ConfigurationStore;
import org.topazproject.fedora.client.Uploader;
import org.topazproject.fedora.client.FedoraAPIM;
import org.topazproject.ws.article.DuplicateIdException;
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

  private static final String OL_LOG_A     = "logMessage";
  private static final String OL_AID_A     = "articleId";
  private static final String OBJECT       = "Object";
  private static final String O_PID_A      = "pid";
  private static final String DATASTREAM   = "Datastream";
  private static final String DS_FIL_A     = "filename";
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
  private final FedoraAPIM         apim;
  private final Uploader           uploader;
  private final ItqlHelper         itql;
  private final ArticlePEP         pep;

  /** 
   * Create a new ingester pointing to the given fedora server.
   * 
   * @param apim     the Fedora APIM client
   * @param uploader the Fedora uploader client
   * @param itql     the mulgara iTQL client
   * @param pep      the policy-enforcer to use for access-control
   */
  public Ingester(FedoraAPIM apim, Uploader uploader, ItqlHelper itql, ArticlePEP pep) {
    this.apim     = apim;
    this.uploader = uploader;
    this.itql     = itql;
    this.pep      = pep;

    tFactory = new TransformerFactoryImpl();
    tFactory.setURIResolver(new URLResolver());
  }

  /** 
   * Ingest a new article. 
   * 
   * @param zip  the zip archive containing the article and it's related objects
   * @return the DOI of the new article
   * @throws DuplicateIdException if an article or other object already exists with any of the
   *                              DOI's specified in the zip
   * @throws IngestException if there's any other problem ingesting the article
   */
  public String ingest(Zip zip) throws DuplicateIdException, IngestException {
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
      Document objInfo = zip2Obj(zip, zipInfo, handler);
      if (log.isDebugEnabled())
        log.debug("Got object-info '" + dom2String(objInfo) + "'");

      // get the article id
      Element objList = objInfo.getDocumentElement();
      String doi = objList.getAttribute(OL_AID_A);

      // do the access check, now that we have the doi
      pep.checkAccess(pep.INGEST_ARTICLE,
                      URI.create(ArticleImpl.pid2URI(ArticleImpl.doi2PID(doi))));

      // add the stuff
      String txn = "ingest " + doi;
      try {
        itql.beginTxn(txn);

        /* put the RDF into the triple-store before ingesting into Fedora, because it's much
         * harder to properly roll back Fedora if an error occurs there.
         */
        mulgaraInsert(objInfo);
        fedoraIngest(zip, objInfo);

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
    } catch (TransformerException te) {
      throw new IngestException("Zip format error", te);
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
   * @return a document describing the fedora objects to create (must adhere to fedora.dtd)
   * @throws TransformerException if an error occurs during the processing
   */
  private Document zip2Obj(Zip zip, String zipInfo, String handler) throws TransformerException {
    Transformer t = tFactory.newTransformer(new StreamSource(handler));
    t.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
    t.setURIResolver(new ZipURIResolver(zip));

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
  private void mulgaraInsert(Document objInfo)
      throws TransformerException, IOException, RemoteException {
    // set up the transformer to generate the triples
    Transformer t = tFactory.newTransformer(new StreamSource(findRDFXML2TriplesConverter()));

    // create the fedora objects
    Element objList = objInfo.getDocumentElement();
    NodeList objs = objList.getElementsByTagNameNS(RDFNS, RDF);
    for (int idx = 0; idx < objs.getLength(); idx++) {
      Element obj = (Element) objs.item(idx);
      mulgaraInsertOneRDF(obj, t);
    }
  }

  private void mulgaraInsertOneRDF(Element obj, Transformer t)
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
  private void fedoraIngest(Zip zip, Document objInfo)
      throws DuplicateIdException, TransformerException, IOException, RemoteException {
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
        fedoraIngestOneObj(obj, t, zip, logMsg, objCreated);
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
      if (e instanceof DuplicateIdException)
        throw (DuplicateIdException) e;
      if (e instanceof TransformerException)
        throw (TransformerException) e;
      if (e instanceof IOException)
        throw (IOException) e;
      if (e instanceof RuntimeException)
        throw (RuntimeException) e;
      throw new Error(e);       // how could this happen?
    }
  }

  private void fedoraIngestOneObj(Element obj, Transformer t, Zip zip, String logMsg,
                                  boolean[] objCreated)
      throws DuplicateIdException, TransformerException, IOException, RemoteException {
    // create the foxml doc
    StringWriter sw = new StringWriter(200);
    t.transform(new DOMSource(obj), new StreamResult(sw));

    // create the fedora object
    String pid = obj.getAttribute(O_PID_A);
    fedoraCreateObject(pid, sw.toString(), logMsg);
    objCreated[0] = true;

    // add all (non-DC/non-RDF) datastreams
    NodeList dss = obj.getElementsByTagName(DATASTREAM);
    for (int idx = 0; idx < dss.getLength(); idx++) {
      Element ds = (Element) dss.item(idx);
      fedoraAddDatastream(pid, ds, zip, logMsg);
    }
  }

  private void fedoraCreateObject(String pid, String foxml, String logMsg)
      throws DuplicateIdException, IOException, RemoteException {
    if (log.isDebugEnabled())
      log.debug("Ingesting fedora object '" + pid + "'; foxml = " + foxml);

    try {
      apim.ingest(foxml.getBytes("UTF-8"), "foxml1.0", logMsg);
    } catch (RemoteException re) {
      FedoraUtil.detectDuplicateIdException(re, ArticleImpl.pid2DOI(pid));
    }
  }

  private void fedoraAddDatastream(String pid, Element ds, Zip zip, String logMsg)
      throws IOException, RemoteException {
    long[] size = new long[1];
    InputStream is = zip.getStream(ds.getAttribute(DS_FIL_A), size);
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
    public Source resolve(String href, String base) {
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
  private static class ZipURIResolver implements URIResolver {
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
        return null;

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
