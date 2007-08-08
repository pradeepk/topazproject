/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.plos.journal;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.Session;
import org.topazproject.otm.SessionFactory;
import org.topazproject.otm.Transaction;
import org.topazproject.otm.criterion.DetachedCriteria;
import org.topazproject.otm.criterion.Disjunction;
import org.topazproject.otm.criterion.Restrictions;
import org.topazproject.otm.criterion.SubjectCriterion;
import org.topazproject.otm.filter.ConjunctiveFilterDefinition;
import org.topazproject.otm.filter.CriteriaFilterDefinition;
import org.topazproject.otm.filter.DisjunctiveFilterDefinition;
import org.topazproject.otm.filter.FilterDefinition;
import org.topazproject.otm.query.Results;
import org.topazproject.otm.util.TransactionHelper;

import org.plos.models.Aggregation;
import org.plos.models.Journal;
import org.plos.models.UserProfile;

import org.springframework.beans.factory.annotation.Required;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

/**
 * This service manages journal definitions and associated info. All retrievals and modifications
 * should go through here so it can keep the cache up-to-date.
 *
 * <p>There should be exactly one instance of this class per {@link
 * org.topazproject.otm.SessionFactory SessionFactory} instance. Also, the instance must be created
 * before any {@link org.topazproject.otm.Session Session} instance is created as it needs to
 * register the filter-definition with the session-factory.
 *
 * @author Ronald Tschalär
 */
public class JournalService {
  private static final Log    log = LogFactory.getLog(JournalService.class);
  private static final String RI_MODEL = "ri";

  private final SessionFactory           sf;
  private final Map<String, Set<String>> journalFilters = new HashMap<String, Set<String>>();
  private final Ehcache                  journalCache;          // key/id -> Journal
  private final Ehcache                  objectCarriers;        // obj-id -> Set<journal-id>

  private       Session                  session;

  /** 
   * Create a new journal-service instance. One and only one of these should be created for evey
   * session-factory instance, and this must be done before the first session instance is created.
   * 
   * @param sf           the session-factory to use
   * @param journalCache the cache to use for caching journal definitions
   * @param objectCache  the cache to use for caching the list of journals that carry each object
   */
  public JournalService(SessionFactory sf, Ehcache journalCache, Ehcache objectCache) {
    this.sf             = sf;
    this.journalCache   = journalCache;
    this.objectCarriers = objectCache;

    initialize();
  }

  private void initialize() {
    sf.setClassMetadata(new ClassMetadata(Object.class, "Object", null, Collections.EMPTY_SET,
                                          RI_MODEL, null, null, Collections.EMPTY_SET));

    Session s = sf.openSession();
    try {
      TransactionHelper.doInTx(s, new TransactionHelper.Action<Void>() {
        public Void run(Transaction tx) {
          Set<Aggregation> preloaded = new HashSet<Aggregation>();

          List<Journal> l = (List<Journal>) tx.getSession().createCriteria(Journal.class).list();
          for (Journal j : l)
            loadJournal(j, preloaded, tx.getSession());

          Map<URI, Set<Journal>> cl = buildCarrierMap(null, tx.getSession());
          for (Map.Entry<URI, Set<Journal>> e : cl.entrySet())
            objectCarriers.put(new Element(e.getKey(), getIds(e.getValue())));

          return null;
        }
      });
    } finally {
      s.close();
    }
  }

  private static <K, T> void put(Map<K, Set<T>> map, K key, T value) {
    Set<T> values = map.get(key);
    if (values == null)
      map.put(key, values = new HashSet<T>());
    values.add(value);
  }

  private static Set<URI> getIds(Set<Journal> jList) {
    Set<URI> ids = new HashSet<URI>();
    for (Journal j: jList)
      ids.add(j.getId());
    return ids;
  }

  private void loadJournal(Journal j, Set<Aggregation> preloaded, Session s) {
    if (log.isDebugEnabled())
      log.debug("loading journal '" + j.getKey() + "'");

    // create the filter definitions
    Map<String, FilterDefinition> jfds =
        getAggregationFilters(j, j.getKey() + "-" + System.currentTimeMillis(), s,
                              new HashSet<Aggregation>());

    if (log.isDebugEnabled())
      log.debug("journal '" + j.getKey() + "' has filters: " + jfds);

    synchronized (sf) {
      // clear old defs
      Set<String> oldDefs = journalFilters.remove(j.getKey());
      if (oldDefs != null) {
        for (String fn : oldDefs)
          sf.removeFilterDefinition(fn);
      }

      // save the new filter-defs
      for (FilterDefinition fd : jfds.values()) {
        sf.addFilterDefinition(fd);
        put(journalFilters, j.getKey(), fd.getFilterName());
      }

      if (!journalFilters.containsKey(j.getKey()))
        journalFilters.put(j.getKey(), Collections.EMPTY_SET);
    }

    // cache journal
    preloadAggregation(j, preloaded, s);
    journalCache.put(new Element(j.getKey(), j));
    journalCache.put(new Element(j.getId(), j));
  }

