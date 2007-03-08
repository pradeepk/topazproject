package org.topazproject.otm;

import java.lang.reflect.Method;

import java.net.URI;

import java.util.ArrayList;
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

import org.topazproject.otm.annotations.Rdf;
import org.topazproject.otm.mapping.Mapper;

/**
 * An otm session (similar to hibernate session). And similar to hibernate session, not thread
 * safe.
 *
 * @author Pradeep Krishnan
 */
public class Session {
  private static final Log log            = LogFactory.getLog(Session.class);
  private SessionFactory   sessionFactory;
  private Transaction      txn            = null;

  // xxx: revisit. taking advantage of subject-uri ==> Object one-to-one association.
  private Map<String, Object>       cleanMap        = new HashMap<String, Object>();
  private Map<String, Object>       dirtyMap        = new HashMap<String, Object>();
  private Map<String, Object>       deleteMap       = new HashMap<String, Object>();
  private Set<String>               pristineProxies = new HashSet<String>();
  private Map<String, Set<Wrapper>> associations    = new HashMap<String, Set<Wrapper>>();
  private Set<String>               currentIds      = new HashSet<String>();

/**
   * Creates a new Session object.
   *
   * @param sessionFactory the session factory that created this session
   */
  public Session(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  /**
   * Gets the session factory that created this session.
   *
   * @return the session factory
   */
  public SessionFactory getSessionFactory() {
    return sessionFactory;
  }

  /**
   * Begins a new transaction. All session usage is within transaction scope.
   *
   * @return the transaction
   */
  public Transaction beginTransaction() {
    if (txn == null)
      txn = new Transaction(this);

    return txn;
  }

  /**
   * Gets the current transaction.
   *
   * @return the transaction
   */
  public Transaction getTransaction() {
    return txn;
  }

  /**
   * Close and release all resources.
   */
  public void close() {
    clear();

    if (txn != null)
      txn.endTransaction();

    txn = null;
  }

  /**
   * Flushes all modified objects to the triple-store. Usually called automatically on a
   * transaction commit.
   *
   * @throws RuntimeException on an error
   */
  public void flush() {
    if (txn == null)
      throw new RuntimeException("No active transaction");

    for (Map.Entry<String, Object> e : deleteMap.entrySet())
      write(e.getKey(), e.getValue(), true);

    for (Map.Entry<String, Object> e : dirtyMap.entrySet())
      write(e.getKey(), e.getValue(), false);

    deleteMap.clear();
    cleanMap.putAll(dirtyMap);
    dirtyMap.clear();
  }

  /**
   * Clear persistence state of all objects.
   */
  public void clear() {
    cleanMap.clear();
    dirtyMap.clear();
    deleteMap.clear();
    pristineProxies.clear();
    associations.clear();
  }

  /**
   * Marks an object for storage. All associated objects are stored too.
   *
   * @param o the object to store
   *
   * @return the object id
   */
  public String saveOrUpdate(Object o) {
    String id = checkObject(o);
    sync(o, id, false, true, true);

    return id;
  }

  /**
   * Mark an object for deletion from storage. All associated objects are deleted too.
   *
   * @param o the object to delete
   *
   * @return the object id.
   */
  public String delete(Object o) {
    String id = checkObject(o);

    if (currentIds.contains(id))
      return id; // loop

    try {
      currentIds.add(id);

      cleanMap.remove(id);
      dirtyMap.remove(id);
      deleteMap.put(id, o);

      ClassMetadata cm     = sessionFactory.getClassMetadata(o.getClass());
      Set<Wrapper>  assocs = new HashSet<Wrapper>();

      for (Mapper p : cm.getFields()) {
        if (p.getSerializer() != null)
          continue;

        for (Object ao : p.get(o))
          assocs.add(new Wrapper(checkObject(ao), ao));
      }

      Set<Wrapper> old = associations.remove(id);

      if (old != null)
        assocs.addAll(old);

      for (Wrapper ao : assocs)
        delete(ao.get());
    } finally {
      currentIds.remove(id);
    }

    return id;
  }

  /**
   * Loads an object from the session or a newly created dynamic proxy for it. Does not hit
   * the triplestore.
   *
   * @param <T> the type of object
   * @param clazz the class of the object
   * @param id the id of the object
   *
   * @return the object or null if deleted from session
   */
  public <T> T load(Class<T> clazz, String id) {
    Object o = deleteMap.get(id);

    if (o != null)
      return null;

    o = cleanMap.get(id);

    if (o == null)
      o = dirtyMap.get(id);

    if (o == null) {
      o = newDynamicProxy(clazz, id, checkClass(clazz));
      cleanMap.put(id, o);
    }

    return clazz.cast(o);
  }

  /**
   * Gets an object from the session or from the triple store.
   *
   * @param <T> the type of the object
   * @param clazz the class of the object
   * @param id the id of the object
   *
   * @return the object or null if deleted or does not exist in store
   */
  public <T> T get(Class<T> clazz, String id) {
    Object o = deleteMap.get(id);

    if (o != null)
      return null;

    o = cleanMap.get(id);

    if (o == null)
      o = dirtyMap.get(id);

    if (o == null) {
      o = getFromStore(clazz, id, checkClass(clazz));

      if (o != null)
        cleanMap.put(id, o);
    }

    return clazz.cast(o);
  }

  /**
   * Gets the ids for a list of objects.
   *
   * @param objs list of objects
   *
   * @return the list of ids
   */
  public List<String> getIds(List objs) {
    List<String> results = new ArrayList<String>(objs.size());

    for (Object o : objs)
      results.add(checkObject(o));

    return results;
  }

  private void write(String id, Object o, boolean delete) {
    boolean       pristineProxy = pristineProxies.contains(id);
    ClassMetadata cm            = sessionFactory.getClassMetadata(o.getClass());
    TripleStore   store         = sessionFactory.getTripleStore();

    if (delete || !pristineProxy) {
      if (log.isDebugEnabled())
        log.debug("deleting from store: " + id);

      store.delete(cm, id, txn);
    }

    if (!delete && !pristineProxy) {
      if (log.isDebugEnabled())
        log.debug("inserting into store: " + id);

      store.insert(cm, id, o, txn);
    }
  }

  private Object getFromStore(Class clazz, String id, ClassMetadata cm) {
    if (txn == null)
      throw new RuntimeException("No transaction active");

    TripleStore              store = sessionFactory.getTripleStore();

    TripleStore.ResultObject ro    = store.get(cm, id, txn);

    return (ro == null) ? null : instantiate(ro);
  }

  private Object instantiate(TripleStore.ResultObject ro) {
    Set<Wrapper> allAssocs = new HashSet<Wrapper>();

    for (Mapper p : ro.unresolvedAssocs.keySet()) {
      List assocs = new ArrayList();

      for (String val : ro.unresolvedAssocs.get(p)) {
        // lazy load
        Object a = load(p.getComponentType(), val);

        if (a != null) {
          assocs.add(a);
          allAssocs.add(new Wrapper(val, a));
        }
      }

      p.set(ro.o, assocs);
    }

    for (Mapper p : ro.resolvedAssocs.keySet()) {
      List assocs = new ArrayList();

      for (TripleStore.ResultObject val : ro.resolvedAssocs.get(p)) {
        Object a = sync(instantiate(val), val.id, true, false, false);

        if (a != null) {
          assocs.add(a);
          allAssocs.add(new Wrapper(val.id, a));
        }
      }

      p.set(ro.o, assocs);
    }

    associations.put(ro.id, allAssocs);

    return ro.o;
  }

  private Object sync(final Object other, final String id, final boolean merge,
                      final boolean update, final boolean cascade) {
    if (currentIds.contains(id))
      return null; // loop and hence the return value is unused

    Object o = null;

    try {
      currentIds.add(id);
      o = deleteMap.remove(id);

      if (o == null)
        o = dirtyMap.get(id);

      boolean dirtyExists = o != null;

      if (!dirtyExists)
        o = cleanMap.get(id);

      if (merge && (o != null) && (other != o))
        copy(o, other);
      else
        o = other;

      if (update || dirtyExists) {
        dirtyMap.put(id, o); // re-put since it may have been deleted
        cleanMap.remove(id);
      } else {
        cleanMap.put(id, o);
      }

      if (cascade) {
        ClassMetadata cm     = sessionFactory.getClassMetadata(o.getClass());
        Set<Wrapper>  assocs = new HashSet<Wrapper>();

        for (Mapper p : cm.getFields()) {
          if (p.getSerializer() != null)
            continue;

          for (Object ao : p.get(o))
            assocs.add(new Wrapper(checkObject(ao), ao));
        }

        Set<Wrapper> old = associations.put(id, assocs);

        if (old != null) {
          old.removeAll(assocs);

          for (Wrapper ao : old)
            delete(ao.get());
        }

        for (Wrapper ao : assocs)
          sync(ao.get(), ao.id(), merge, update, cascade);
      }
    } finally {
      currentIds.remove(id);
    }

    return o;
  }

  private Object copy(Object o, Object other) {
    ClassMetadata ocm = checkClass(other.getClass());
    ClassMetadata cm  = checkClass(o.getClass());

    if (!cm.getSourceClass().isAssignableFrom(ocm.getSourceClass()))
      throw new RuntimeException(cm.toString() + " is not assignable from " + ocm);

    for (Mapper p : cm.getFields()) {
      Mapper op = ocm.getMapperByUri(p.getUri());

      if (op == null)
        continue;

      p.setRawValue(o, op.getRawValue(other, false));
    }

    return o;
  }

  private Object newDynamicProxy(final Class clazz, final String id, final ClassMetadata cm) {
    MethodHandler mi =
      new MethodHandler() {
        private Object loaded = null;

        public Object invoke(Object self, Method m, Method proceed, Object[] args)
                      throws Throwable {
          if (loaded == null) {
            log.info(m.getName() + " on " + id + " is forcing a load from store");
            loaded = getFromStore(clazz, id, cm);
            pristineProxies.remove(id);
          }

          return m.invoke(loaded, args);
        }
      };

    try {
      Object o = sessionFactory.getProxyMapping(clazz).newInstance();
      cm.getIdField().set(o, Collections.singletonList(id));
      ((ProxyObject) o).setHandler(mi);
      pristineProxies.add(id);

      return o;
    } catch (Exception e) {
      throw new RuntimeException("Dynamic proxy instantiation failed", e);
    }
  }

  private String checkObject(Object o) {
    if (o == null)
      throw new RuntimeException("Null object");

    ClassMetadata cm      = checkClass(o.getClass());

    Mapper        idField = cm.getIdField();
    List          ids     = idField.get(o);

    if (ids.size() == 0)
      throw new RuntimeException("No id generation support yet " + o.getClass());

    return (String) ids.get(0);
  }

  private ClassMetadata checkClass(Class clazz) {
    ClassMetadata cm = sessionFactory.getClassMetadata(clazz);

    if (cm == null) {
      Class c = sessionFactory.getProxyMapping(clazz);

      if (c != null)
        cm = sessionFactory.getClassMetadata(c);

      if (cm == null)
        throw new RuntimeException("No class metadata found for " + clazz);
    }

    if (!cm.isEntity())
      throw new RuntimeException("No id-field or rdf:type or graph/model found for " + clazz);

    return cm;
  }

  // For use in sets where we want id equality rather than object equality.
  // eg. associations that are yet to be loaded from the triplestore
  private static class Wrapper {
    private String idy;
    private Object o;

    public Wrapper(String id, Object o) {
      this.idy   = id;
      this.o     = o;
    }

    public Object get() {
      return o;
    }

    public String id() {
      return idy;
    }

    public int hashCode() {
      return idy.hashCode();
    }

    public boolean equals(Object other) {
      return (other instanceof Wrapper) ? idy.equals(((Wrapper) other).idy) : false;
    }
  }
}
