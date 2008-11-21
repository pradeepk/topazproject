package org.topazproject.otm.stores;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.topazproject.otm.AbstractBlob;
import org.topazproject.otm.AbstractConnection;
import org.topazproject.otm.Blob;
import org.topazproject.otm.BlobStore;
import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.Connection;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.Session;

public abstract class FileBackedBlobStore implements BlobStore {
  protected static char[] hex = "01234567890abcdef".toCharArray();
  protected File root;
  private int nextId = 1;
  private List<File> txns = new ArrayList<File>();
  protected ReadWriteLock storeLock = new ReentrantReadWriteLock();

  protected FileBackedBlobStore(File root) throws OtmException {
    try {
      if (!root.exists())
        root.mkdirs();

      if (!root.exists())
        throw new IOException("Failed to create " + root);

      if (!root.isDirectory())
        throw new IOException("Not a directory " + root);
    } catch (Throwable t) {
      throw new OtmException("Invalid root directory " + root, t);
    }
    this.root = root;
  }


  protected File allocateTxn() {
    synchronized (txns) {
      return (txns.size() > 0) ? txns.remove(0) : new File(root, "txn" + nextId++);
    }

  }

  /**
   * Return back the txn directory.
   *
   * @param txn the txn temp directory
   */
  protected void returnTxn(File txn) {
    synchronized (txns) {
      txns.add(txn);
    }
  }


  public void flush(Connection con) throws OtmException {
  }


  public Blob getBlob(ClassMetadata cm, String id, Object blob, Connection con) throws OtmException {
    FileBackedBlobStoreConnection ebsc = (FileBackedBlobStoreConnection) con;
    return ebsc.get(cm, id, blob);
  }

  public static abstract class FileBackedBlobStoreConnection extends AbstractConnection {
    protected final FileBackedBlobStore store;
    protected File                  txn;
    protected Map<String, FileBackedBlob> blobs = new HashMap<String, FileBackedBlob>();

    public FileBackedBlobStoreConnection(FileBackedBlobStore store, Session sess) throws OtmException {
      super(sess);
      this.store = store;
      this.txn = store.allocateTxn();

      enlistResource(newXAResource());
    }

    protected String normalize(String id) throws OtmException {
      try {
        URI uri = new URI(id);

        return uri.normalize().toString();
      } catch (Exception e) {
        throw new OtmException("Blob id must be a valid URI. id = " + id, e);
      }
    }

    protected File toFile(File baseDir, String normalizedId, String suffix) throws OtmException {
      try {
        byte[] input = normalizedId.getBytes("UTF-8");
        byte[] digest = MessageDigest.getInstance("SHA-1").digest(input);
        assert digest.length == 20; // 160-bit digest

        File f = baseDir;
        int idx = 0;

        for (int i = 0; i < 4; i++) {
          StringBuilder sb = new StringBuilder();

          for (int j = 0; j < 5; j++) {
            byte b = digest[idx++];
            sb.append(hex[(b >>> 4) & 0x0F]);
            sb.append(hex[b & 0x0F]);
          }

          if (i < 3)
            f = new File(f, sb.toString());
          else {
            f.mkdirs();

            if (!f.exists())
              throw new IOException("Failed to create directory " + f);

            f = new File(f, sb.append(suffix).toString());
          }
        }

        return f;
      } catch (Exception e) {
        throw new OtmException("Failed to map id to a file under " + baseDir, e);
      }
    }

    public FileBackedBlob get(ClassMetadata cm, String id, Object instance) throws OtmException {
      if (txn == null)
        throw new OtmException("Attempt to use a connection that is closed");

      id = normalize(id);

      FileBackedBlob blob = blobs.get(id);

      if (blob != null)
        return blob;

      File work = toFile(txn, id, ".dat");

      if (work.exists())
        work.delete();

      if (work.exists())
        throw new Error("Failed to delete file:" + work);

      blob = doGetBlob(cm, id, instance, work);

      blobs.put(id, blob);

      return blob;
    }

    protected abstract FileBackedBlob doGetBlob(ClassMetadata cm, String id, Object instance, File work) throws OtmException;

