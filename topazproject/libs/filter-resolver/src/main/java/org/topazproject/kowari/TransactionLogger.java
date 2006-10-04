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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FilenameFilter;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.List;
import java.util.Properties;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.apache.log4j.Logger;
import org.jrdf.graph.Node;
import org.jrdf.graph.Literal;
import org.jrdf.graph.URIReference;
import org.kowari.query.TuplesException;
import org.kowari.query.rdf.BlankNodeImpl;
import org.kowari.resolver.spi.GlobalizeException;
import org.kowari.resolver.spi.ResolverException;
import org.kowari.resolver.spi.ResolverSession;
import org.kowari.resolver.spi.Statements;

/** 
 * This logs all transactions (committed statements) to a log file.
 * 
 * Configuration properties:
 * <dl>
 *   <dt>topaz.fr.transactionLogger.log.fileName</dt>
 *   <dd>the log file name (required); old versions (if enabled) will have .N appended</dd>
 *   <dt>topaz.fr.transactionLogger.log.maxSize</dt>
 *   <dd>the maximum size, in bytes, before the log file will be rotated (optional). Note that
 *       the actual file size may exceed this slightly, and may exceed this significantly if
 *       an error occurs rotating the log files.</dd>
 *   <dt>topaz.fr.transactionLogger.log.maxAge</dt>
 *   <dd>the maximum age, in seconds, before a log file will be rotated (optional). Note that
 *       the actual age may exceed this slightly, and may exceed this significantly if an error
 *       occurs rotating the log files.</dd>
 *   <dt>topaz.fr.transactionLogger.flushInterval</dt>
 *   <dd>how often, in milliseconds, to flush the buffers to disk (optional). If not specified
 *       this default to 30 seconds.</dd>
 * </dl>
 *
 * @author Ronald Tschalär
 */
class TransactionLogger extends AbstractFilterHandler {
  private static final Logger logger = Logger.getLogger(TransactionLogger.class);

  private final List            updQueue = new ArrayList();
  private final Map             txQueue  = new HashMap();
  private       boolean         newUpdPending;
  private final XAResource      xaResource;
  private final Worker          worker;
  private final TxLog           txLog;
  private       Xid             currentTxId;

  /** 
   * Create a new logger instance. 
   * 
   * @param config  the configuration to use
   * @param dbURI   ignored
   * @throws IOException 
   */
  public TransactionLogger(Properties config, URI dbURI) throws IOException {
    String fileName  = config.getProperty("topaz.fr.transactionLogger.log.fileName");
    String maxSize   = config.getProperty("topaz.fr.transactionLogger.log.maxSize");
    String maxAge    = config.getProperty("topaz.fr.transactionLogger.log.maxAge");
    String flushIval = config.getProperty("topaz.fr.transactionLogger.flushInterval");
    String bufSize   = config.getProperty("topaz.fr.transactionLogger.writeBufferSize");

    if (fileName == null)
      throw new IllegalArgumentException("Missing config entry 'topaz.fr.transactionLogger.log.fileName'");

    txLog = new TxLog(fileName, maxSize != null ? Long.parseLong(maxSize) : -1L,
                      maxAge != null ? Long.parseLong(maxAge) : -1L,
                      bufSize != null ? Integer.parseInt(bufSize) : -1);

    xaResource = new LoggerXAResource();

    worker = new Worker(flushIval != null ? Long.parseLong(flushIval) : 30000L);
    worker.start();
  }

  public void modelCreated(URI filterModel, URI realModel) throws ResolverException {
    queue("create <" + filterModel + "> <" + FilterResolver.MODEL_TYPE + ">;\n");
  }

  public void modelRemoved(URI filterModel, URI realModel) {
    queue("drop <" + filterModel + "> <" + FilterResolver.MODEL_TYPE + ">;\n");
  }

  public void modelModified(URI filterModel, URI realModel, Statements stmts, boolean occurs,
                            ResolverSession resolverSession) throws ResolverException {
    StringBuffer sb = new StringBuffer(500);
    sb.append(occurs ? "insert " : "delete ");

    try {
      stmts.beforeFirst();
      while (stmts.next()) {
        try {
          String s = toString(resolverSession, stmts.getSubject());
          String p = toString(resolverSession, stmts.getPredicate());
          String o = toString(resolverSession, stmts.getObject());

          sb.append(s).append(" ").append(p).append(" ").append(o).append(" ");
        } catch (ResolverException re) {
          logger.error("Error getting statement", re);
        }
      }
    } catch (TuplesException te) {
      throw new ResolverException("Error getting statements", te);
    }

    sb.append(occurs ? "into <" : "from <").append(filterModel).append(">;\n");
    queue(sb.toString());
  }

