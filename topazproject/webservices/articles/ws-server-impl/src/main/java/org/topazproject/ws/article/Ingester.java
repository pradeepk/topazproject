
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

import org.apache.log4j.Logger;
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
  private static final Logger log = Logger.getLogger(Ingester.class);

  private final TransformerFactory tFactory;
  private final FedoraAPIM         apim;
  private final Uploader           uploader;

  /** 
   * Create a new ingester pointing to the given fedora server.
   * 
   * @param apim     the Fedora APIM client
   * @param uploader the Fedora uploader client
   */
  public Ingester(FedoraAPIM apim, Uploader uploader) {
    this.apim     = apim;
    this.uploader = uploader;

    tFactory = new TransformerFactoryImpl();
  }

  /** 
   * Ingest a new article. 
   * 
   * @param zip  the zip archive containing the article and it's related objects
   * @throws DuplicateIdException if an article or other object already exists with any of the
   *                              DOI's specified in the zip
   * @throws IngestException if there's any other problem ingesting the article
   */
  public void ingestNew(Zip zip) throws DuplicateIdException, IngestException {
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

      // ingest into fedora
      fedoraIngest(zip, objInfo);
    } catch (RemoteException re) {
      throw new IngestException("Error ingesting into fedora", re);
    } catch (IOException ioe) {
      throw new IngestException("Error talking to fedora", ioe);
    } catch (TransformerException te) {
      throw new IngestException("Zip format error", te);
    }
  }

  /** 
   * Ingest an update of an existing article. 
   * 
   * @param zip  the zip archive containing the article and it's related objects
   * @return the version number assigned to this version
   * @throws NoSuchIdException if not article exists with the DOI specified in the zip
   * @throws IngestException if there's any other problem ingesting the article
   */
  public int ingestUpdate(Zip zip) throws NoSuchIdException, IngestException {
    try {
      // get zip info
      String zipInfo = Zip2Xml.describeZip(zip);

      // find ingest format handler
      String handler = findIngestHandler(zipInfo);

      // XXX
      return -1;
    } catch (RemoteException re) {
      throw new IngestException("Error ingesting into fedora", re);
    } catch (IOException ioe) {
      throw new IngestException("Error talking to fedora", ioe);
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
    String logMsg = objList.getAttribute("logMessage");
    if (logMsg == null)
      logMsg = "Ingest";

    // create the fedora objects
    NodeList objs = objList.getElementsByTagName("Object");
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
          String pid = obj.getAttribute("pid");
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
    String pid = obj.getAttribute("pid");
    fedoraCreateObject(pid, sw.toString(), logMsg);

    // add all (non-DC/non-RDF) datastreams
    NodeList dss = obj.getElementsByTagName("Datastream");
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
    String reLoc = uploader.upload(zip.getStream(ds.getAttribute("filename")));
    apim.addDatastream(pid, ds.getAttribute("id"), new String[0], ds.getAttribute("label"), true,
                       ds.getAttribute("mimeType"), ds.getAttribute("formatUri"), reLoc,
                       ds.getAttribute("controlGroup").substring(0, 1),
                       ds.getAttribute("state").length() > 0 ?
                          ds.getAttribute("state").substring(0, 1) : "A",
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
        InputStream is = zip.getStream(uri.getPath());

        if (log.isDebugEnabled())
          log.debug("resolved: uri='" + uri + "', found=" + (is != null));

        return (is != null) ? new StreamSource(is, uri.toString()) : null;
      } catch (IOException ioe) {
        throw new TransformerException(ioe);
      }
    }
  }
}
