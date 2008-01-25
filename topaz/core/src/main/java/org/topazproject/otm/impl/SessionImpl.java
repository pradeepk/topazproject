/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.otm.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.otm.event.PreInsertEventListener;
import org.topazproject.otm.event.PostLoadEventListener;
import org.topazproject.otm.id.IdentifierGenerator;
import org.topazproject.otm.mapping.Loader;
import org.topazproject.otm.mapping.Mapper;
import org.topazproject.otm.mapping.Mapper.CascadeType;
import org.topazproject.otm.mapping.Mapper.FetchType;
import org.topazproject.otm.query.Results;

import org.topazproject.otm.OtmException;
import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.Filter;
import org.topazproject.otm.Transaction;
import org.topazproject.otm.BlobStore;
import org.topazproject.otm.TripleStore;
import org.topazproject.otm.Session;
import org.topazproject.otm.Query;
import org.topazproject.otm.Rdf;

/**
 * An otm session (similar to hibernate session). And similar to hibernate session, not thread
 * safe.
 *
 * @author Pradeep Krishnan
 */
public class SessionImpl extends AbstractSession {
  private static final Log                     log            = LogFactory.getLog(SessionImpl.class);
  private final Map<Id, Object>                cleanMap       = new HashMap<Id, Object>();
  private final Map<Id, Object>                dirtyMap       = new HashMap<Id, Object>();
  private final Map<Id, Object>                deleteMap      = new HashMap<Id, Object>();
  private final Map<Id, LazyLoadMethodHandler> proxies        = new HashMap<Id, LazyLoadMethodHandler>();
  private final Map<Id, Set<Wrapper>>          orphanTrack    = new HashMap<Id, Set<Wrapper>>();
  private final Set<Id>                        currentIds     = new HashSet<Id>();

  private static final StateCache              states         = new StateCache() {
    public void insert(Object o, ClassMetadata cm, Session session) throws OtmException {
      synchronized(this) {
        super.insert(o, cm, session);
      }
    }
    public Collection<Mapper> update(Object o, ClassMetadata cm, Session session)
        throws OtmException {
      synchronized(this) {
        return super.update(o, cm, session);
      }
    }
    public void remove(Object o) {
      synchronized(this) {
        super.remove(o);
      }
    }
  };

  /**
   * Creates a new SessionImpl object.
   *
   * @param sessionFactory the session factory that created this session
   */
  SessionImpl(SessionFactoryImpl sessionFactory) {
    super(sessionFactory);
  }

  /*
   * inherited javadoc
   */
  public void flush() throws OtmException {
    if (txn == null)
      throw new OtmException("No active transaction");

    // auto-save objects that aren't explicitly saved
    for (Id id : new HashSet<Id>(cleanMap.keySet())) {
      Object o = cleanMap.get(id);
      if (o != null) {
        ClassMetadata cm = checkClass(o.getClass());
        if (!cm.isView())
          sync(o, id, false, true, CascadeType.saveOrUpdate, true);
      }
    }

    for (Map.Entry<Id, Object> e : deleteMap.entrySet())
      write(e.getKey(), e.getValue(), true);

    for (Object o : dirtyMap.values())
      if (o instanceof PreInsertEventListener)
        ((PreInsertEventListener)o).onPreInsert(this, o);

    for (Map.Entry<Id, Object> e : dirtyMap.entrySet())
      write(e.getKey(), e.getValue(), false);

    deleteMap.clear();
    cleanMap.putAll(dirtyMap);
    dirtyMap.clear();
  }

  /*
   * inherited javadoc
   */
  public void clear() {
    cleanMap.clear();
    dirtyMap.clear();
    deleteMap.clear();
    proxies.clear();
    orphanTrack.clear();
  }

  /*
   * inherited javadoc
   */
  public String saveOrUpdate(Object o) throws OtmException {
    Id id = checkObject(o, true, true);
    sync(o, id, false, true, CascadeType.saveOrUpdate, true);

    return id.getId();
  }

