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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.plos.cache.CacheManager.CacheEvent;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

/**
 * A cache implementation using Ehcache.
 *
 * @author Pradeep Krishnan
 */
public class EhcacheProvider implements Cache {
  private static final Log  log          = LogFactory.getLog(EhcacheProvider.class);
  private CacheManager      cacheManager;
  private Queue<CacheEvent> events;
  private final Ehcache     cache;
  private final Map         local        = new HashMap();
  private boolean           removedAll   = false;

  /**
   * Creates a new EhcacheProvider object.
   *
   * @param cache the ehcache object
   */
  public EhcacheProvider(CacheManager cacheManager, Ehcache cache) {
    this.cache          = cache;
    this.cacheManager   = cacheManager;
    cacheManager.registerCache(this);
  }

  /*
   * inherited javadoc
   */
  public void setEventQueue(Queue<CacheEvent> events) {
    this.events = events;
    local.clear();
    events.add(new CacheEvent() {
        public void execute(boolean commit) {
          if (EhcacheProvider.this.removedAll && commit) {
            cache.removeAll();

            if (log.isDebugEnabled())
              log.debug("Removed-All from " + getName());
          }

          EhcacheProvider.this.removedAll = false;
        }
      });
  }

  /*
   * inherited javadoc
   */
  public String getName() {
    return cache.getName();
  }

  /*
   * inherited javadoc
   */
  public <T> T get(Object key) {
    Object val = rawGet(key);

    return NULL.equals(val) ? null : (T) val;
  }

  /*
   * inherited javadoc
   */
  public Object rawGet(Object key) {
    Object val = local.get(key);

    if ((val == null) && removedAll)
      val = NULL;

    if (val == null) {
      Element e = cache.get(key);

      if (e != null)
        val = e.getValue();
    }

    if (log.isTraceEnabled())
      log.trace("Cache " + ((val == null) ? "miss" : "hit") + " for '" + key + "' in " + getName());

    return val;
  }

  /*
   * inherited javadoc
   */
  public <T, E extends Exception> T get(final Object key, final Lookup<T, E> lookup)
                                 throws E {
    Object val = rawGet(key);

    if ((val == null) && (lookup != null)) {
      val =
        lookup.execute(new Operation<E>() {
            public Object execute(boolean degraded) throws E {
              if (degraded)
                log.warn("Degraded mode lookup for key '" + key + "' in cache '"
                         + EhcacheProvider.this.getName() + "'");

              Object val = rawGet(key);

              if (val == null)
                val = lookup.lookup();

              if (val == null)
                val = NULL;

              cache.put(new Element(key, val));

              if (log.isDebugEnabled())
                log.debug("Populated entry with '" + key + "' in " + getName());

              return val;
            }
          });
    }

    return NULL.equals(val) ? null : (T) val;
  }

  /*
   * inherited javadoc
   */
  public void put(final Object key, final Object val) {
    if (events == null)
      throw new NullPointerException("events queue is null");

    local.put(key, val);
    events.add(new CacheEvent() {
        public void execute(boolean commit) {
          if (commit)
            EhcacheProvider.this.commit(key);
        }
      });
    cacheManager.cacheEntryChanged(this, key, val);
  }

  /*
   * inherited javadoc
   */
  public void remove(final Object key) {
    if (events == null)
      throw new NullPointerException("events queue is null");

    local.put(key, NULL);
    events.add(new CacheEvent() {
        public void execute(boolean commit) {
          if (commit)
            EhcacheProvider.this.commit(key);
        }
      });
    cacheManager.cacheEntryRemoved(this, key);
  }

  /*
   * inherited javadoc
   */
  public void removeAll() {
    if (events == null)
      throw new NullPointerException("events queue is null");

    local.clear();
    removedAll = true;
    cacheManager.cacheCleared(this);
  }

  /*
   * inherited javadoc
   */
  public Set<?> getKeys() {
    Set keys = removedAll ? new HashSet() : new HashSet(cache.getKeys());

    for (Object key : local.keySet()) {
      if (NULL.equals(local.get(key)))
        keys.remove(key);
      else
        keys.add(key);
    }

    return keys;
  }

  private void commit(Object key) {
    Object val = local.get(key);

    if (NULL.equals(val)) {
      cache.remove(key);

      if (log.isDebugEnabled())
        log.debug("Removed '" + key + "' from " + getName());
    } else if (val != null) {
      cache.put(new Element(key, val));

      if (log.isDebugEnabled())
        log.debug("Added '" + key + "' to " + getName());
    }

    local.remove(key);
  }
}