  private void removeJournal(Journal j) {
    if (log.isDebugEnabled())
      log.debug("removing journal '" + j.getKey() + "'");

    synchronized (sf) {
      Set<String> oldDefs = journalFilters.remove(j.getKey());
      if (oldDefs != null) {
        for (String fn : oldDefs)
          sf.removeFilterDefinition(fn);
      }
    }

    journalCache.remove(j.getKey());
    journalCache.remove(j.getId());
  }

  private Map<String, FilterDefinition> getAggregationFilters(Aggregation a, String pfx, Session s,
                                                              Set<Aggregation> processed) {
    processed.add(a);

    // create a combined set of filter-defs from the rule-based and static collections
    Map<String, Set<FilterDefinition>> sfd = new HashMap<String, Set<FilterDefinition>>();
    int idx = 0;
    for (DetachedCriteria dc : a.getSmartCollectionRules()) {
      dc.setAlias(normalize(dc.getAlias(), sf));
      put(sfd, dc.getAlias(), new CriteriaFilterDefinition(pfx + "-rf-" + idx++, dc));
    }

    Map<String, FilterDefinition> smartFilterDefs = combineFilterDefs(sfd, pfx + "-af-");

    Map<String, FilterDefinition> staticFilterDefs =
        buildStaticFilters(a.getSimpleCollection(), pfx + "-sf-", s);

    Map<String, FilterDefinition> afds =
        mergeFilterDefs(smartFilterDefs, staticFilterDefs, pfx + "-of-");

    // recursively resolve aggregations selected by the filters
    Set<String> oldFilters = s.listFilters();
    for (String fn : oldFilters)
      s.disableFilter(fn);

    for (FilterDefinition fd : afds.values())
      s.enableFilter(fd);

    List<Aggregation> aggs = (List<Aggregation>) s.createCriteria(Aggregation.class).list();

    for (String fn : s.listFilters())
      s.disableFilter(fn);

    for (String fn : oldFilters)
      s.enableFilter(fn);

    idx = 0;
    for (Aggregation ag : aggs) {
      if (!processed.contains(ag))
        afds = mergeFilterDefs(afds, getAggregationFilters(ag, pfx + "-ag-" + idx, s, processed),
                               pfx + "-mf-" + idx++);
    }

    // done
    return afds;
  }

  /**
   * and(...) the filter-defs for each class.
   */
  private Map<String, FilterDefinition> combineFilterDefs(Map<String, Set<FilterDefinition>> fds,
                                                          String pfx) {
    Map<String, FilterDefinition> res = new HashMap<String, FilterDefinition>();

    int idx = 0;
    for (String cls : fds.keySet()) {
      ConjunctiveFilterDefinition and = new ConjunctiveFilterDefinition(pfx + idx, cls);
      for (FilterDefinition fd : fds.get(cls))
        and.addFilterDefinition(fd);

      res.put(cls, and);
    }

    return res;
  }

  /** 
   * Build the set of filter-definitions for the statically-defined list of objects.
   *
   * <p>Ideally this would create filter-definitions based on something like the following OQL
   * query: 
   * <pre>
   *  select o from java.lang.Object o, Journal j where j.id = :jid and o = j.simpleCollection;
   * </pre>
   * Unfortunately, however, this cannot be turned into a Criteria which currently is needed in
   * order to apply the filter to Criteria-based queries. So instead it creates an explicit list
   * of or'd id's.
   * 
   * @param uris the list of id's of the objects
   * @param pfx  the prefix to use for filter-names
   * @param s    the otm session to use
   * @return the set of filter-defs
   */
  private Map<String, FilterDefinition> buildStaticFilters(List<URI> uris, String pfx, Session s) {
    if (uris.size() == 0)
      return Collections.EMPTY_MAP;

    // get all the rdf:type's for each object
    StringBuilder typeQry = new StringBuilder(110 + uris.size() * 70);
    // Note to the unwary: this may look Article specific, but it isn't.
    typeQry.append("select id, (select o.<rdf:type> from Object x) from Object o ").
            append("where id := cast(o, Article).id and (");

    for (URI uri : uris)
      typeQry.append("id = <").append(uri).append("> or ");

    typeQry.setLength(typeQry.length() - 4);
    typeQry.append(");");

    Results r = s.createQuery(typeQry.toString()).execute();

    // build a map of uri's keyed by class
    Map<Class, Set<String>> idsByClass = new HashMap<Class, Set<String>>();
    while (r.next()) {
      String  id = r.getString(0);
      Results tr = r.getSubQueryResults(1);

      Set<String> types = new HashSet<String>();
      while (tr.next())
        types.add(tr.getString(0));

      if (types.size() == 0) {
        log.warn("object '" + id + "' from static collection either doesn't exist or has no type" +
                 " - ignoring it");
        continue;
      }

      Class c = sf.mostSpecificSubClass(Object.class, types);
      if (c == null) {
        log.error("no class registered for static collection object '" + id + "'; types were '" +
                  types + "'");
        continue;
      }

      put(idsByClass, c, id);
    }

    // create a filter-definition for each class
    Map<String, FilterDefinition> fds = new HashMap<String, FilterDefinition>();
    for (Class c : idsByClass.keySet()) {
      String cname = normalize(c.getName(), sf);
      DetachedCriteria dc = new DetachedCriteria(cname);
      Disjunction or = Restrictions.disjunction();
      dc.add(or);

      for (String id : idsByClass.get(c))
        or.add(new SubjectCriterion(id));

      fds.put(cname, new CriteriaFilterDefinition(pfx + c.getName(), dc));
    }

    return fds;
  }

