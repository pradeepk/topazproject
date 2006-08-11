/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.kowari;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.xml.rpc.ServiceException;

import org.apache.log4j.Logger;
import org.jrdf.graph.Node;
import org.jrdf.graph.URIReference;
import org.kowari.query.Answer;
import org.kowari.query.QueryException;
import org.kowari.query.TuplesException;
import org.kowari.server.SessionFactory;
import org.kowari.server.JRDFSession;
import org.kowari.resolver.spi.GlobalizeException;
import org.kowari.resolver.spi.ResolverException;
import org.kowari.resolver.spi.ResolverSession;
import org.kowari.resolver.spi.Statements;
import org.openrdf.rio.RdfDocumentWriter;
import org.openrdf.rio.rdfxml.RdfXmlWriter;
import org.trippi.io.RIOTripleWriter;
import org.trippi.impl.kowari.KowariTripleIterator;

import fedora.client.APIMStubFactory;
import fedora.client.Uploader;
import fedora.common.Constants;
import fedora.server.management.FedoraAPIM;

/** 
 * This takes care of updating Fedora with the new statements. The changed objects are queued, and a
 * worker periodically processes the queue and updates the DC and RELS-EXT streams of the affected
 * objects.
 * 
 * @author Ronald Tschalär
 */
class FedoraUpdater {
  private static final Logger logger = Logger.getLogger(FedoraUpdater.class);

  private final Set             queue = new HashSet();
  private final FedoraAPIM      apim;
  private final Uploader        uploader;
  private final URI             modelURI;
  private final SessionFactory  sessFactory;
  private       JRDFSession     sess;

  /** 
   * Create a new udpater instance.
   * 
   * @param serverURL   the URL of the fedora server's API-A/API-M webservice
   * @param username    the username with which to authenticate to fedora
   * @param password    the password with which to authenticate to fedora
   * @param modelURI    the URI of the resource-index model
   * @param sessFactory the factory to use to get db sessions
   * @throws IOException if an error occured setting up the fedora client or updater
   */
  public FedoraUpdater(URI serverURL, String username, String password, URI modelURI,
                       SessionFactory sessFactory) throws IOException, ServiceException {
    this.sessFactory = sessFactory;
    this.modelURI    = modelURI;

    /* create the instances needed to access fedora.
     * Note that we don't use FedoraClient directly (FedoraClient.getAPIM()) because that
     * goes off and initializes (messes up) the log4j stuff. Instead, we just go to the soap
     * factory directly.
     */
    apim = APIMStubFactory.getStub(serverURL.getScheme(), serverURL.getHost(), serverURL.getPort(),
                                   username, password);
    uploader = new Uploader(serverURL.getScheme(), serverURL.getHost(), serverURL.getPort(),
                            username, password);

    new Worker(5000L).start();
  }

  public void queueMod(Statements statements, boolean occurs, ResolverSession resolverSession)
      throws ResolverException {
    synchronized (queue) {
      try {
        statements.beforeFirst();
        while (statements.next()) {
          URIReference s = toURINode(resolverSession, statements.getSubject());
          queue.add(s);
          if (logger.isDebugEnabled())
            logger.debug("Queued '" + s + "' for update");
        }
      } catch (TuplesException te) {
        throw new ResolverException("Error getting statements", te);
      }
    }
  }

  private void processQueue() {
    if (logger.isDebugEnabled())
      logger.debug("Processing update queue");

    // get our db session
    if (sess == null) {
      try {
        sess = (JRDFSession) sessFactory.newJRDFSession();
      } catch (QueryException qe) {
        logger.warn("Failed to get db session - retrying later", qe);
        return;
      }
    }

    // make a copy of the queue and clear it
    final Set subjects = new HashSet();
    synchronized (queue) {
      subjects.addAll(queue);
      queue.clear();
    }

    // process each subject
    for (Iterator iter = subjects.iterator(); iter.hasNext(); ) {
      URIReference subj = (URIReference) iter.next();
      try {
        updateSubject(subj, sess);
      } catch (Exception e) {
        logger.warn("Failed to update '" + subj + "' - requeueing for later", e);

        synchronized (queue) {
          queue.add(subj);
        }
      }
    }
  }