  /*
   * inherited javadoc
   */
  public String delete(Object o) throws OtmException {
    Id id = checkObject(o, true, true);

    if (currentIds.contains(id))
      return id.getId(); // loop

    try {
      currentIds.add(id);

      cleanMap.remove(id);
      dirtyMap.remove(id);
      deleteMap.put(id, o);

      ClassMetadata<?> cm     = sessionFactory.getClassMetadata(o.getClass());
      Set<Wrapper>     assocs = new HashSet<Wrapper>();

      for (Mapper p : cm.getFields()) {
        if (!p.isAssociation())
          continue;

        // ignore this association if delete does not cascade
        if (!p.isCascadable(CascadeType.delete))
          continue;

        for (Object ao : p.get(o))
          assocs.add(new Wrapper(checkObject(ao, true, true), ao));
      }

      Set<Wrapper> old = orphanTrack.remove(id);

      if (old != null)
        assocs.addAll(old);

      for (Wrapper ao : assocs)
        delete(ao.get());
    } finally {
      currentIds.remove(id);
    }

    return id.getId();
  }

  /*
   * inherited javadoc
   */
  public String evict(Object o) throws OtmException {
    final Id id = checkObject(o, true, true);

    if (currentIds.contains(id))
      return id.getId(); // loop

    try {
      currentIds.add(id);

      cleanMap.remove(id);
      dirtyMap.remove(id);

      ClassMetadata<?> cm     = sessionFactory.getClassMetadata(o.getClass());
      Set<Wrapper>     assocs = new HashSet<Wrapper>();

      for (Mapper p : cm.getFields()) {
        if (!p.isAssociation())
          continue;

        // ignore this association if evict does not cascade
        if (!p.isCascadable(CascadeType.evict))
          continue;

        for (Object ao : p.get(o))
          assocs.add(new Wrapper(checkObject(ao, true, true), ao));
      }

      for (Wrapper ao : assocs)
        evict(ao.get());
    } finally {
      currentIds.remove(id);
    }

    return id.getId();
  }

  /*
   * inherited javadoc
   */
  public boolean contains(Object o) throws OtmException {
    final Id id = checkObject(o, true, false);

    return (cleanMap.get(id) == o) || (dirtyMap.get(id) == o);
  }

  /*
   * inherited javadoc
   */
  public <T> T load(Class<T> clazz, String oid) throws OtmException {
    if ((oid == null) || oid.equals(Rdf.rdf + "nil"))
      return null;

    Id     id = new Id(clazz, oid);
    Object o  = deleteMap.get(id);

    if (o != null)
      return null;

    o = cleanMap.get(id);

    if (o == null)
      o = dirtyMap.get(id);

    if (o == null) {
      o = newDynamicProxy(clazz, id, checkClass(clazz));
      cleanMap.put(id, o);
    }

    if ((o == null) || clazz.isInstance(o))
      return clazz.cast(o);

    throw new OtmException("something wrong: asked to load() <" + oid + "> and map to class '"
                           + clazz + "' but what we ended up with is an instance of '"
                           + o.getClass());
  }

  /*
   * inherited javadoc
   */
  public <T> T get(Class<T> clazz, String oid) throws OtmException {
    return get(clazz, oid, true);
  }

  /*
   * inherited javadoc
   */
  public <T> T get(Class<T> clazz, String oid, boolean filterObj) throws OtmException {
    if ((oid == null) || oid.equals(Rdf.rdf + "nil"))
      return null;

    Id     id = new Id(clazz, oid);
    Object o  = deleteMap.get(id);

    if (o != null)
      return null;

    o = cleanMap.get(id);

    if (o == null)
      o = dirtyMap.get(id);

    if (o == null) {
      o = getFromStore(id, checkClass(clazz), null, filterObj);

      if (o != null) {
        // Must merge here. Associations may have a back-pointer to this Id.
        // If those associations are loaded from the store even before
        // we complete this get() operation, there will be an instance
        // in our cache for this same Id.
        o = sync(o, id, true, false, null, true);
      }
    }

    if (o instanceof ProxyObject)
        o.equals(null); // ensure it is loaded

    if ((o == null) || clazz.isInstance(o))
      return clazz.cast(o);

    throw new OtmException("something wrong: asked to get() <" + oid + "> and map to class '"
                           + clazz + "' but what we ended up with is an instance of '"
                           + o.getClass());
  }

