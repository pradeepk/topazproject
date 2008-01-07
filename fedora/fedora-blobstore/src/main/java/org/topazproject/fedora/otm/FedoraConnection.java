/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.fedora.otm;

import java.net.URL;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.fedora.client.FedoraAPIA;
import org.topazproject.fedora.client.FedoraAPIM;
import org.topazproject.fedora.client.Uploader;

import org.topazproject.otm.AbstractConnection;
import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.OtmException;

/**
 * Acts as the client to Fedora and manages the transactional aspects of user operations.
 *
 * @author Pradeep Krishnan
 */
public class FedoraConnection extends AbstractConnection {
  private static final Log        log        = LogFactory.getLog(FedoraConnection.class);
  private FedoraBlobStore         bs;
  private Map<String, FedoraBlob> undoI;
  private Map<String, FedoraBlob> undoD;
  private Map<String, FedoraBlob> insertMap  = new HashMap<String, FedoraBlob>();
  private Map<String, FedoraBlob> deleteMap  = new HashMap<String, FedoraBlob>();
  private Map<String, byte[]>     contentMap = new HashMap<String, byte[]>();
  private FedoraAPIM              apim;
  private FedoraAPIA              apia;
  private Uploader                upld;

/**
   * Creates a new FedoraConnection object.
   *
   * @param bs the FedoraBlobStore that this connects to
   */
  public FedoraConnection(FedoraBlobStore bs) {
    this.bs                                  = bs;
  }

  /*
   * inherited javadoc
   */
  public void insert(ClassMetadata cm, String id, byte[] blob)
              throws OtmException {
    if (bs == null)
      throw new OtmException("Attempt to use a connection that is closed");

    insertMap.put(id, bs.toBlob(cm, id));
    deleteMap.remove(id);
    contentMap.put(id, blob);
  }

  /*
   * inherited javadoc
   */
  public void delete(ClassMetadata cm, String id) throws OtmException {
    if (bs == null)
      throw new OtmException("Attempt to use a connection that is closed");

    deleteMap.put(id, bs.toBlob(cm, id));
    insertMap.remove(id);
  }

  /*
   * inherited javadoc
   */
  public byte[] get(ClassMetadata cm, String id) throws OtmException {
    if (bs == null)
      throw new OtmException("Attempt to use a connection that is closed");

    if (deleteMap.get(id) != null)
      return null;

    byte[] blob = contentMap.get(id);

    if (blob == null) {
      blob = bs.toBlob(cm, id).get(this);

      if (blob != null)
        contentMap.put(id, blob);
    }

    return blob;
  }

  /**
   * Gets the API-A stub used by this connection.
   *
   * @return the API-A stub
   */
  public FedoraAPIA getAPIA() {
    if (apia == null)
      apia = bs.createAPIA();

    return apia;
  }

  /**
   * Gets the API-M stub used by this connection.
   *
   * @return the API-M stub
   */
  public FedoraAPIM getAPIM() {
    if (apim == null)
      apim = bs.createAPIM();

    return apim;
  }

  /**
   * Gets the Fedora uploader used by this connection.
   *
   * @return the Fedora uploader
   */
  public Uploader getUploader() {
    if (upld == null)
      upld = bs.createUploader();

    return upld;
  }

  /**
   * Create a URL to access the given Datastream.
   *
   * @param pid the Fedora PID
   * @param dsId the Fedora dsId
   *
   * @return the URL
   *
   * @throws OtmException on an error
   */
  public URL getDatastreamLocation(String pid, String dsId)
                            throws OtmException {
    try {
      return bs.getFedoraBaseUri().resolve("get/" + pid + "/" + dsId).toURL();
    } catch (Exception e) {
      throw new OtmException("Failed to build a data-stream access URL", e);
    }
  }

  /*
   * inherited javadoc
   */
  protected void doPrepare() throws OtmException {
    undoI = new HashMap<String, FedoraBlob>();
    ingest(insertMap, undoI);
    insertMap = null;
  }

  /*
   * inherited javadoc
   */
  protected void doCommit() throws OtmException {
    try {
      undoD = new HashMap<String, FedoraBlob>();
      purge(deleteMap, undoD);
      undoD   = null;
      undoI   = null;
    } finally {
      close();
    }
  }

  /*
   * inherited javadoc
   */
  protected void doRollback() throws OtmException {
    close();
  }

  private void close() {
    // undo deletes
    if (undoD != null) {
      if (undoD.size() > 0)
        log.warn("Undoing purges for " + undoD.keySet());

      ingest(undoD);
      undoD = null;
    }

    // undo inserts
    if (undoI != null) {
      if (undoI.size() > 0)
        log.warn("Undoing ingests for " + undoI.keySet());

      purge(undoI);
      undoI = null;
    }

    insertMap    = null;
    contentMap   = null;
    deleteMap    = null;
    bs           = null;
  }

  private Map<String, FedoraBlob> ingest(Map<String, FedoraBlob> map, Map<String, FedoraBlob> undo)
                                  throws OtmException {
    OtmException error = null;

    for (String id : map.keySet()) {
      try {
        FedoraBlob blob = map.get(id);
        blob.ingest(contentMap.get(id), this);

        if (undo != null)
          undo.put(id, blob);
      } catch (Throwable t) {
        OtmException e;

        if (t instanceof OtmException)
          e = (OtmException) t;
        else
          e = new OtmException("Failed to ingest blob for " + id, t);

        if (undo != null)
          throw e;
        else
          log.warn("Failed to ingest blob for " + id, t);

        if (error == null)
          error = e;
      }
    }

    if (error != null)
      throw error;

    return undo;
  }

  private Map<String, FedoraBlob> purge(Map<String, FedoraBlob> map, Map<String, FedoraBlob> undo)
                                 throws OtmException {
    OtmException error = null;

    for (String id : map.keySet()) {
      try {
        FedoraBlob blob = map.get(id);
        blob.purge(this);

        if (undo != null)
          undo.put(id, blob);
      } catch (Throwable t) {
        OtmException e;

        if (t instanceof OtmException)
          e = (OtmException) t;
        else
          e = new OtmException("Failed to purge blob for " + id, t);

        if (undo != null)
          throw e;
        else
          log.warn("Failed to purge blob for " + id, t);

        if (error == null)
          error = e;
      }
    }

    if (error != null)
      throw error;

    return undo;
  }

  private void ingest(Map<String, FedoraBlob> map) {
    try {
      ingest(map, null);
    } catch (Throwable t) {
    }
  }

  private void purge(Map<String, FedoraBlob> map) {
    try {
      purge(map, null);
    } catch (Throwable t) {
    }
  }
}