    private XAResource newXAResource() {
      return new XAResource() {
        public void commit(Xid xid, boolean onePhase) throws XAException {
          if (onePhase) {
            try {
              doPrepare();
            } catch (XAException xae) {
              doRollback();
              throw xae;
            }
          }
          doCommit();
        }

        public void rollback(Xid xid) throws XAException {
          doRollback();
        }

        public int prepare(Xid xid) throws XAException {
          // assumption: doPrepare only throws RMERR, which means this resource stays in S2
          if (doPrepare()) {
            return XA_OK;
          } else {
            close();
            return XA_RDONLY;
          }
        }

        public void start(Xid xid, int flags) {
        }

        public void end(Xid xid, int flags) {
        }

        public Xid[] recover(int flag) {
          return new Xid[0];
        }

        public void forget(Xid xid) {
        }

        public boolean isSameRM(XAResource xares) {
          return xares == this;
        }

        public int getTransactionTimeout() {
          return 0;
        }

        public boolean setTransactionTimeout(int secs) {
          return false;
        }
      };
    }

    private boolean doPrepare() throws XAException {
      try {
        int mods = 0;
        // Note: we keep the lock acquired thru the commit phase.
        store.storeLock.writeLock().lock();
        for (FileBackedBlob blob : blobs.values())
          if (blob.prepare())
            mods++;
        return (mods > 0);
      } catch (Exception oe) {
        throw (XAException) new XAException(XAException.XAER_RMERR).initCause(oe);
      }
    }

    private void doCommit() throws XAException {
      boolean abort = true;
      try {
        // Note: lock acquired in doPrepare().
        for (FileBackedBlob blob : blobs.values())
          blob.commit();
        abort = false;
      } catch (Exception oe) {
        throw (XAException) new XAException(XAException.XAER_RMERR).initCause(oe);
      } finally {
        cleanup(abort);
      }
    }

    private void doRollback() {
      store.storeLock.writeLock().lock();
      cleanup(true);
    }

    private void cleanup(boolean abort) {
      try {
        for (FileBackedBlob blob : blobs.values())
          blob.cleanup(abort);

        blobs.clear();

        if (txn != null)
          store.returnTxn(txn);

        txn = null;
      } finally {
        store.storeLock.writeLock().unlock();
      }
    }

    public void close() {
      if (txn != null)
        doRollback();
    }
  }

  public abstract static class FileBackedBlob extends AbstractBlob {
    protected Boolean exists = null;
    protected ChangeState state = ChangeState.NONE;
    protected ChangeState undo = ChangeState.NONE;
    protected final File tmp;
    protected final File bak;

    public FileBackedBlob(String id, File tmp, File bak) {
      super(id);
      this.tmp = tmp;
      this.bak = bak;
    }

    public ChangeState getChangeState() {
      return state;
    }

    // TODO: remove this hack
    public byte[] readAll(boolean original) throws OtmException {
      if (!original || ((state != ChangeState.DELETED) && (state != ChangeState.WRITTEN)))
        return readAll();
      File f = null;
      try {
        f = File.createTempFile("search-tmp", ".dat");

        if (log.isTraceEnabled())
          log.trace("Loading old data for search index removal for " + getId() + " from " + f );

        copyFromStore(f, true);
        ByteArrayOutputStream out = new ByteArrayOutputStream((int) f.length());
        copy(new FileInputStream(f), out);
        return out.toByteArray();
      } catch (IOException e) {
        throw new OtmException("Failed to read from store: " + getId(), e);
      } finally {
        if (f != null)
          f.delete();
      }
    }

    @Override
    public InputStream doGetInputStream() throws OtmException {
      if (!exists())
        throw new OtmException("Blob " + getId() + " does not exist. Write to it first to create.");

      if (log.isTraceEnabled())
        log.trace("Creating inputstream for " + getId() + " from " + tmp);

      try {
        return new FileInputStream(tmp);
      } catch (IOException e) {
        throw new OtmException("Failed to open file: " + tmp + " for " + getId(), e);
      }
    }

    @Override
    public OutputStream doGetOutputStream() throws OtmException {
      if (state == ChangeState.DELETED)
        throw new OtmException("Blob " + getId() + " is deleted. Cannot write to it.");

      create();

      if (log.isTraceEnabled())
        log.trace("Creating outputstream for " + getId() + " from " + tmp);

      try {
        return new FileOutputStream(tmp);
      } catch (IOException e) {
        throw new OtmException("Failed to open file: " + tmp + " for " + getId(), e);
      }
    }