  /*
   * inherited javadoc
   */
  public <T> T merge(T o) throws OtmException {
    Id id = checkObject(o, true, false);

    // make sure it is loaded
    T ao = (T) get(o.getClass(), id.getId());

    if (ao == null) {
      // does not exist; so make a copy first
      try {
        ao = (T) o.getClass().newInstance();
      } catch (Exception e) {
        throw new OtmException("instantiation failed", e);
      }

      Map<Id, Object> loopDetect = new HashMap<Id, Object>();
      loopDetect.put(id, ao);
      copy(ao, o, loopDetect); // deep copy
      o = ao;
    }

    ao = (T) sync(o, id, true, true, CascadeType.merge, false);

    return ao;
  }

  /*
   * inherited javadoc
   */
  public void refresh(Object o) throws OtmException {
    Id id = checkObject(o, false, true);

    if (dirtyMap.containsKey(id) || cleanMap.containsKey(id)) {
      o = getFromStore(id, checkClass(o.getClass()), o, true);
      sync(o, id, true, false, CascadeType.refresh, true);
    }
  }

  private <T> boolean isPristineProxy(Id id, T o) {
    LazyLoadMethodHandler llm           = (o instanceof ProxyObject) ? proxies.get(id) : null;
    return (llm != null) ? !llm.isLoaded() : false;
  }

  private <T> void write(Id id, T o, boolean delete) throws OtmException {
    ClassMetadata<T>      cm            = sessionFactory.getClassMetadata((Class<T>) o.getClass());
    TripleStore           store         = sessionFactory.getTripleStore();
    BlobStore             bs            = sessionFactory.getBlobStore();
    Loader                bf            = cm.getBlobField();

    if (delete) {
      if (log.isDebugEnabled())
        log.debug("deleting from store: " + id);

     states.remove(o);
     if (bf != null)
       bs.delete(cm, id.getId(), txn);
     else
       store.delete(cm, cm.getFields(), id.getId(), o, txn);
    } else if (isPristineProxy(id, o)) {
      if (log.isDebugEnabled())
        log.debug("Update skipped for " + id + ". This is a proxy object and is not even loaded.");
    } else {
      Collection<Mapper> fields = states.update(o, cm, this);
      boolean firstTime = (fields == null);
      if (firstTime)
        fields = cm.getFields();
      int nFields = fields.size();
      if (log.isDebugEnabled()) {
        if (firstTime)
          log.debug("Saving " + id + " to store.");
        else if (nFields == cm.getFields().size())
            log.debug("Full update for " + id + ".");
        else if (nFields == 0)
          log.debug("Update skipped for " + id + ". No changes to the object state.");
        else {
          Collection<Mapper> skips = new ArrayList(cm.getFields());
          skips.removeAll(fields);
          StringBuilder buf = new StringBuilder(100);
          char sep = ' ';
          for (Mapper m : skips) {
            buf.append(sep).append(m.getName());
            sep = ',';
          }
          log.debug("Partial update for " + id + ". Skipped:" + buf);
        }
      }

      if (bf == null) {
        if (firstTime || (nFields > 0)) {
          store.delete(cm, fields, id.getId(), o, txn);
          store.insert(cm, fields, id.getId(), o, txn);
        }
      } else {
        switch(states.digestUpdate(o, bf)) {
        case delete:
          bs.delete(cm, id.getId(), txn);
          break;
        case update:
          bs.delete(cm, id.getId(), txn);
        case insert:
          bs.insert(cm, id.getId(), (byte[]) bf.getRawValue(o, false), txn);
          break;
        case noChange:
        default:
          break;
        }
      }
    }
  }


