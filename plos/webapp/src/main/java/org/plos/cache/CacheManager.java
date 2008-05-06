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
package org.plos.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.Transaction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.otm.ClassMetadata;

/**
 * Manages transactions across a set of caches. It also brokers the event notifications from
 * the caches to the listeners.
 *
 * @author Pradeep Krishnan
 */
public class CacheManager implements CacheListener, ObjectListener {
  private static final Log                   log             =
    LogFactory.getLog(CacheManager.class);
  private final CacheManagerFactory          factory;
  private final long                         lockWaitSeconds;
  private final Lock                         updateLock      = new ReentrantLock();
  private final List<Listener>               listeners       = new ArrayList<Listener>();
  private final Map<Transaction, TxnContext> contexts        =
    new HashMap<Transaction, TxnContext>();

  /**
   * Creates a new CacheManager object.
   *
   * @param factory the factory that created this.
   * @param lockWaitSeconds seconds to wait on the lock before aborting
   */
  CacheManager(CacheManagerFactory factory, long lockWaitSeconds) {
    this.factory           = factory;
    this.lockWaitSeconds   = lockWaitSeconds;
  }

  /**
   * Register a listener with the cache manager.
   *
   * @param listener listener for cache or object update events.
   */
  public void registerListener(Listener listener) {
    listeners.add(listener);
  }

  /**
   * Gets the current transaction context.
   *
   * @return the current transaction context
   *
   * @throws RuntimeException DOCUMENT ME!
   */
  public TxnContext getTxnContext() {
    Transaction txn;

    try {
      txn = factory.getTransactionManager().getTransaction();
    } catch (Exception e) {
      throw new RuntimeException("Error getting current transaction", e);
    }

    if (txn == null)
      throw new RuntimeException("No transaction active");

    return getTxnContext(txn);
  }

  /**
   * Gets the context for a transaction.
   *
   * @param txn the transaction
   *
   * @return the transaction context
   */
  public TxnContext getTxnContext(Transaction txn) {
    synchronized (contexts) {
      TxnContext ctx = contexts.get(txn);

      if (ctx == null) {
        ctx = new TxnContext(txn);
        contexts.put(txn, ctx);

        try {
          txn.registerSynchronization(ctx);
        } catch (Exception e) {
          log.warn("Failed to register synchronization", e);
        }
      }

      return ctx;
    }
  }

  /*
   * inherited javadoc
   */
  public void cacheEntryChanged(Cache cache, Object key, Object val) {
    for (Listener listener : listeners)
      if (listener instanceof CacheListener)
        ((CacheListener) listener).cacheEntryChanged(cache, key, val);
  }

  /*
   * inherited javadoc
   */
  public void cacheEntryRemoved(Cache cache, Object key) {
    for (Listener listener : listeners)
      if (listener instanceof CacheListener)
        ((CacheListener) listener).cacheEntryRemoved(cache, key);
  }

  /*
   * inherited javadoc
   */
  public void cacheCleared(Cache cache) {
    for (Listener listener : listeners)
      if (listener instanceof CacheListener)
        ((CacheListener) listener).cacheCleared(cache);
  }

  /*
   * inherited javadoc
   */
  public void objectChanged(ClassMetadata cm, String id, Object obj) {
    for (Listener listener : listeners)
      if (listener instanceof ObjectListener)
        ((ObjectListener) listener).objectChanged(cm, id, obj);
  }

  /*
   * inherited javadoc
   */
  public void objectRemoved(ClassMetadata cm, String id) {
    for (Listener listener : listeners)
      if (listener instanceof ObjectListener)
        ((ObjectListener) listener).objectRemoved(cm, id);
  }

  /**
   * The cache events that are waiting for the transaction to complete.
   */
  public static interface CacheEvent {
    /**
     * Execute the event now that the transaction is completing.
     *
     * @param ctx transaction ctx
     * @param commit true for commits, false for rollbacks
     */
    public void execute(TxnContext ctx, boolean commit);
  }

  public class TxnContext implements Synchronization {
    private final Transaction          txn;
    private final Queue<CacheEvent>    queue           = new LinkedList<CacheEvent>();
    private final Map<String, Map>     locals          = new HashMap<String, Map>();
    private final Map<String, Boolean> removedAll      = new HashMap<String, Boolean>();
    private final Set<String>          changedJournals = new HashSet<String>();
    private boolean                    locked          = false;

    public TxnContext(Transaction txn) {
      this.txn = txn;

      if (log.isDebugEnabled())
        log.debug("TxnContext started for " + txn);
    }

    public void beforeCompletion() {
      try {
        if (!updateLock.tryLock(lockWaitSeconds, TimeUnit.SECONDS))
          throw new RuntimeException("Failed to acquire lock to update caches after waiting for "
                                     + lockWaitSeconds + " seconds.");
      } catch (InterruptedException e) {
        throw new RuntimeException("Interrupted while waiting to acquire a lock", e);
      }

      if (log.isDebugEnabled())
        log.debug("beforeCompletion: acquired update-lock for " + txn);

      locked = true;
    }

    public void afterCompletion(int status) {
      if (log.isDebugEnabled())
        log.debug("afterCompletion: processing event-queue with " + queue.size() + " entries for "
                  + txn);

      if (locked == false)
        log.warn("afterCompletion: called without acquiring the update-lock");

      CacheEvent ev;

      while ((ev = queue.poll()) != null)
        ev.execute(this, status == Status.STATUS_COMMITTED);

      if (locked) {
        updateLock.unlock();

        if (log.isDebugEnabled())
          log.debug("afterCompletion: released update-lock for " + txn);

        locked = false;
      }

      synchronized (contexts) {
        if (contexts.remove(txn) != null) {
          if (log.isDebugEnabled())
            log.debug("Removed context from context-map for " + txn);
        } else {
          Iterator<Map.Entry<Transaction, TxnContext>> it = contexts.entrySet().iterator();

          while (it.hasNext()) {
            Map.Entry<Transaction, TxnContext> entry = it.next();

            if (entry.getValue() == this) {
              it.remove();

              if (log.isDebugEnabled())
                log.debug("Removed context from context-map by iteration for " + txn);

              break;
            }
          }
        }
      }
    }

    public void enqueue(CacheEvent ev) {
      queue.add(ev);
    }

    public Map getLocal(String cache) {
      Map m = locals.get(cache);

      if (m == null)
        locals.put(cache, m = new HashMap());

      return m;
    }

    public boolean getRemovedAll(String cache) {
      return removedAll.get(cache);
    }

    public void setRemovedAll(String cache, boolean val) {
      removedAll.put(cache, val);
    }

    public Set<String> getChangedJournals() {
      return changedJournals;
    }
  }
}
