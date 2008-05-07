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

import java.io.Serializable;

import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

/**
 * Transactional cache abstraction. Currently only supports a READ_COMMITTED isolation level.
 *
 * @author Pradeep Krishnan
 */
public interface Cache {
  /**
   * Represents 'null' in cache.
   */
  public static Null NULL = new Null();

  /**
   * Gets the name of this cache.
   *
   * @return the name of this cache
   */
  public String getName();

  /**
   * Gets the cache-manager associated with this cache.
   *
   * @return the cache manager
   */
  public CacheManager getCacheManager();

  /**
   * Gets an object from the cache.
   *
   * @param <T> the type of the value expected
   * @param key the key
   *
   * @return the value or null
   */
  public <T> T get(Object key);

  /**
   * Gets an object from the cache.
   *
   * @param key the key
   *
   * @return the value or null or Null
   */
  public Object rawGet(Object key);

  /**
   * Read-thru cache that looks up from cache first and falls back to the supplied look-up.
   * The entry returned by the lookup is committed immediately to the cache and becomes available
   * to all other caches that share the same underlying cache/store. (READ_COMMITTED)
   *
   * @param <T> type of the value expected
   * @param <E> type of the exception expected to be thrown by lookup
   * @param key the key to use for the lookup
   * @param refresh the max-age of entries in the cache (in seconds), or -1 for indefinite
   * @param lookup the lookup to call to get the value if not found in the cache; may be null
   *
   * @return the value in the cache, or returned by the <var>lookup</var> if not in the cache
   *
   * @throws E the exception thrown by lookup
   */
  public <T, E extends Exception> T get(Object key, int refresh, Lookup<T, E> lookup)
                                 throws E;

  /**
   * Puts an object into the cache.
   *
   * @param key the key
   * @param val the value
   */
  public void put(Object key, Object val);

  /**
   * Removes an object at the specified key.
   *
   * @param key the key
   */
  public void remove(Object key);

  /**
   * Removes all items from the cache.
   */
  public void removeAll();

  /**
   * Gets all keys from the cache. Watch out for large caches!
   *
   * @return the set of keys
   */
  public Set<?> getKeys();

  /**
   * An object to indicate 'null' values stored in the cache.
   */
  public static class Null implements Serializable {
    public boolean equals(Object o) {
      return o instanceof Null;
    }

    public int hashCode() {
      return 0;
    }
  }

  /**
   * The call back to support read-thru cache lookup.
   *
   * @param <T> the type of value looked up
   * @param <E> the type of exception to expect on lookup
   */
  public static abstract class Lookup<T, E extends Exception> {
    /**
     * Looks up a value to populate the cache.
     *
     * @return the value to return; this value will be put into the cache
     *
     * @throws E from look up.
     */
    public abstract T lookup() throws E;

    /**
     * Executes a read-thru cache operation. The control is given here primarily to support
     * any locking strategies that the lookup implementation provides. By default it executes the
     * supplied operation.
     *
     * @param operation the operation to execute
     *
     * @return the value returned by executing the operation; not necessarily of type <T>
     *
     * @throws Exception the exception thrown by lookup
     */
    public Object execute(Operation operation) throws Exception {
      return operation.execute(false);
    }

    /**
     * A call-back operation from the Cache during a read-thru cache read.
     */
    public interface Operation {
      public Object execute(boolean degradedMode) throws Exception;
    }
  }

  /**
   * A lookup implementation for Read-thru caches that synchronizes all operations via a
   * monitor lock held on the supplied lock object. This ensures that the cache is only populated
   * by one thread. This mechanism is prone to dead-locks and therefore the LockedLockup should be
   * considered instead.
   *
   * @param <T> the type of value looked up
   * @param <E> the type of exception to expect on lookup
   */
  public static abstract class SynchronizedLookup<T, E extends Exception> extends Lookup<T, E> {
    private Object lock;

    public SynchronizedLookup(Object lock) {
      this.lock = lock;
    }

    public Object execute(Operation operation) throws Exception {
      synchronized (lock) {
        return operation.execute(false);
      }
    }
  }

  /**
   * A lookup implementation for Read-thru caches that synchronizes all operations via a lock
   * held on the supplied lock object. This ensures that the cache is only populated by one
   * thread. This is the preferred lookup strategy since dead-locks are detected by timeouts and
   * degrades to loading from the  backing cache/store.
   *
   * @param <T> the type of value looked up
   * @param <E> the type of exception to expect on lookup
   */
  public static abstract class LockedLookup<T, E extends Exception> extends Lookup<T, E> {
    private Lock     lock;
    private long     timeWait;
    private TimeUnit unit;

    public LockedLookup(Lock lock, long timeWait, TimeUnit unit) {
      this.lock       = lock;
      this.timeWait   = timeWait;
      this.unit       = unit;
    }

    public Object execute(Operation operation) throws Exception {
      boolean acquired = lock.tryLock(timeWait, unit);
      try {
        return operation.execute(!acquired);
      } finally {
        if (acquired)
          lock.unlock();
      }
    }
  }
}