  private <T> T getFromStore(Id id, ClassMetadata<T> cm, T instance, boolean filterObj)
                       throws OtmException {
    if (txn == null)
      throw new OtmException("No transaction active");

    TripleStore store = sessionFactory.getTripleStore();

    if (cm.isView())
      instance = loadView(cm, id.getId(), instance, txn,
                                  new ArrayList<Filter>(filters.values()), filterObj);
    else if (cm.getBlobField() == null)
      instance = store.get(cm, id.getId(), instance, txn, 
                                  new ArrayList<Filter>(filters.values()), filterObj);
    else if (instance == null) {
      try {
        instance = cm.getSourceClass().newInstance();
      } catch (Throwable t) {
        throw new OtmException("Failed to instantiate a new object for " + cm, t);
      }
      cm.getIdField().set(instance, Collections.singletonList(id.getId()));
    }

    if (instance == null)
      return instance;

    cm = sessionFactory.getClassMetadata((Class<T>) instance.getClass());

    Loader bf = cm.getBlobField();
    if (bf != null)
      bf.setRawValue(instance, sessionFactory.getBlobStore().get(cm, id.getId(), txn));

    if (!cm.isView()) {
      for (Mapper m : cm.getFields())
        if (m.getFetchType() == FetchType.eager) {
          for (Object o : m.get(instance))
            if (o != null)
              o.equals(null);
        }
    }

    if (instance instanceof PostLoadEventListener)
      ((PostLoadEventListener)instance).onPostLoad(this, instance);

    if (!cm.isView()) {
      states.insert(instance, cm, this);
      if (bf != null)
        states.digestUpdate(instance, bf);
    }

    return instance;
  }

  private <T> T loadView(ClassMetadata<T> cm, String id, T instance, Transaction txn,
                         List<Filter> filters, boolean filterObj) throws OtmException {
    Query q = createQuery(cm.getQuery());
    Set<String> paramNames = q.getParameterNames();
    if (paramNames.size() != 1 || !"id".equals(paramNames.iterator().next()))
      throw new OtmException("View queries must have exactly one parameter, and the parameter " +
                             "name must be 'id'; class='" + cm.getSourceClass().getName() +
                             "', query='" + cm.getQuery() + "'");

    Results r = q.setParameter("id", id).execute();

    if (r.next())
      instance = createInstance(cm, instance, id, r);
    else
      instance = null;

    return instance;
  }

  private <S> S createInstance(ClassMetadata<S> cm, S obj, String id, Results r)
      throws OtmException {
    try {
      if (obj == null)
        obj = cm.getSourceClass().newInstance();
    } catch (Exception e) {
      throw new OtmException("Failed to create instance of '" + cm.getSourceClass().getName() + "'",
                             e);
    }

    if (id != null && cm.getIdField() != null)
      cm.getIdField().set(obj, Collections.singletonList(id));

    for (Mapper m : cm.getFields()) {
      int    idx = r.findVariable(m.getProjectionVar());
      Object val = getValue(r, idx, m.getComponentType(), m.getFetchType() == FetchType.eager);
      if (val instanceof List)
        m.set(obj, (List) val);
      else
        m.setRawValue(obj, val);
    }

    return obj;
  }

  private Object getValue(Results r, int idx, Class<?> type, boolean eager) throws OtmException {
    switch (r.getType(idx)) {
      case CLASS:
        return (type == String.class) ? r.getString(idx) : r.get(idx, eager);

      case LITERAL:
        return (type == String.class) ? r.getString(idx) : r.getLiteralAs(idx, type);

      case URI:
        return (type == String.class) ? r.getString(idx) : r.getURIAs(idx, type);

      case SUBQ_RESULTS:
        ClassMetadata<?> scm = sessionFactory.getClassMetadata(type);
        boolean isSubView = scm != null && !scm.isView() && !scm.isPersistable();

        List<Object> vals = new ArrayList<Object>();

        Results sr = r.getSubQueryResults(idx);
        sr.beforeFirst();
        while (sr.next())
          vals.add(isSubView ? createInstance(scm, null, null, sr) : getValue(sr, 0, type, eager));

        return vals;

      default:
        throw new Error("unknown type " + r.getType(idx) + " encountered");
    }
  }
  