    @Override
    public void closed(Closeable stream) {
      if (log.isTraceEnabled())
        log.trace("Closing stream for " + getId() + " on " + tmp);
      super.closed(stream);
    }

    @Override
    public void writing(OutputStream out) {
      switch (state) {
        case CREATED:
        case NONE:
          if (log.isTraceEnabled())
            log.trace("Write detected on " + tmp + " for " + getId());
          state = ChangeState.WRITTEN;
          break;
        case DELETED:
          // WTF? All streams should be closed on delete.
          log.error("Writing to deleted Blob file: " + tmp + " for " + getId());
      }
    }

    public boolean create() throws OtmException {
      switch (state) {
        case DELETED:
        case NONE:
          if (log.isTraceEnabled())
            log.trace("Creating " + tmp + " for " + getId());
          try {
            boolean ret = tmp.createNewFile();
            state = ChangeState.CREATED;
            exists = Boolean.TRUE;
            return ret;
          } catch (IOException e) {
            throw new OtmException("Failed to create file: " + tmp + " for " + getId(), e);
          }
      }
      return false;
    }

    public boolean delete() throws OtmException {
      switch (state) {
        case CREATED:
        case NONE:
        case WRITTEN:
          for (Closeable stream : streams) {
            try {
              stream.close();
            } catch (IOException e) {
              log.warn("Failed to close a stream : " + tmp + " for " + getId(), e);
            }
          }
          streams.clear();
          if (log.isTraceEnabled())
            log.trace("Deleting " + tmp + " for " + getId());
          boolean ret = tmp.delete();
          state = ChangeState.DELETED;
          exists = Boolean.FALSE;
          return ret;
      }
      return false;
    }

    protected abstract boolean createInStore() throws OtmException;
    protected abstract boolean copyFromStore(File to, boolean asBackup) throws OtmException;
    protected abstract boolean moveToStore(File from) throws OtmException;
    protected abstract boolean deleteFromStore() throws OtmException;

    public boolean exists() throws OtmException {
      if (exists != null)
        return exists;

      exists = copyFromStore(tmp, false);

      return exists;
    }

    public boolean prepare() throws OtmException {
      if (log.isTraceEnabled())
        log.trace("Preparing to commit " + getId());

      close();
      switch (state) {
        case DELETED:
        case WRITTEN:
          backup();
      }

      return state != ChangeState.NONE;
    }

    public void commit() throws OtmException {
      boolean success = true;
      String operation = "no-op";
      switch (state) {
        case DELETED:
          operation = "delete";
          success = deleteFromStore();
          break;
        case CREATED:
          operation = "create";
          success = createInStore();
          break;
        case WRITTEN:
          operation = "save";
          success = moveToStore(tmp);
          break;
      }

      if (!success)
        throw new OtmException("Commit(" + operation + ") failed for " + getId());
      else {
        if (log.isDebugEnabled())
          log.debug("Committed(" + operation + ") on " + getId());
        exists = null;
        undo = state;
        state = ChangeState.NONE;
      }
    }

    public void cleanup(boolean abort) {
      if (abort && ((undo == ChangeState.DELETED) || (undo == ChangeState.WRITTEN)))
        restore();

      undo = ChangeState.NONE;
      exists = null;
      tmp.delete();
    }

    private void backup() throws OtmException {
      if (bak.exists()) {
        if (log.isDebugEnabled())
          log.debug("Deleting old backup for " + getId());
        bak.delete();
        if (bak.exists())
          throw new OtmException("Failed to delete old backup at " + bak);
      }

      copyFromStore(bak, true);
    }

    private void restore() {
      if (!bak.exists())
        return;

      if (log.isDebugEnabled())
        log.debug("Restoring " + bak + " for id " + getId());

      try {
        if (!moveToStore(bak))
          log.error("Failed to restore from backup " + bak + " for id " + getId());
        else
          log.warn("Restored from backup " + bak + " for id " + getId());
      } catch (OtmException e) {
        log.error("Failed to restore from backup " + bak + " for id " + getId(), e);
      }
    }
  }
}