  private void updateSubject(URIReference subj, JRDFSession sess) throws Exception {
    if (logger.isDebugEnabled())
      logger.debug("Processing '" + subj + "'...");

    // get all statements for this subject
    Answer ans = sess.find(modelURI, subj, null, null);

    // create dublin-core and rels-ext docs
    ByteArrayOutputStream dcBaos = new ByteArrayOutputStream(200);
    ByteArrayOutputStream reBaos = new ByteArrayOutputStream(200);

    RIOTripleWriter rtw = new RIOTripleWriter(new SplitWriter(dcBaos, reBaos), new HashMap());
    KowariTripleIterator kti = new KowariTripleIterator(ans);
    rtw.write(kti);

    byte[] dcCont = dcBaos.toByteArray();
    byte[] reCont = reBaos.toByteArray();

    // get fedora object-id for subject
    String objId = subj.getURI().toString();
    if (!objId.startsWith(Constants.FEDORA.uri)) {
      logger.error("Invalid object-uri '" + objId + "' - must start with '" + Constants.FEDORA.uri +
                   "'");
      // don't throw an exception, because that will trigger a retry, and that is pointless...
      return;
    }
    objId = objId.substring(Constants.FEDORA.uri.length());

    // update dublin-core
    try {
      apim.modifyDatastreamByValue(objId, "DC", null, null, true, null, null,
                                   dcCont, null, "Modifed by FedoraUpdater", false);
    } catch (Exception e) {
      logger.debug("Object '" + objId + "' doesn't seem to exist - creating it", e);

      // Hmm, object doesn't exist, so create and try again
      createObject(objId, "RDF subject node", "RDF");
      apim.modifyDatastreamByValue(objId, "DC", null, null, true, null, null,
                                   dcCont, null, "Modifed by FedoraUpdater", false);
    }

    // update rels-ext
    try {
      apim.modifyDatastreamByValue(objId, "RELS-EXT", null, null, true, null, null,
                                   reCont, null, "Modifed by FedoraUpdater", false);
    } catch (RemoteException re) {
      // Ugh! What a hack...
      if (re.getMessage() == null || !re.getMessage().startsWith("java.lang.NullPointerException:"))
        throw re;

      logger.debug("RELS-EXT for object '" + objId + "' doesn't seem to exist - creating it", re);

      // Hmm, rels-ext doesn't exist, so create instead
      String reLoc = uploader.upload(new ByteArrayInputStream(reCont));
      apim.addDatastream(objId, "RELS-EXT", new String[0], "RDF statements", true, "text/xml",
                         null, reLoc, "X", "A", "Created by FedoraUpdater");
    }

    if (logger.isDebugEnabled())
      logger.debug("'" + subj + "' updated");
  }

  private void createObject(String objId, String label, String cModel) throws Exception {
    StringBuffer xml = new StringBuffer();
    xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
    xml.append("<foxml:digitalObject xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n");
    xml.append("           xmlns:foxml=\"info:fedora/fedora-system:def/foxml#\"\n");
    xml.append("           xsi:schemaLocation=\"info:fedora/fedora-system:def/foxml# http://www.fedora.info/definitions/1/0/foxml1-0.xsd\"\n");
    xml.append("           PID=\"" + xmlEscape(objId) + "\">\n");
    xml.append("  <foxml:objectProperties>\n");
    xml.append("    <foxml:property NAME=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#type\" VALUE=\"FedoraObject\"/>\n");
    xml.append("    <foxml:property NAME=\"info:fedora/fedora-system:def/model#label\" VALUE=\"" + xmlEscape(label) + "\"/>\n");
    xml.append("    <foxml:property NAME=\"info:fedora/fedora-system:def/model#contentModel\" VALUE=\"" + xmlEscape(cModel) + "\"/>\n");
    xml.append("  </foxml:objectProperties>\n");
    xml.append("</foxml:digitalObject>");
    String objXML = xml.toString();

    apim.ingest(objXML.getBytes("UTF-8"), "foxml1.0", "Created by FedoraUpdater");
  }

  private static final String xmlEscape(String in) {
    return in.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("\"", "&quot;").replaceAll("'", "&apos;");
  }

  private static URIReference toURINode(ResolverSession resolverSession, long node)
      throws ResolverException {
    Node globalNode = null;

    // Globalise the node
    try {
      globalNode = resolverSession.globalize(node);
    } catch (GlobalizeException ge) {
      throw new ResolverException("Couldn't globalize node " + node, ge);
    }

    // Check that our node is a URIReference
    if (!(globalNode instanceof URIReference))
      throw new ResolverException("Node parameter " + globalNode + " isn't a URI reference");

    return (URIReference) globalNode;
  }

  /**
   * This implements a writer that splits the statements into a DC and a RELS-EXT stream according
   * to whether the predicate is from DC or not. Furthermore, it completely ignores fedora system
   * predicates.
   */
  private static class SplitWriter implements RdfDocumentWriter {
    private static final String OAI_DC_PFX = "http://www.openarchives.org/OAI/2.0/oai_dc/";

    private final RdfDocumentWriter dcWriter;
    private final RdfDocumentWriter reWriter;

    public SplitWriter(OutputStream dcStream, OutputStream reStream) {
      dcWriter = new RdfXmlWriter(dcStream);
      reWriter = new RdfXmlWriter(reStream);
    }

    public void setNamespace(String prefix, String name) throws IOException {
      dcWriter.setNamespace(prefix, name);
      reWriter.setNamespace(prefix, name);
    }

    public void startDocument() throws IOException {
      dcWriter.startDocument();
      reWriter.startDocument();
    }

    public void endDocument() throws IOException {
      dcWriter.endDocument();
      reWriter.endDocument();
    }

    public void writeStatement(org.openrdf.model.Resource subject, org.openrdf.model.URI predicate,
                               org.openrdf.model.Value object) throws IOException {
      if (predicate.getNamespace().startsWith(Constants.FEDORA_SYSTEM_DEF_URI))
        ;       // ignore
      else if (predicate.getNamespace().startsWith(Constants.DC.uri) ||
               predicate.getNamespace().startsWith(OAI_DC_PFX))
        dcWriter.writeStatement(subject, predicate, object);
      else
        reWriter.writeStatement(subject, predicate, object);
    }

    public void writeComment(String comment) throws IOException {
      dcWriter.writeComment(comment);
      reWriter.writeComment(comment);
    }
  }

  private class Worker extends Thread {
    private final long interval;

    public Worker(long interval) {
      super("FedoraUpdate-Worker");
      setDaemon(true);
      this.interval = interval;
    }

    public void run() {
      while (true) {
        try {
          sleep(interval);
        } catch (InterruptedException ie) {
          logger.warn("Worker thread interrupted - exiting", ie);
          return;
        }

        processQueue();
      }
    }
  }
}