  /**
   * Synchronize the Session Cache entries with the current state of the object; either
   * loaded from the database or supplied by the application.
   *
   * @param other an object that may or may not be known to the session and may even be 
   *              a duplicate instance (same Id, different instance)
   * @param id    the object id
   * @param merge the 'other' object needs to be merged to existing instance in Session 
   * @param update this operation is performed because of an update by the application
   *               (as opposed to a load from the store) 
   * @param cascade the applicable cascade control (or 'null' for certain internal operations)
   * @param skipProxy whether to avoid the force-loading of proxy object
   *
   * @return the object from Session Cache.
   */
  private Object sync(final Object other, final Id id, final boolean merge, final boolean update,
                      final CascadeType cascade, final boolean skipProxy) throws OtmException {
    if (currentIds.contains(id))
      return null; // loop and hence the return value is unused

    Object o = null;

    try {
      currentIds.add(id);
      o = deleteMap.get(id);

      if (o != null)
        throw new OtmException("Attempt to use a deleted object. Remove all references to <" 
                                + id + "> from all 'Persisted' objects");

      o = dirtyMap.get(id);

      boolean dirtyExists = o != null;

      if (!dirtyExists)
        o = cleanMap.get(id);

      if (merge && (o != null) && (other != o))
        copy(o, other, null); // shallow copy
      else
        o = other;

      if (update || dirtyExists) {
        dirtyMap.put(id, o);
        cleanMap.remove(id);
      } else {
        cleanMap.put(id, o);
      }

      // avoid loading lazy loaded objects
      if (skipProxy && isPristineProxy(id, o))
        return o;

      ClassMetadata<?> cm     = checkClass(o.getClass());
      Set<Wrapper>     assocs = new HashSet<Wrapper>();

      for (Mapper p : cm.getFields()) {
        if (!p.isAssociation() || (p.getUri() == null))
          continue;

        boolean deep = ((cascade != null) && p.isCascadable(cascade));
        boolean deepDelete = p.isCascadable(CascadeType.deleteOrphan);
        for (Object ao : p.get(o)) {
          // Note: duplicate check is not performed when merging
          Id aid = checkObject(ao, update, !merge);

          // Note: sync() here will not return a merged object. see copy()
          if (deep)
            sync(ao, aid, merge, update, cascade, skipProxy);
          if (deepDelete)
            assocs.add(new Wrapper(aid, ao));
        }
      }

      // Note: We do this unconditionally. ie. update==false or update==true
      // We do the orphan deletes only when update==true. update==false is
      // used in cases where a fresh load from the database was performed
      // and therefore discarding the previous orphanTrack entries is just fine.
      Set<Wrapper> old = orphanTrack.put(id, assocs);

      if (update && (old != null)) {
        old.removeAll(assocs);

        for (Wrapper ao : old)
          delete(ao.get());
      }
    } finally {
      currentIds.remove(id);
    }

    return o;
  }

  private Object copy(Object o, Object other, Map<Id, Object> loopDetect)
               throws OtmException {
    ClassMetadata<?> ocm = checkClass(other.getClass());
    ClassMetadata<?> cm  = checkClass(o.getClass());

    if (!cm.getSourceClass().isAssignableFrom(ocm.getSourceClass()))
      throw new OtmException(cm.toString() + " is not assignable from " + ocm);

    if (log.isDebugEnabled())
      log.debug("Copy merging " + checkObject(o, false, false));

    for (Mapper p : cm.getFields()) {
      Mapper op = ocm.getMapperByUri(p.getUri(), p.hasInverseUri(), p.getRdfType());

      if (op == null)
        continue;

      if ((loopDetect == null) || !p.isAssociation())
        p.set(o, op.get(other));
      else {
        List cc = new ArrayList();

        for (Object ao : op.get(other)) {
          Id     id  = checkObject(ao, false, false);
          Object aoc = loopDetect.get(id);

          if (aoc == null) {
            try {
              aoc = ao.getClass().newInstance();
            } catch (Exception e) {
              throw new OtmException("instantiation failed", e);
            }

            loopDetect.put(id, aoc);
            copy(aoc, ao, loopDetect);
          }

          cc.add(aoc);
        }

        p.set(o, cc);
      }
    }

    return o;
  }

