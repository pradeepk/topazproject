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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.plos.journal.JournalService;

import org.plos.models.Journal;

import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.Interceptor;
import org.topazproject.otm.Session;
import org.topazproject.otm.Transaction;
import org.topazproject.otm.mapping.Binder;
import org.topazproject.otm.mapping.Binder.RawFieldData;
import org.topazproject.otm.mapping.BlobMapper;
import org.topazproject.otm.mapping.Mapper;
import org.topazproject.otm.mapping.RdfMapper;
import org.topazproject.otm.metadata.Definition;
import org.topazproject.otm.metadata.Reference;

/**
 * An interceptor that listens to changes from OTM and updates the cache-manager and also
 * maintains an object cache that acts as a second level cache for OTM.
 *
 * @author Pradeep Krishnan
 */
public class OtmInterceptor implements Interceptor {
  private static final Log     log             = LogFactory.getLog(OtmInterceptor.class);
  private static final NotNull NOT_NULL        = new NotNull();
  private final CacheManager   cacheManager;
  private final Cache          cache;
  private final JournalService journalService;
  private Set<String>          changedJournals = null;

  /**
   * Creates a new OtmInterceptor object.
   *
   * @param cacheManager the cache manager to update.
   * @param objCache the object cache 
   * @param journalService the journal service  
   */
  public OtmInterceptor(CacheManager cacheManager, Cache objCache, JournalService journalService) {
    this.cacheManager     = cacheManager;
    this.cache            = objCache;
    this.journalService   = journalService;
  }

  /*
   * inherited javadoc
   */
  public void afterTransactionBegin(Transaction txn) {
    cacheManager.beginTransaction();
    changedJournals = new HashSet<String>();
  }

  /*
   * inherited javadoc
   */
  public void beforeTransactionCompletion(Transaction txn) {
    cacheManager.prepare();
  }

  /*
   * inherited javadoc
   */
  public void afterTransactionCompletion(Transaction txn) {
    cacheManager.complete(txn.wasCommitted());
    changedJournals = null;
  }

  /*
   * inherited javadoc
   */
  public Object getEntity(Session session, ClassMetadata cm, String id, Object instance) {
    Object val = cache.rawGet(id);

    if (Cache.NULL.equals(val))
      return NULL;

    if (val == null)
      return null;

    String journal = getCurrentJournal();

    if (journal != null) {
      if (changedJournals.contains(journal))
        return null;

      Object o = cache.rawGet(journal + "-" + id);

      if (o == null)
        return null;

      if (Cache.NULL.equals(o))
        return NULL;
    }

    if (log.isTraceEnabled())
      log.trace("Returning an entity from the cache " + cm.getName() + " " + id);

    /* Note: Assumes get filtering produces identical results.
     * This assumption currently holds because filters are
     * not currently applied on 'writes' by OTM.
     */
    return ((Entry) val).get(session, cm, id, instance);
  }

  private void attach(Session session, ClassMetadata cm, String id, Object instance,
                      Collection<RdfMapper> fields, BlobMapper blob) {
    if (cm.isView())
      return; // since we can't safely invalidate

    Object val = cache.rawGet(id);
    Entry  e;

    if (val instanceof Entry)
      e = new Entry((Entry) val);
    else
      e = new Entry();

    e.set(session, cm, id, instance, fields, blob);
    cache.put(id, e);

    String journal = getCurrentJournal();

    if (journal != null)
      cache.put(journal + "-" + id, NOT_NULL);
  }

  /*
   * inherited javadoc
   */
  public void onPostRead(Session session, ClassMetadata cm, String id, Object instance) {
    attach(session, cm, id, instance, cm.getRdfMappers(), cm.getBlobField());
  }

  /*
   * inherited javadoc
   */
  public void onPostWrite(Session session, ClassMetadata cm, String id, Object instance,
                          Collection<RdfMapper> fields, BlobMapper blob) {
    attach(session, cm, id, instance, fields, blob);

    /* Note: if a smart-collection rule was updated as opposed to
     * new rules added or deleted, we wouldn't be able to detect it.
     * In that case we need to be explicitly told. But currently
     * journal definitions are not updated on the fly. So even
     * this attempt to detect a change is not likely to be hit.
     */
    if ((instance instanceof Journal)
         && (fields.contains(cm.getMapperByName("smartCollectionRules"))
             || fields.contains(cm.getMapperByName("simpleCollection"))))
      journalChanged(((Journal) instance).getKey(), false);

    cacheManager.objectChanged(cm, id, instance);
  }

  /*
   * inherited javadoc
   */
  public void onPostDelete(Session session, ClassMetadata cm, String id, Object instance) {
    cache.remove(id);

    for (String journal : journalService.getAllJournalKeys())
      if (!"".equals(journal))
        cache.remove(journal + "-" + id);

    if (instance instanceof Journal)
      journalChanged(((Journal) instance).getKey(), true);

    cacheManager.objectRemoved(cm, id);
  }

