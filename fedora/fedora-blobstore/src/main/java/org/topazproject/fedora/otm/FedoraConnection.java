/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007-2008 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.topazproject.fedora.otm;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import java.rmi.RemoteException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.fedora.client.Datastream;
import org.topazproject.fedora.client.FedoraAPIA;
import org.topazproject.fedora.client.FedoraAPIM;
import org.topazproject.fedora.client.Uploader;
import org.topazproject.fedora.otm.FedoraBlob.INGEST_OP;

import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.Session;
import org.topazproject.otm.stores.FileBackedBlobStore;
import org.topazproject.otm.stores.FileBackedBlobStore.FileBackedBlob;
import org.topazproject.otm.stores.FileBackedBlobStore.FileBackedBlobStoreConnection;

/**
 * Acts as the client to Fedora and manages the transactional aspects of user operations.
 *
 * @author Pradeep Krishnan
 */
public class FedoraConnection extends FileBackedBlobStoreConnection {
  private static final Log    log    = LogFactory.getLog(FedoraConnection.class);
  private FedoraAPIM              apim;
  private FedoraAPIA              apia;
  private Uploader                upld;

  /**
   * Creates a new FedoraConnection object.
   *
   * @param bs   the FedoraBlobStore that this connects to
   * @param sess the session this connection is attached to
   * @throws OtmException on an error
   */
  public FedoraConnection(FedoraBlobStore bs, Session sess) throws OtmException {
    super(bs, sess);
  }

  /**
   * Get the underlying fedora blob-store.
   *
   * @return the blob-store, or null if this connection has been closed
   */
  FedoraBlobStore getBlobStore() {
    return (FedoraBlobStore) super.store;
  }

    /**
   * Gets the API-A stub used by this connection.
   *
   * @return the API-A stub
   */
  public FedoraAPIA getAPIA() {
    if (apia == null)
      apia = getBlobStore().createAPIA();

    return apia;
  }

  /**
   * Gets the API-M stub used by this connection.
   *
   * @return the API-M stub
   */
  public FedoraAPIM getAPIM() {
    if (apim == null)
      apim = getBlobStore().createAPIM();

    return apim;
  }

