/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2007 by Topaz, Inc.
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

package org.plos.journal;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.EntityMode;
import org.topazproject.otm.Interceptor.Updates;
import org.topazproject.otm.Session;
import org.topazproject.otm.SessionFactory;
import org.topazproject.otm.Transaction;
import org.topazproject.otm.query.Results;
import org.topazproject.otm.util.TransactionHelper;
import org.topazproject.otm.mapping.EntityBinder;
import org.topazproject.otm.mapping.java.ClassBinder;

import org.plos.cache.Cache;
import org.plos.cache.ObjectListener;
import org.plos.models.Article;
import org.plos.models.Journal;

/**
 * This service manages the reverse mapping from an object to the set of journals it appears in.
 *
 * <p>There should be exactly one instance of this class per {@link
 * org.topazproject.otm.SessionFactory SessionFactory} instance.
 *
 * <p>This services does extensive caching of journal objects, the filters associated with each
 * journal, and the list of journals each object (article) belongs to (according to the filters).
 * For this reason it must be notified any time a journal or article is added, removed, or changed.
 *
 * @author Ronald Tschalär
 */
public class JournalCarrierService {
  private static final Log    log = LogFactory.getLog(JournalCarrierService.class);
  private static final String RI_MODEL = "ri";

  private final SessionFactory           sf;
  private final Cache                    objectCarriers;        // obj-id -> Set<journal-key>
  private final JournalKeyService        journalKeyService;
  private final JournalFilterService     journalFilterService;

  /**
   * Create a new journal-service instance. One and only one of these should be created for evey
   * session-factory instance, and this must be done before the first session instance is created.
   *
   * @param sf           the session-factory to use
   * @param journalCache the cache to use for caching journal definitions
   * @param objectCache  the cache to use for caching the list of journals that carry each object
   */
  public JournalCarrierService(SessionFactory sf, Cache objectCache, 
      JournalKeyService journalKeyService, JournalFilterService journalFilterService) {
    this.sf                   = sf;
    this.objectCarriers       = objectCache;
    this.journalKeyService    = journalKeyService;
    this.journalFilterService = journalFilterService;

    addObjectClassToSf();
    initialize();

    objectCarriers.getCacheManager().registerListener(new ObjectListener() {
      public void objectChanged(Session s, ClassMetadata cm, String id, Object o, Updates updates) {
        /* Note: if a smart-collection rule was updated as opposed to
         * new rules added or deleted, we wouldn't be able to detect it.
         * In that case we need to be explicitly told. But currently
         * journal definitions are not updated on the fly. So even
         * this attempt to detect a change is not likely to be hit.
         */
        if ((o instanceof Journal)
             && ((updates == null)
                 || updates.isChanged("smartCollectionRules")
                 || updates.isChanged("simpleCollection")))
            updateCarrierMap(((Journal)o).getKey(), false, s);

        if ((o instanceof Article) && (updates == null))
          objectWasAdded(((Article) o).getId(), s);
         
      }
      public void objectRemoved(Session s, ClassMetadata cm, String id, Object o) {
        if (o instanceof Journal)
            updateCarrierMap(((Journal)o).getKey(), true, s);

        if (o instanceof Article)
          objectWasDeleted(((Article) o).getId());
      }
    });
  }

  private void initialize() {
    /* spring initializes singletons at startup, so no session is available yet, and hence
     * we create our own and create our own transaction. Alternatively, we could use
     * lazy-init="true".
     */
    Session s = sf.openSession();
    try {
      TransactionHelper.doInTx(s, new TransactionHelper.Action<Void>() {
        public Void run(Transaction tx) {
          Map<URI, Set<Journal>> cl = buildCarrierMap(null, tx.getSession());
          for (Map.Entry<URI, Set<Journal>> e : cl.entrySet())
            objectCarriers.put(e.getKey(), getKeys(e.getValue()));

          return null;
        }
      });
    } finally {
      s.close();
    }
  }

  private void addObjectClassToSf() {
    Map<EntityMode, EntityBinder> binders = new HashMap<EntityMode, EntityBinder>();
    binders.put(EntityMode.POJO, new ClassBinder(Object.class));
    sf.setClassMetadata(new ClassMetadata(binders, "Object",
          null, Collections.EMPTY_SET, RI_MODEL, null, Collections.EMPTY_SET, null, null,
          Collections.EMPTY_SET));
  }

