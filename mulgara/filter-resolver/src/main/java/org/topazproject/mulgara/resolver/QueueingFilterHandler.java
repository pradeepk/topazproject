/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2008 by Topaz, Inc.
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

package org.topazproject.mulgara.resolver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.apache.log4j.Logger;
import org.mulgara.resolver.spi.DummyXAResource;

/** 
 * This implements some common functionality for filter-handlers that queue updates which are
 * then processed asynchronously by another thread.
 * 
 * @author Ronald Tschalär
 */
abstract class QueueingFilterHandler<T> extends AbstractFilterHandler {
  protected final Logger            logger;
  protected final boolean           orderedMods;
  protected final List<T>           updQueue = new ArrayList<T>();
  protected final Map<Xid, List<T>> txQueue  = new HashMap<Xid, List<T>>();
  protected       boolean           newUpdPending;
  protected final XAResource        xaResource;
  protected final Worker            worker;
  protected final ThreadLocal<Xid>  currentTxId = new ThreadLocal<Xid>();

  /** 
   * Create a new queueing-filter instance. Events are queued and the queue is processed by a
   * separate thread which invokes either {@link #processQueue processQueue} or {@link #idleCallback
   * idleCallback}, depending on whether anything is available after the <var>coalesce</var> and
   * <var>wait</var> timeouts have expired.
   * 
   * @param workWait    how long to wait for something to arrive on the queue, in milliseconds. May
   *                    be 0 to wait indefinitely.
   * @param clscWait    if &gt; 0, the worker will sleep for <var>clscWait</var> milliseconds after
   *                    something arrives on the queue; this is to allow events to coalesce. Set to
   *                    0 to have events processed as soon as possible.
   * @param workerName  the name to assign the worker thread
   * @param orderedMods true of the mods should stay ordered
   * @param logger      the logger to use
   * @throws IOException 
   */
  protected QueueingFilterHandler(long workWait, long clscWait, String workerName,
                                  boolean orderedMods, Logger logger) {
    this.logger      = logger;
    this.orderedMods = orderedMods;

    xaResource = new QueueingXAResource();

    worker = new Worker(workerName, workWait, clscWait);
  }

  protected void queue(T obj) {
    synchronized (txQueue) {
      List<T> queue = txQueue.get(currentTxId.get());
      if (queue == null)
        txQueue.put(currentTxId.get(), queue = new ArrayList<T>());

      queue.add(obj);
    }
  }

  /** 
   * Return the XAResource representing this instance. The XAResource is used to know
   * when a transaction is being committed or rolled-back.
   * 
   * @return the xa-resource
   */
  public XAResource getXAResource() {
    return xaResource;
  }

  public void abort() {
    try {
      xaResource.rollback(currentTxId.get());
    } catch (Exception e) {
      logger.error("Error rolling back tx for abort");
    }
  }

  /**
   * Flush all pending data and shut down.
   */
  public void close() {
    logger.info("Flushing worker");
    worker.interrupt();
    try {
      worker.join();
    } catch (InterruptedException ie) {
      logger.warn("interrupted while waiting for worker to be flushed");
    }
  }


  /* =====================================================================
   * ==== Everything below is run in the context of the Worker thread ====
   * =====================================================================
   */

  protected void processQueue() {
    if (logger.isDebugEnabled())
      logger.debug("Processing worker queue");

    // make a copy of the queue and clear it
    final Collection<T> mods = orderedMods ? (Collection<T>) new ArrayList<T>() : new HashSet<T>();
    synchronized (updQueue) {
      mods.addAll(updQueue);
      updQueue.clear();
      newUpdPending = false;
    }

    Iterator<T> iter = mods.iterator();
    T cur = null;
    try {
      while (iter.hasNext())
        handleQueuedItem(cur = iter.next());
    } catch (Exception e) {
      logger.error("Failed to write to transaction-log - requeueing for later", e);

      synchronized (updQueue) {
        int idx = 0;
        if (cur != null)
          updQueue.add(idx++, cur);
        while (iter.hasNext())
          updQueue.add(idx++, iter.next());
      }
    }
  }

  protected abstract void handleQueuedItem(T obj) throws Exception;

  protected abstract void idleCallback() throws Exception;

  protected abstract void shutdownCallback() throws Exception;

  /** 
   * This represents the queueing handler as a resource. It is used to ensure we only operate on
   * stuff that's been committed.
   */
  protected class QueueingXAResource extends DummyXAResource {
    public void start(Xid xid, int flags) {
      if (logger.isTraceEnabled())
        logger.trace("setting xid '" + xid + "'");

      currentTxId.set(xid);
    }

    public void end(Xid xid, int flags) {
      if (logger.isTraceEnabled())
        logger.trace("clearing xid '" + xid + "'");

      currentTxId.set(null);
    }

    public void commit(Xid xid, boolean onePhase) {
      if (logger.isTraceEnabled())
        logger.trace("committing xid '" + xid + "'");

      List<T> queue;
      synchronized (txQueue) {
        queue = txQueue.remove(xid);
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
      if (logger.isTraceEnabled())
        logger.trace("rolling back xid '" + xid + "'");

      synchronized (txQueue) {
        txQueue.remove(xid);
      }
    }
  }

  protected class Worker extends Thread {
    private final long maxWait;
    private final long coalesce;

    public Worker(String name, long maxWait, long coalesce) {
      super(name);
      setDaemon(true);
      this.maxWait  = (maxWait > 0L ? maxWait : 0L);
      this.coalesce = coalesce;
    }

    public void run() {
      while (true) {
        boolean process;
        try {
          synchronized (updQueue) {
            if (!newUpdPending)
              updQueue.wait(maxWait);

            process = newUpdPending;
          }

          if (process && coalesce > 0)
            sleep(coalesce);
        } catch (InterruptedException ie) {
          logger.warn("Worker thread interrupted - exiting", ie);
          break;
        }

        try {
          if (process)
            processQueue();
          else
            idleCallback();
        } catch (Throwable t) {
          logger.error("Caught exception processing queue", t);
        }
      }

      // flush anything left
      processQueue();
      try {
        shutdownCallback();
      } catch (Exception e) {
        logger.error("Error closing log", e);
      }
    }
  }
}