  /**
   * Gets the Fedora uploader used by this connection.
   *
   * @return the Fedora uploader
   */
  public Uploader getUploader() {
    if (upld == null)
      upld = getBlobStore().createUploader();

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
      return getBlobStore().getFedoraBaseUri().resolve("get/" + pid + "/" + dsId).toURL();
    } catch (Exception e) {
      throw new OtmException("Failed to build a data-stream access URL", e);
    }
  }

  @Override
  protected FileBackedBlob doGetBlob(ClassMetadata cm, String id, Object instance, File work) throws OtmException {
    FedoraBlobStore bs = getBlobStore();
    FedoraBlobFactory bf   = bs.mostSpecificBlobFactory(id);

    if (bf == null)
      throw new OtmException("Can't find a blob factory for " + id);

    FedoraBlob fb = bf.createBlob(cm, id, instance, this);
    File bak = toFile(bs.getBackupRoot(), id, ".bak");

    return new FileBackedFedoraBlob(fb, id, work, bak);
  }

  /**
   * Ingest the contents into Fedora.
   *
   * @param blob the blob contents
   * @param ref the uploaded content reference
   *
   * @throws OtmException on an error
   */
  private boolean ingest(FedoraBlob blob, String ref) throws OtmException {
    String[]   newPid = new String[] { blob.getPid() };
    String[]   newDs  = new String[] { blob.getDsId() };

    try {
      INGEST_OP op = blob.getFirstIngestOp();
      int maxIter = 3;
      while (op != null && maxIter-- > 0) {
        switch (op) {
          case AddObj:
            op = addObject(blob, ref, newPid);
            break;

          case AddDs:
            op = addDatastream(blob, ref, newDs);
            break;

          case ModDs:
            op = modifyDatastream(blob, ref);
            break;

          default:
            throw new Error("Internal error: unexpected op " + op);
        }
      }

      if (op != null)
        throw new OtmException("Loop detected: failed to ingest " + blob.getBlobId() + ", pid=" + blob.getPid() +
                               ", dsId=" + blob.getDsId() + ", op=" + op);

    } catch (Exception e) {
      if (e instanceof OtmException)
        throw (OtmException) e;

      throw new OtmException("Write to Fedora failed", e);
    }

    if (!blob.getPid().equals(newPid[0]))
      throw new OtmException("PID mismatch in ingest. Expecting '" + blob.getPid() + "', got '" + newPid[0]
                             + "'");

    if (!blob.getDsId().equals(newDs[0]))
      throw new OtmException("DS-ID mismatch in add-DS. Expecting '" + blob.getDsId() + "', got '" + newDs[0]
                             + "'");

    if (log.isDebugEnabled())
      log.debug("Wrote " + blob.getBlobId() + " as " + blob.getPid() + "/" + blob.getDsId());

    return true;
  }

  private INGEST_OP addObject(FedoraBlob blob, String ref, String[] newPid) throws Exception {
    INGEST_OP new_op;

    try {
      if (log.isDebugEnabled())
        log.debug("Ingesting '" + blob.getPid() + "' with data-stream '" + blob.getDsId() + "'");

      newPid[0] = apim.ingest(blob.getFoxml(ref), "foxml1.0", "created");
      new_op = null;
    } catch (Exception e) {
      if (log.isDebugEnabled())
        log.debug("ingest failed: ", e);

      if (isObjectExistsException(e))
        new_op = INGEST_OP.AddDs;
      else
        throw e;
    }

    return new_op;
  }

  private INGEST_OP addDatastream(FedoraBlob blob, String ref, String[] newDs) throws Exception {
    INGEST_OP new_op;

    try {
      if (log.isDebugEnabled())
        log.debug("Adding data-stream '" + blob.getDsId() + "' for '" + blob.getPid() + "'");

      newDs[0] = apim.addDatastream(blob.getPid(), blob.getDsId(), new String[0], blob.getDatastreamLabel(), false,
                                    blob.getContentType(), null, ref, "M", "A", "created");
      new_op = null;
    } catch (Exception e) {
      if (log.isDebugEnabled())
        log.debug("add datastream failed: ", e);

      if (isNoSuchObjectException(e))
        new_op = INGEST_OP.AddObj;
      else if (isDatastreamExistsException(e))
        new_op = INGEST_OP.ModDs;
      else
        throw e;
    }

    return new_op;
  }

  private INGEST_OP modifyDatastream(FedoraBlob blob, String ref) throws Exception {
    INGEST_OP new_op;

    try {
      if (log.isDebugEnabled())
        log.debug("Modifying data-stream(by reference) '" + blob.getDsId() + "' for '" + blob.getPid() + "'");

      apim.modifyDatastreamByReference(blob.getPid(), blob.getDsId(), new String[0], blob.getDatastreamLabel(), false,
                                       blob.getContentType(), null, ref, "A", "updated", false);
      new_op = null;
    } catch (Exception e) {
      if (log.isDebugEnabled())
        log.debug("ds-modify failed: ", e);

      if (isNoSuchObjectException(e))
        new_op = INGEST_OP.AddObj;
      else if (isNoSuchDatastreamException(e))
        new_op = INGEST_OP.AddDs;
      else
        throw e;
    }

    return new_op;
   }


  /* WARNING: this is fedora version specific! */
  private static boolean isNoSuchObjectException(Exception e) {
    return e instanceof RemoteException &&
           e.getMessage().startsWith("fedora.server.errors.ObjectNotInLowlevelStorageException");
  }

  /* WARNING: this is fedora version specific! */
  private static boolean isNoSuchDatastreamException(Exception e) {
    return e instanceof RemoteException &&
           e.getMessage().startsWith("java.lang.Exception: Uncaught exception from Fedora Server");
  }

  /* WARNING: this is fedora version specific! */
  private static boolean isObjectExistsException(Exception e) {
    return e instanceof RemoteException &&
           e.getMessage().startsWith("fedora.server.errors.ObjectExistsException");
  }

  /* WARNING: this is fedora version specific! */
  private static boolean isDatastreamExistsException(Exception e) {
    return e instanceof RemoteException &&
           e.getMessage().startsWith("fedora.server.errors.GeneralException: A datastream already exists");
  }

  /**
   * Gets the data-stream meta object from Fedora.
   *
   * @param blob the fedora blob
   *
   * @return the data-stream meta object
   *
   * @throws OtmException on an error
   */
  protected Datastream getDatastream(FedoraBlob blob) throws OtmException {
    try {
      return getAPIM().getDatastream(blob.getPid(), blob.getDsId(), null);
    } catch (Exception e) {
      if (isNoSuchObjectException(e) || isNoSuchDatastreamException(e))
        return null;

      throw new OtmException("Error while getting the data-stream for " + blob.getPid() + "/" + blob.getDsId(), e);
    }
  }

  /**
   * Checks if the Fedora object itself can be purged. In general an object can be purged if
   * the remaining data-streams on it are not significant. Override it in sub-classes to handle
   * app specific processing.
   *
   * @param blob the fedora blob
   *
   * @return true if it can be purged, false if only the datastream should be purged, or null
   *         if nothing should be done (e.g. because the object doesn't exist in the first place)
   *
   * @throws OtmException on an error
   */
  protected Boolean canPurgeObject(FedoraBlob blob) throws OtmException {
    try {
      if (blob.hasSingleDs())
        return true;
      return blob.canPurgeObject(getAPIM().getDatastreams(blob.getPid(), null, null));
    } catch (Exception e) {
      if (isNoSuchObjectException(e)) {
        if (log.isDebugEnabled())
          log.debug("Object " + blob.getBlobId()+ " at " + blob.getPid() + " doesn't exist in blob-store", e);
        return null;
      }

      throw new OtmException("Error in obtaining the list of data-streams on " + blob.getPid(), e);
    }
  }


  /**
   * Purge this Blob from Fedora.
   *
   * @param blob the blob to purge
   *
   * @throws OtmException on an error
   */
  private boolean purge(FedoraBlob blob) throws OtmException {
    FedoraAPIM apim = getAPIM();

    try {
      Boolean canPurgeObject = canPurgeObject(blob);
      if (canPurgeObject == null) {
        if (log.isDebugEnabled())
          log.debug("Not purging Object or datastram for " + blob.getBlobId() + " at " + blob.getPid()+ "/" + blob.getDsId());
        return false;
      }

      if (canPurgeObject) {
        if (log.isDebugEnabled())
          log.debug("Purging Object " + blob.getBlobId() + " at " + blob.getPid() + "/" + blob.getDsId());

        apim.purgeObject(blob.getPid(), "deleted", false);
      } else {
        if (log.isDebugEnabled())
          log.debug("Purging Datastream " + blob.getBlobId() + " at " + blob.getPid() + "/" + blob.getDsId());

        apim.purgeDatastream(blob.getPid(), blob.getDsId(), null, "deleted", false);
      }

      return true;
    } catch (Exception e) {
      if (!isNoSuchObjectException(e))
        throw new OtmException("Purge failed", e);

      if (log.isDebugEnabled())
        log.debug("Datastream " + blob.getBlobId() + " at " + blob.getPid() + "/" + blob.getDsId() +
                  " doesn't exist in blob-store", e);

      return false;
    }
  }

  /**
   * Gets the blob's InputStream from Fedora.
   *
   * @param blob the blob
   *
   * @return the InputStream or null
   *
   * @throws OtmException on an error
   */
  private InputStream getBlobStream(FedoraBlob blob) throws OtmException {
    Datastream stream = getDatastream(blob);

    if (stream == null)
      return null;

    URL  location = getDatastreamLocation(blob.getPid(), blob.getDsId());

    try {
      return location.openStream();
    } catch (Exception e) {
      if (log.isDebugEnabled())
        log.debug("Error while opening a stream to read from " + blob.getPid() + "/" + blob.getDsId()
                  + ". According to Fedora the stream must exist - but most likeley was"
                  + " purged recently. Treating this as if it was purged and does not exist.", e);

      return null;
    }
  }

  private class FileBackedFedoraBlob extends FileBackedBlobStore.FileBackedBlob {
    private final FedoraBlob fb;
    private String ref = null;

    public FileBackedFedoraBlob(FedoraBlob fb, String id, File tmp, File bak) {
      super(id, tmp, bak);
      this.fb = fb;
    }

    @Override
    protected boolean copyFromStore(OutputStream out, boolean asBackup) throws OtmException {
      InputStream in = getBlobStream(fb);
      if (in == null)
        return false;

      try {
        copy(in, out);
      } catch (IOException e) {
        throw new OtmException("Copy failed for " + getId(), e);
      } finally {
        closeAll(in);
      }

      return true;
    }

    @Override
    protected boolean createInStore() throws OtmException {
      return true;
    }

    @Override
    protected boolean deleteFromStore() throws OtmException {
      return purge(fb);
    }

    @Override
    public boolean prepare() throws OtmException {
      if (!super.prepare())
        return false;

      /*
       * Upload in prepare phase. Important for large blobs since this
       * is the most error prone part of the process.
       */
      ref = null;
      switch(getChangeState()) {
        case CREATED:
        case WRITTEN:
           try {
             if (log.isDebugEnabled())
               log.debug("Uploading blob to fedora: " + tmp + " for " + getId());
             ref = getUploader().upload(tmp);
           } catch (IOException e) {
             throw new OtmException("Failed to upload: " + tmp + " for " + getId(), e);
           }
      }

      return true;
    }

    @Override
    protected boolean moveToStore(File from) throws OtmException {
      String r = ref;
      ref = null;

      if ((from != tmp) || (r == null)) {
        try {
          if (log.isDebugEnabled())
            log.debug("Uploading blob to fedora: " + from + " for " + getId());
          r = getUploader().upload(tmp);
        } catch (IOException e) {
          throw new OtmException("Failed to upload: " + from + " for " + getId(), e);
        }
      }

      return ingest(fb, r);
    }
  }
}