  private void journalChanged(String journal, boolean deleted) {
    changedJournals.add(journal);

    String prefix = journal + "-";
    int    count  = 0;

    for (String key : (Collection<String>)cache.getKeys()) {
      if (key.startsWith(prefix)) {
        cache.remove(key);
        count++;
      }
    }

    if (count > 0)
      log.warn("Removed(pending-commit) " + count + " entries from '" + cache.getName()
               + "' because the journal '" + journal + "' was "
               + (deleted ? "deleted." : "updated."));
  }

  private String getCurrentJournal() {
    String journal = journalService.getCurrentJournalKey();

    if ("".equals(journal))
      journal = null;

    return journal;
  }

  /**
   * An object cache-entry. The value map here is based on RdfDefinition name and therefore
   * it is dependent only on subject-id. Values are the serialized representations.
   */
  private static class Entry implements Serializable {
    private final Set<String>               types;
    private final Map<String, List<String>> values;
    private final Map<String, Set<String>>  tla;
    private Map<String, List<String>>       pmap;
    private byte[]                          blob;

    public Entry() {
      blob     = null;
      pmap     = null;
      types    = new HashSet<String>();
      values   = new HashMap<String, List<String>>();
      tla      = new HashMap<String, Set<String>>();
    }

    public Entry(Entry other) {
      types    = new HashSet<String>(other.types);
      blob     = copy(other.blob);
      pmap     = copy(other.pmap);
      values   = copy(other.values);
      tla      = copyS(other.tla);
    }

    public Object get(Session sess, ClassMetadata cm, String id, Object instance) {
      cm = sess.getSessionFactory().getSubClassMetadata(cm, sess.getEntityMode(), types);

      if (cm == null)
        return null;

      if (instance == null)
        instance = cm.getEntityBinder(sess).newInstance();

      cm.getIdField().getBinder(sess).set(instance, Collections.singletonList(id));

      for (RdfMapper mapper : cm.getRdfMappers()) {
        if (mapper.isPredicateMap())
          mapper.getBinder(sess).setRawValue(instance, copy(pmap));
        else {
          List<String> val = values.get(getName(mapper, sess));

          if (val != null)
            mapper.getBinder(sess).load(instance, val, tla, mapper, sess);
        }
      }

      if (cm.getBlobField() != null)
        cm.getBlobField().getBinder(sess).setRawValue(instance, copy(blob));

      return instance;
    }

    public void set(Session sess, ClassMetadata cm, String id, Object instance,
                    Collection<RdfMapper> fields, BlobMapper blobField) {
      types.addAll(cm.getTypes());

      if (blobField != null)
        blob = copy((byte[]) blobField.getBinder(sess).getRawValue(instance, false));

      for (RdfMapper mapper : fields) {
        Binder binder = mapper.getBinder(sess);
        RawFieldData data = binder.getRawFieldData(instance);
        if (data != null) {
          values.put(getName(mapper, sess), data.getValues());
          Map<String, Set<String>> m = data.getTypeLookAhead();
          if (m != null)
            tla.putAll(m);
        } else if (mapper.isPredicateMap())
          pmap = copy((Map<String, List<String>>) binder.getRawValue(instance, false));
        else if (!mapper.isAssociation())
          values.put(getName(mapper, sess), binder.get(instance));
        else {
          List          a  = binder.get(instance);
          List<String>  v  = new ArrayList(a.size());
          ClassMetadata am =
            sess.getSessionFactory().getClassMetadata(mapper.getAssociatedEntity());

          for (Object o : a) {
            ClassMetadata aom =
              sess.getSessionFactory().getInstanceMetadata(am, sess.getEntityMode(), o);
            String        oid = (String) aom.getIdField().getBinder(sess).get(o).get(0);
            v.add(oid);
            tla.put(oid, aom.getTypes());
          }

          values.put(getName(mapper, sess), v);
        }
      }
    }

    private static byte[] copy(byte[] src) {
      if (src == null)
        return null;

      byte[] dest = new byte[src.length];
      System.arraycopy(src, 0, dest, 0, src.length);

      return dest;
    }

    private static Map<String, List<String>> copy(Map<String, List<String>> src) {
      if (src == null)
        return null;

      Map<String, List<String>> dest = new HashMap<String, List<String>>(src.size());

      for (String name : src.keySet())
        dest.put(name, new ArrayList(src.get(name)));

      return dest;
    }

    private static Map<String, Set<String>> copyS(Map<String, Set<String>> src) {
      if (src == null)
        return null;

      Map<String, Set<String>> dest = new HashMap<String, Set<String>>(src.size());

      for (String name : src.keySet())
        dest.put(name, new HashSet(src.get(name)));

      return dest;
    }

    private String getName(Mapper mapper, Session sess) {
      return getName(mapper.getDefinition(), sess);
    }

    private String getName(Definition def, Session sess) {
      if (def instanceof Reference)
        return getName(sess.getSessionFactory().getDefinition(((Reference) def).getReferred()), sess);

      return def.getName();
    }
  }

  /**
   * An object to indicate existence of objects in a journal.
   */
  private static class NotNull implements Serializable {
    public boolean equals(Object o) {
      return o instanceof NotNull;
    }

    public int hashCode() {
      return 0;
    }
  }
}