  private static String toString(ResolverSession resolverSession, long node)
      throws ResolverException {
    Node globalNode = null;

    // Globalise the node
    try {
      globalNode = resolverSession.globalize(node);
    } catch (GlobalizeException ge) {
      throw new ResolverException("Couldn't globalize node " + node, ge);
    }

    // Turn it into a string
    if (globalNode instanceof URIReference)
      return "<" + ((URIReference) globalNode).getURI() + ">";

    if (globalNode instanceof Literal) {
      Literal l = (Literal) globalNode;
      String val = l.getLexicalForm().replaceAll("\\\\", "\\\\\\\\").replaceAll("'", "\\\\'");
      val = "'" + val + "'";
        if (l.getDatatypeURI() != null)
            val += "^^<" + l.getDatatypeURI() + ">";
        else if (!l.getLanguage().equals(""))
            val += "@" + l.getLanguage();
        return val;
    }

    if (globalNode instanceof BlankNodeImpl)
      return "$_" + ((BlankNodeImpl) globalNode).getNodeId();

    throw new ResolverException("Unsupported node type " + globalNode.getClass().getName());
  }

  private void queue(String str) {
    synchronized (txQueue) {
      List queue = (List) txQueue.get(currentTxId);
      if (queue == null)
        txQueue.put(currentTxId, queue = new ArrayList());

      queue.add(str);
    }
  }

  /** 
   * Return the XAResource representing this instance. The XAResource is used to know
   * when a transaction is being committed or rolled-back.
   * 
   * @return 
   */
  public XAResource getXAResource() {
    return xaResource;
  }

  /**
   * Flush all pending data and shut down.
   */
  public void close() {
    logger.info("Flushing logger");
    worker.interrupt();
    try {
      worker.join();
    } catch (InterruptedException ie) {
      logger.warn("interrupted while waiting for logger to be flushed");
    }
  }


  /* =====================================================================
   * ==== Everything below is run in the context of the Worker thread ====
   * =====================================================================
   */

  private void processQueue() {
    if (logger.isDebugEnabled())
      logger.debug("Processing logger queue");

    // make a copy of the queue and clear it
    final List mods = new ArrayList();
    synchronized (updQueue) {
      mods.addAll(updQueue);
      updQueue.clear();
      newUpdPending = false;
    }

    Iterator iter = mods.iterator();
    String cur = null;
    try {
      while (iter.hasNext()) {
        cur = (String) iter.next();
        txLog.write(cur);
      }
    } catch (Exception e) {
      logger.error("Failed to write to transaction-log - requeueing for later", e);

      synchronized (updQueue) {
        int idx = 0;
        updQueue.add(idx++, cur);
        while (iter.hasNext())
          updQueue.add(idx++, iter.next());
      }
    }
  }

  /** 
   * This represents the logger as a resource. It is used to ensure we only log stuff that's been
   * committed.
   */
  private class LoggerXAResource extends DummyXAResource {
    public void start(Xid xid, int flags) {
      currentTxId = xid;
    }

    public void end(Xid xid, int flags) {
      currentTxId = null;
    }

    public void commit(Xid xid, boolean onePhase) {
      List queue;
      synchronized (txQueue) {
        queue = (List) txQueue.remove(xid);
      }

      if (queue != null) {
        synchronized (updQueue) {
          updQueue.addAll(queue);
          newUpdPending = true;
          updQueue.notify();
        }
      }
    }

    public void rollback(Xid xid) {
      synchronized (txQueue) {
        txQueue.remove(xid);
      }
    }
  }

  private static class TxLog {
    private final File   logFile;
    private final File   tsFile;
    private final long   maxSize;
    private final long   maxAge;
    private final int    bufSize;
    private final String baseName;

    private Writer writer;
    private long   curMaxSize;
    private long   curMaxAge;
    private long   curSize;
    private long   maxTime;

    /** 
     * @param fileName the name of the log file; old versions will be name
     *                 <var>fileName</var>.&lt;N&gt; for N = 1...
     * @param maxSize  the maximum size (in bytes) the log file is allowed to get before it is
     *                 rotated, or -1 for no limit
     * @param maxAge   the maximum age (in seconds) the log file is allowed to get before it is
     *                 rotated, or -1 for no limit
     * @param bufSize  the size of the write buffer to use, or -1 for the default
     */
    public TxLog(String fileName, long maxSize, long maxAge, int bufSize) throws IOException {
      this.logFile = new File(fileName).getAbsoluteFile();
      this.maxSize = maxSize > 0 ? maxSize : Long.MAX_VALUE;
      this.maxAge  = maxAge  > 0 ? maxAge * 1000L : Long.MAX_VALUE;
      this.bufSize = bufSize > 0 ? bufSize : 9000;

      baseName = logFile.getName() + ".";
      tsFile = new File(logFile.getParentFile(), baseName + "timestamp");

      curSize    = (int) logFile.length();
      maxTime    = this.maxAge < Long.MAX_VALUE ? tsFile.lastModified() + this.maxAge :
                                                  Long.MAX_VALUE;
      curMaxSize = this.maxSize;
      curMaxAge  = this.maxAge;

      if (logFile.isDirectory())
        throw new IOException("Log file '" + logFile + "' is a directory");
      if (logFile.exists() && !logFile.canWrite())
        throw new IOException("Can't write to log file '" + logFile + "'");

      logger.info("Starting transaction log, file = '" + logFile + "', maxSize = " + this.maxSize +
                  " bytes, maxAge = " + (this.maxAge / 1000) + " seconds, bufferSize = " +
                  this.bufSize + " bytes");

      open();
      rotateIfNecessary();
    }