  private static String normalize(String name, SessionFactory sf) {
    ClassMetadata cm = sf.getClassMetadata(name);
    return (cm != null) ? cm.getName() : name;
  }

  /**
   * or(...) together the filter-definitions for each class.
   */
  private static Map<String, FilterDefinition> mergeFilterDefs(Map<String, FilterDefinition> l1,
                                                               Map<String, FilterDefinition> l2,
                                                               String pfx) {
    Map<String, FilterDefinition> res = new HashMap<String, FilterDefinition>();
    int idx = 0;

    for (String cls : l1.keySet()) {
      FilterDefinition f1 = l1.get(cls);
      FilterDefinition f2 = l2.remove(cls);
      if (f2 == null) {
        res.put(cls, f1);
      } else {
        DisjunctiveFilterDefinition or = new DisjunctiveFilterDefinition(pfx + idx++, cls);
        or.addFilterDefinition(f1);
        or.addFilterDefinition(f2);

        res.put(cls, or);
      }
    }

    res.putAll(l2);

    return res;
  }

  private static void preloadAggregation(Aggregation a, Set<Aggregation> preloaded, Session s) {
    if (a == null || preloaded.contains(a))
      return;
    preloaded.add(a);

    if (a.getEditorialBoard() != null && a.getEditorialBoard().getEditors() != null) {
      for (UserProfile up : a.getEditorialBoard().getEditors())
        up.getInterests();
    }

    a.getSimpleCollection();

    for (DetachedCriteria dc : a.getSmartCollectionRules())
      preloadCriteria(dc, s);

    preloadAggregation(a.getSupersedes(), preloaded, s);
    preloadAggregation(a.getSupersededBy(), preloaded, s);
  }

  private static void preloadCriteria(DetachedCriteria c, Session s) {
    c.getExecutableCriteria(s);         // easiest way of touching everything...
  }

  private Map<URI, Set<Journal>> buildCarrierMap(URI oid, Session s) {
    Map<URI, Set<Journal>> carriers = new HashMap<URI, Set<Journal>>();

    for (String jName : journalFilters.keySet()) {
      Element e = journalCache.get(jName);
      Journal j = (e != null) ? (Journal) e.getObjectValue() : getAndLoadJournal(jName, s);

      Set<URI> obj = getObjects(jName, oid, s);
      for (URI o : obj)
        put(carriers, o, j);
    }

    return carriers;
  }

  private void updateCarrierMap(Journal j, boolean deleted, Session s) {
    Map<URI, Set<Journal>> carriers = new HashMap<URI, Set<Journal>>();

    Set<URI> obj = deleted ? Collections.EMPTY_SET : getObjects(j.getKey(), null, s);

    for (URI o : (List<URI>) objectCarriers.getKeys()) {
      Element e = objectCarriers.get(o);
      if (e == null)
        continue;
      Set<URI> jIds = (Set<URI>) e.getObjectValue();

      boolean mod = jIds.remove(j.getId());
      if (obj.remove(o)) {
        jIds.add(j.getId());
        mod ^= true;
      }

      if (mod)
        objectCarriers.put(e);
    }
  }