  private <T> T newDynamicProxy(final Class<T> clazz, final Id id, final ClassMetadata<T> cm)
                          throws OtmException {
    LazyLoadMethodHandler mi =
      new LazyLoadMethodHandler() {
        private boolean loaded = false;
        private boolean loading = false;

        public Object invoke(Object self, Method m, Method proceed, Object[] args)
                      throws Throwable {
          if (!loaded && !loading) {
            if (log.isDebugEnabled())
              log.debug(m.getName() + " on " + id + " is forcing a load from store");

            loading = true;
            try {
              getFromStore(id, cm, (T) self, false);
            } finally {
              loading = false;
            }
            loaded = true;
          }

          try {
            return proceed.invoke(self, args);
          } catch (InvocationTargetException ite) {
            if (log.isDebugEnabled())
              log.debug("Caught ite while invoking '" + proceed + "' on '" + self + "'", ite);
            throw ite.getCause();
          }
        }

        public boolean isLoaded() {
          return loaded;
        }

      };

    try {
      T o = sessionFactory.getProxyMapping(clazz).newInstance();
      cm.getIdField().set(o, Collections.singletonList(id.getId()));
      ((ProxyObject) o).setHandler(mi);
      proxies.put(id, mi);

      if (log.isDebugEnabled())
        log.debug("Dynamic proxy created for " + id);

      return o;
    } catch (Exception e) {
      throw new OtmException("Dynamic proxy instantiation failed", e);
    }
  }

  private Id checkObject(Object o, boolean isUpdate, boolean dupCheck) throws OtmException {
    if (o == null)
      throw new NullPointerException("Null object");

    ClassMetadata<?> cm      = checkClass(o.getClass());
    if (cm.isView() && isUpdate)
      throw new OtmException("View's may not be updated: " + o.getClass());

    Mapper           idField = cm.getIdField();

    if (idField == null)
      throw new OtmException("Must have an id field for " + o.getClass());

    List          ids     = idField.get(o);
    String        id      = null;

    if (ids.size() == 0) {
      IdentifierGenerator generator = idField.getGenerator();

      if (generator == null)
        throw new OtmException("No id generation for " + o.getClass() + " on " + idField);

      if (txn == null)
        throw new OtmException("No active transaction");

      id = generator.generate(cm, txn);
      idField.set(o, Collections.singletonList(id)); // So user can get at it after saving

      if (log.isDebugEnabled())
        log.debug(generator.getClass().getSimpleName() + " generated '" + id + "' for "
                  + o.getClass().getSimpleName());
    } else {
      id = (String) ids.get(0);
    }

    Id oid = new Id(o.getClass(), id);
    if (dupCheck) {
      for (Object ex : new Object[] {deleteMap.get(oid), cleanMap.get(oid), dirtyMap.get(oid)})
        if ((ex != null) && (ex != o))
          throw new OtmException("Session already contains another object instance with id <" 
            + id + ">");
    }
    return oid;
  }

  // For use in sets where we want id equality rather than object equality.
  // eg. associations that are yet to be loaded from the triplestore
  private static class Wrapper {
    private Id     id;
    private Object o;

    public Wrapper(Id id, Object o) {
      this.id   = id;
      this.o    = o;
    }

    public Object get() {
      return o;
    }

    public Id getId() {
      return id;
    }

    public int hashCode() {
      return id.hashCode();
    }

    public boolean equals(Object other) {
      return (other instanceof Wrapper) ? id.equals(((Wrapper) other).id) : false;
    }
  }

  private interface LazyLoadMethodHandler extends MethodHandler {
    public boolean isLoaded();
  }

}
