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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

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
  private static final Log         log             = LogFactory.getLog(CacheManager.class);
  private final Lock               updateLock;
  private final long               lockWaitSeconds;
  private final Map<String, Cache> caches          = new HashMap<String, Cache>();
  private Queue<CacheEvent>        queue           = new LinkedList<CacheEvent>();
  private List<Listener>           listeners       = new ArrayList<Listener>();
  private boolean                  locked          = false;

  /**
   * Creates a new CacheManager object.
   *
   * @param updateLock the lock to synchronize on
   * @param lockWaitSeconds seconds to wait on the lock before aborting
   */
  CacheManager(Lock updateLock, long lockWaitSeconds) {
    this.updateLock        = updateLock;
    this.lockWaitSeconds   = lockWaitSeconds;
  }

  /**
   * Shutdown/abort the cache manager and release any locks held.
   */
  public void shutdown() {
    if (locked) {
      log.warn("Shutdown called with locks held. Releasing ...");
      updateLock.unlock();
    }
  }

  /**
   * Register a cache with the cache manager.
   *
   * @param cache the cache to register
   */
  public void registerCache(Cache cache) {
    caches.put(cache.getName(), cache);
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
   * Begins a new transaction.
   *
   * @throws RuntimeException when a transactio was already started
   */
  public void beginTransaction() {
    if (!queue.isEmpty())
      throw new RuntimeException("A transaction is already active ...");

    for (Cache cache : caches.values())
      cache.setEventQueue(queue);
  }

  /**
   * Prepare to complete a transaction. Acquires the locks.
   *
   * @throws RuntimeException on a failure to acquire locks.
   */
  public void prepare() {
    try {
      if (!updateLock.tryLock(lockWaitSeconds, TimeUnit.SECONDS))
        throw new RuntimeException("Failed to acquire lock to update caches after waiting for "
                                   + lockWaitSeconds + " seconds.");
    } catch (InterruptedException e) {
      throw new RuntimeException("Interrupted while waiting to acquire a lock", e);
    }

    locked = true;
  }

  /**
   * Completes a transaction. The queued cache events are executed and all locks are
   * released.
   *
   * @param commit true to commit the changes or false otherwise
   */
  public void complete(boolean commit) {
    CacheEvent ev;

    while ((ev = queue.poll()) != null)
      ev.execute(commit);

    if (locked)
      updateLock.unlock();

    locked = false;
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
     * @param commit true for commits, false for rollbacks
     */
    public void execute(boolean commit);
  }
}