  private static <K, T> void put(Map<K, Set<T>> map, K key, T value) {
    Set<T> values = map.get(key);
    if (values == null)
      map.put(key, values = new HashSet<T>());
    values.add(value);
  }

  private static Set<String> getKeys(Set<Journal> jList) {
    Set<String> keys = new HashSet<String>();
    for (Journal j: jList)
      keys.add(j.getKey());
    return keys;
  }


  /* must be invoked with journalCache monitor held and active tx on session */
  private void updateCarrierMap(String jName, boolean deleted, Session s) {
    Map<URI, Set<Journal>> carriers = new HashMap<URI, Set<Journal>>();

    Set<URI> obj = deleted ? Collections.EMPTY_SET : getObjects(jName, null, s);

    int count = 0;
    for (URI o : (Set<URI>) objectCarriers.getKeys()) {
      Set<String> keys = objectCarriers.get(o);
      if (keys == null)
        continue;

      boolean mod = keys.remove(jName);
      if (obj.remove(o)) {
        keys.add(jName);
        mod ^= true;
      }

      if (mod) {
        objectCarriers.put(o, keys);
        count++;
      }
    }
    if (log.isDebugEnabled())
      log.debug("journal '" + jName + "' was " + (deleted ? "deleted" : "updated")
          + " and as a result carriers for " + count + " objects have been updated.");
  }

  /* must be invoked with journalCache monitor held and active tx on session and in local context */
  private Map<URI, Set<Journal>> buildCarrierMap(URI oid, Session s) {
    Map<URI, Set<Journal>> carriers = new HashMap<URI, Set<Journal>>();

    for (Journal j : journalKeyService.getAllJournals(s)) {
      Set<URI> obj = getObjects(j.getKey(), oid, s);
      for (URI o : obj)
        put(carriers, o, j);
    }

    return carriers;
  }

  private Set<URI> getObjects(String jName, URI obj, Session s) {
    Set<String> oldFilters = s.listFilters();
    for (String fn : oldFilters)
      s.disableFilter(fn);

    for (String fn : journalFilterService.getFilters(jName, s))
      s.enableFilter(fn);

    Set<URI> res = new HashSet<URI>();

    // XXX: should be "... from Object ..." but filters don't get applied then
    String q = "select id from Article o where id := cast(o, Article).id" +
                (obj != null ? " and id = <" + obj + ">;" : ";");
    try {
      Results r = s.createQuery(q).execute();
      while (r.next())
        res.add(r.getURI(0));
    } finally {
      for (String fn : s.listFilters())
        s.disableFilter(fn);

      for (String fn : oldFilters)
        s.enableFilter(fn);
    }

    return res;
  }


  public Set<String> getJournalKeysForObject(final URI oid, final Session s) {
    return objectCarriers.get(oid, -1,
        new Cache.SynchronizedLookup<Set<String>, RuntimeException>(oid.toString().intern()) {
          public Set<String> lookup() {
             Collection<Set<Journal>> jnlSets = buildCarrierMap(oid, s).values();
             Set<Journal> jnlList = (jnlSets.size() > 0) 
                              ? jnlSets.iterator().next() : Collections.EMPTY_SET;
             return getKeys(jnlList);
          }
        });
  }

  public Set<Journal> getJournalsForObject(final URI oid, final Session s) {
    Set<Journal> jnlList = new HashSet<Journal>();

    for (String key : getJournalKeysForObject(oid, s))
      jnlList.add(journalKeyService.getJournal(key, s));

    jnlList.remove(null); // just in case something was deleted

    return jnlList;
  }

  /**
   * Notify the journal service of a newly added object (e.g. an article). This assumes an active
   * transaction on the session.
   *
   * @param oid the info:&lt;oid&gt; uri of the object
   */
  private void objectWasAdded(URI oid, Session s) {
    Set<String> keys = getJournalKeysForObject(oid, s);

    if (log.isDebugEnabled())
      log.debug("object '" + oid + "' was added and belongs to journals: " + keys);
  }

  /**
   * Notify the journal service of a recently deleted object (e.g. article).
   *
   * @param oid the info:&lt;oid&gt; uri of the object
   */
  private void objectWasDeleted(URI oid) {
    objectCarriers.remove(oid);

    if (log.isDebugEnabled())
      log.debug("object '" + oid + "' was removed");
  }


}
