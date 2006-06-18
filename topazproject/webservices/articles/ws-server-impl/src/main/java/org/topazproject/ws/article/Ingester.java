
package org.topazproject.ws.article;

import java.io.InputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.rmi.RemoteException;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.topazproject.mulgara.itql.ItqlHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import fedora.client.Uploader;
import fedora.server.management.FedoraAPIM;

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
   * @param itql     the mulgara iTQL client client
   * @param pep      the policy-enforcer to use for access-control
   */
  public Ingester(FedoraAPIM apim, Uploader uploader, ItqlHelper itql, ArticlePEP pep) {
    this.apim     = apim;
    this.uploader = uploader;
    this.itql     = itql;
    this.pep      = pep;

    tFactory = new TransformerFactoryImpl();
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

      // use handler to convert zip to fedora-object descriptions
      Document objInfo = zip2Obj(zip, zipInfo, handler);
      if (log.isDebugEnabled())
        log.debug("Got object-info '" + dom2String(objInfo) + "'");

      // get the article id
      Element objList = objInfo.getDocumentElement();
      String doi = objList.getAttribute(OL_AID_A);

      // ingest into fedora
      pep.checkAccess(pep.INGEST_ARTICLE,
                      URI.create(ArticleImpl.pid2URI(ArticleImpl.doi2PID(doi))));
      fedoraIngest(zip, objInfo);

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

    Source    inp = new StreamSource(new StringReader(zipInfo), "zip:/");
    DOMResult res = new DOMResult();
    t.transform(inp, res);

    return (Document) res.getNode();
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
    int idx = 0;
    try {
      for (idx = 0; idx < objs.getLength(); idx++) {
        Element obj = (Element) objs.item(idx);
        fedoraIngestOneObj(obj, t, zip, logMsg);
      }
    } catch (Exception e) {
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

  private void fedoraIngestOneObj(Element obj, Transformer t, Zip zip, String logMsg)
      throws DuplicateIdException, TransformerException, IOException, RemoteException {
    // create the foxml doc
    StringWriter sw = new StringWriter(200);
    t.transform(new DOMSource(obj), new StreamResult(sw));

    // create the fedora object
    String pid = obj.getAttribute(O_PID_A);
    fedoraCreateObject(pid, sw.toString(), logMsg);

    // add all (non-DC/non-RDF) datastreams
    NodeList dss = obj.getElementsByTagName(DATASTREAM);
    for (int idx = 0; idx < dss.getLength(); idx++) {
      Element ds = (Element) dss.item(idx);
      fedoraAddDatastream(pid, ds, zip, logMsg);
    }
  }

  private void fedoraCreateObject(String pid, String foxml, String logMsg)
      throws DuplicateIdException, IOException, RemoteException {
    try {
      apim.ingest(foxml.getBytes("UTF-8"), "foxml1.0", logMsg);
    } catch (RemoteException re) {
      FedoraUtil.detectDuplicateIdException(re, pid);
    }
  }

  private void fedoraAddDatastream(String pid, Element ds, Zip zip, String logMsg)
      throws IOException, RemoteException {
    String reLoc = uploader.upload(zip.getStream(ds.getAttribute(DS_FIL_A)));
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
   * This allows the stylesheets to access XML docs (such as pmc.xml) in the zip archive.
   */
  private static class ZipURIResolver implements URIResolver {
    private final Zip zip;

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
        InputStream is = zip.getStream(uri.getPath().substring(1));

        if (log.isDebugEnabled())
          log.debug("resolved: uri='" + uri + "', found=" + (is != null));

        return (is != null) ? new StreamSource(is, uri.toString()) : null;
      } catch (IOException ioe) {
        throw new TransformerException(ioe);
      }
    }
  }
}