  private Set<URI> getObjects(String jName, URI obj, Session s) {
    Set<String> oldFilters = s.listFilters();
    for (String fn : oldFilters)
      s.disableFilter(fn);

    for (String fn : journalFilters.get(jName))
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

  private Journal getAndLoadJournal(String jName, Session s) {
    List l = s.createCriteria(Journal.class).add(Restrictions.eq("key", jName)).list();
    if (l.size() == 0)
      return null;

    Journal j = (Journal) l.get(0);
    loadJournal(j, new HashSet<Aggregation>(), s);
    updateCarrierMap(j, false, s);
    return j;
  }

  private Journal getAndLoadJournal(URI id, Session s) {
    Journal j = s.get(Journal.class, id.toString());
    if (j != null) {
      loadJournal(j, new HashSet<Aggregation>(), s);
      updateCarrierMap(j, false, s);
    }
    return j;
  }

  /** 
   * Get the names of the {@link org.topazproject.otm.Filter session filters} associated with the
   * specified journal.
   * 
   * @param jName the journal's name (key)
   * @return the list of filters (which may be empty), or null if no journal by the given name is
   *         known
   */
  public Set<String> getFilters(String jName) {
    return journalFilters.get(jName);
  }

  /** 
   * Get the specified journal. This assumes an active transaction on the session.
   * 
   * @param jName  the journal's name
   * @return the journal, or null if no found
   */
  public Journal getJournal(String jName) {
    Element e = journalCache.get(jName);
    if (e != null)
      return (Journal) e.getObjectValue();

    return getAndLoadJournal(jName, session);
  }

  /** 
   * Get the specified journal. This assumes an active transaction on the session.
   * 
   * @param id  the journal's id
   * @return the journal, or null if no found
   */
  public Journal getJournal(URI id) {
    Element e = journalCache.get(id);
    if (e != null)
      return (Journal) e.getObjectValue();

    return getAndLoadJournal(id, session);
  }

  /** 
   * Get the set of all the known journals. This assumes an active transaction on the session.
   * 
   * @return all the journals, or the empty set if there are none
   */
  public Set<Journal> getAllJournals() {
    Set<Journal> res = new HashSet<Journal>();
    for (String jName : journalFilters.keySet()) {
      Journal j = getJournal(jName);
      if (j != null)
        res.add(j);
    }
    return res;
  }

  /** 
   * Signal that the given journal was modified. The filters and object lists will be updated.
   * This assumes an active transaction on the session.
   * 
   * @param j the journal that was modified.
   */
  public void journalWasModified(Journal j) {
    loadJournal(j, new HashSet<Aggregation>(), session);
    updateCarrierMap(j, false, session);
  }

  /** 
   * Signal that the given journal was deleted. The object lists will be updated.
   * This assumes an active transaction on the session.
   * 
   * @param j the journal that was deleted.
   */
  public void journalWasDeleted(Journal j) {
    removeJournal(j);
    updateCarrierMap(j, true, session);
  }

  /** 
   * Get the list of journals which carry the given object (e.g. article).
   * 
   * @param oid the info:&lt;oid&gt; uri of the object
   * @return the list of journals which carry this object; will be empty if this object
   *         doesn't belong to any journal
   */
  public Set<Journal> getJournalsForObject(URI oid) {
    Set<Journal> jnlList;

    Element jl = objectCarriers.get(oid);
    if (jl == null) {
      Collection<Set<Journal>> jnlSets = buildCarrierMap(oid, session).values();
      jnlList = (jnlSets.size() > 0) ? jnlSets.iterator().next() : Collections.EMPTY_SET;
      objectCarriers.put(new Element(oid, getIds(jnlList)));
    } else {
      jnlList = new HashSet<Journal>();
      for (URI id : (Set<URI>) jl.getObjectValue()) {
        Journal j = getJournal(id);
        if (j == null)
          log.error("Unexpected null journal for id '" + id + "'");
        else
          jnlList.add(j);
      }
    }

    return jnlList;
  }

  /** 
   * Notify the journal service of a newly added object (e.g. an article). This assumes an active
   * transaction on the session.
   * 
   * @param oid the info:&lt;oid&gt; uri of the object
   */
  public void objectWasAdded(URI oid) {
    objectCarriers.remove(oid);
    getJournalsForObject(oid);

    if (log.isDebugEnabled())
      log.debug("object '" + oid + "' was added and belongs to journals: " +
                objectCarriers.get(oid).getObjectValue());
  }

  /** 
   * Notify the journal service of a recently deleted object (e.g. article). 
   * 
   * @param oid the info:&lt;oid&gt; uri of the object
   */
  public void objectWasDeleted(URI oid) {
    objectCarriers.remove(oid);

    if (log.isDebugEnabled())
      log.debug("object '" + oid + "' was removed");
  }

  /**
   * Set the OTM session. Called by spring's bean wiring. 
   * 
   * @param session the otm session
   */
  @Required
  public void setOtmSession(Session session) {
    this.session = session;
  }
}
