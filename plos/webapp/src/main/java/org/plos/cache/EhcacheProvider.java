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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.plos.cache.CacheManager.CacheEvent;
import org.plos.cache.CacheManager.TxnContext;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

/**
 * A cache implementation using Ehcache.
 *
 * @author Pradeep Krishnan
 */
public class EhcacheProvider implements Cache {
  private static final Log   log          = LogFactory.getLog(EhcacheProvider.class);
  private final CacheManager cacheManager;
  private final Ehcache      cache;

  /**
   * Creates a new EhcacheProvider object.
   *
   * @param cache the ehcache object
   */
  public EhcacheProvider(CacheManager cacheManager, Ehcache cache) {
    this.cache                            = cache;
    this.cacheManager                     = cacheManager;
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
  public CacheManager getCacheManager() {
    return cacheManager;
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
    TxnContext ctx = cacheManager.getTxnContext();
    Object     val = ctx.getLocal(getName()).get(key);

    if ((val == null) && ctx.getRemovedAll(getName()))
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
  public <T, E extends Exception> T get(final Object key, final int refresh,
                                        final Lookup<T, E> lookup)
                                 throws E {
    Object val = rawGet(key);

    try {
      if ((val == null) && (lookup != null)) {
        val =
          lookup.execute(new Lookup.Operation() {
            public Object execute(boolean degraded) throws Exception {
              if (degraded)
                log.warn("Degraded mode lookup for key '" + key + "' in cache '"
                         + EhcacheProvider.this.getName() + "'");

              Object val = rawGet(key);

              if (val == null)
                val = lookup.lookup();

              if (val == null)
                val = NULL;

              Element e = new Element(key, val);

              if (refresh > 0)
                e.setTimeToLive(refresh);

              cache.put(e);

              if (log.isDebugEnabled())
                log.debug("Populated entry with '" + key + "' in " + getName());

              return val;
            }
          });
      }
    } catch (RuntimeException e)  {
       throw e;
    } catch (Exception e) {
      throw (E)e;
    }

    return NULL.equals(val) ? null : (T) val;
  }

  /*
   * inherited javadoc
   */
  public void put(final Object key, final Object val) {
    TxnContext ctx = cacheManager.getTxnContext();
    ctx.getLocal(getName()).put(key, val);
    ctx.enqueue(new CacheEvent() {
        public void execute(TxnContext ctx, boolean commit) {
          if (commit)
            EhcacheProvider.this.commit(ctx, key);
        }
      });
    cacheManager.cacheEntryChanged(this, key, val);
  }

  /*
   * inherited javadoc
   */
  public void remove(final Object key) {
    TxnContext ctx = cacheManager.getTxnContext();
    ctx.getLocal(getName()).put(key, NULL);
    ctx.enqueue(new CacheEvent() {
        public void execute(TxnContext ctx, boolean commit) {
          if (commit)
            EhcacheProvider.this.commit(ctx, key);
        }
      });
    cacheManager.cacheEntryRemoved(this, key);
  }

  /*
   * inherited javadoc
   */
  public void removeAll() {
    TxnContext ctx = cacheManager.getTxnContext();
    ctx.getLocal(getName()).clear();
    ctx.setRemovedAll(getName(), true);
    cacheManager.cacheCleared(this);
  }

  /*
   * inherited javadoc
   */
  public Set<?> getKeys() {
    TxnContext ctx        = cacheManager.getTxnContext();
    Map        local      = ctx.getLocal(getName());
    boolean    removedAll = ctx.getRemovedAll(getName());
    Set        keys       = removedAll ? new HashSet() : new HashSet(cache.getKeys());

    for (Object key : local.keySet()) {
      if (NULL.equals(local.get(key)))
        keys.remove(key);
      else
        keys.add(key);
    }

    return keys;
  }

  private void commit(TxnContext ctx, Object key) {
    Map    local = ctx.getLocal(getName());
    Object val   = local.get(key);

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