    private void open() throws IOException {
      writer = new OutputStreamWriter(
                        new ByteCounterOutputStream(new FileOutputStream(logFile, true)), "UTF-8");
      writer = new BufferedWriter(writer, bufSize);

      curSize = (int) logFile.length();

      if (curSize == 0)
        new FileOutputStream(tsFile).close();   // touch the timestamp
      maxTime = curMaxAge < Long.MAX_VALUE ? tsFile.lastModified() + curMaxAge : Long.MAX_VALUE;

      logger.info("Opened log file = '" + logFile + "' " +
                  (curSize == 0 ? "as new" : "for append"));
    }

    public void write(String str) throws IOException {
      writer.write(str);
      rotateIfNecessary();
    }

    public void flush() throws IOException {
      writer.flush();

      if (logger.isDebugEnabled())
        logger.debug("Flushed log file = '" + logFile + "' ");
    }

    public void close() throws IOException {
      writer.close();

      logger.info("Closed log file = '" + logFile + "' ");
    }

    private void rotateIfNecessary() throws IOException {
      if (curSize >= curMaxSize)
        logger.info("Max size reached on log file = '" + logFile + "' - rotating");
      else if (System.currentTimeMillis() >= maxTime)
        logger.info("Max age reached on log file = '" + logFile + "' - rotating");
      else
        return;

      close();
      rotate();
      open();
    }

    private void rotate() {
      // if the file doesn't exist, then there's nothing to do
      if (!logFile.exists())
        return;

      // get all old versions of the file
      File logDir = logFile.getParentFile();
      File[] files = logDir.listFiles(new FilenameFilter() {
        public boolean accept(File dir, String name) {
          try {
            return name.startsWith(baseName) &&
                   Integer.parseInt(name.substring(baseName.length())) >= 0;
          } catch (NumberFormatException nfe) {
            logger.debug("f='" + name + "'", nfe);
            return false;
          }
        }
      });

      Arrays.sort(files, new Comparator() {
        public int compare(Object o1, Object o2) {
          String s1 = ((File) o1).getName().substring(baseName.length());
          String s2 = ((File) o2).getName().substring(baseName.length());

          return Integer.valueOf(s1).compareTo(Integer.valueOf(s2));
        }
      });

      // add the current file and the new oldest to the list
      int last = files.length > 0 ?
            Integer.parseInt(files[files.length - 1].getName().substring(baseName.length())) : 0;

      File[] tmp = new File[files.length + 2];
      tmp[0] = logFile;
      System.arraycopy(files, 0, tmp, 1, files.length);
      tmp[files.length + 1] = new File(logDir, baseName + (last + 1));
      files = tmp;

      // rotate them
      for (int idx = files.length - 2; idx >= 0; idx--) {
        if (!files[idx].renameTo(files[idx + 1])) {
          logger.error("Error renaming '" + files[idx] + "' to '" + files[idx + 1] +
                       "' - aborting log rotation");

          // extend limits by 10% so we'll try again later
          if (curMaxSize < Long.MAX_VALUE)
            curMaxSize += maxSize / 10;
          if (curMaxAge < Long.MAX_VALUE)
            curMaxAge  += maxAge / 10;

          return;
        }
      }

      // all went well
      curMaxSize = maxSize;
      curMaxAge  = maxAge;

      logger.info("Rotated " + (files.length - 1) + " log files");
    }

    private class ByteCounterOutputStream extends FilterOutputStream {
      public ByteCounterOutputStream(OutputStream os) {
        super(os);
      }

      public void write(int b) throws IOException {
        out.write(b);
        curSize++;
      }

      public void write(byte[] b) throws IOException {
        out.write(b);
        curSize += b.length;
      }

      public void write(byte[] b, int off, int len) throws IOException {
        out.write(b, off, len);
        curSize += len;
      }
    }
  }

  private class Worker extends Thread {
    private final long interval;

    public Worker(long interval) {
      super("TransactionLogger-Worker");
      setDaemon(true);
      this.interval = interval;
    }

    public void run() {
      while (true) {
        boolean process;
        try {
          synchronized (updQueue) {
            if (!newUpdPending)
              updQueue.wait(interval);

            process = newUpdPending;
          }
        } catch (InterruptedException ie) {
          logger.warn("Worker thread interrupted - exiting", ie);
          break;
        }

        try {
          if (process)
            processQueue();
          else
            txLog.flush();
        } catch (Throwable t) {
          logger.error("Caught exception processing queue", t);
        }
      }

      // flush anything left
      processQueue();
      try {
        txLog.close();
      } catch (IOException ioe) {
        logger.error("Error closing log", ioe);
      }
    }
  }
}
