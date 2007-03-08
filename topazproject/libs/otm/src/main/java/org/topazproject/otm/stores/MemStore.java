package org.topazproject.otm.stores;

import java.net.URI;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.Connection;
import org.topazproject.otm.SessionFactory;
import org.topazproject.otm.Transaction;
import org.topazproject.otm.TripleStore;
import org.topazproject.otm.annotations.Rdf;
import org.topazproject.otm.mapping.Mapper;

/**
 * An abstraction to represent triple stores.
 *
 * @author Pradeep Krishnan
 */
public class MemStore implements TripleStore {
  private Storage             storage    = new Storage();
  private Map<String, String> inverseMap = new HashMap<String, String>();

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public Connection openConnection() {
    return new MemStoreConnection(storage);
  }

  /**
   * DOCUMENT ME!
   *
   * @param con DOCUMENT ME!
   */
  public void closeConnection(Connection con) {
  }

  /**
   * DOCUMENT ME!
   *
   * @param cm DOCUMENT ME!
   * @param id DOCUMENT ME!
   * @param o DOCUMENT ME!
   * @param txn DOCUMENT ME!
   */
  public void insert(ClassMetadata cm, String id, Object o, Transaction txn) {
    MemStoreConnection msc     = (MemStoreConnection) txn.getConnection();
    Storage            storage = msc.getStorage();

    storage.insert(cm.getModel(), id, Rdf.rdf + "type", cm.getTypes().toArray(new String[0]));

    for (Mapper p : cm.getFields()) {
      if (p.hasInverseUri())
        continue;

      if (p.getSerializer() != null)
        storage.insert(cm.getModel(), id, p.getUri(), (String[]) p.get(o).toArray(new String[0]));
      else
        storage.insert(cm.getModel(), id, p.getUri(),
                       txn.getSession().getIds(p.get(o)).toArray(new String[0]));
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @param cm DOCUMENT ME!
   * @param id DOCUMENT ME!
   * @param txn DOCUMENT ME!
   */
  public void delete(ClassMetadata cm, String id, Transaction txn) {
    MemStoreConnection msc     = (MemStoreConnection) txn.getConnection();
    Storage            storage = msc.getStorage();
    String             model   = cm.getModel();

    for (String uri : cm.getUris())
      storage.remove(model, id, uri);
  }

  /**
   * DOCUMENT ME!
   *
   * @param cm DOCUMENT ME!
   * @param id DOCUMENT ME!
   * @param txn DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   *
   * @throws RuntimeException DOCUMENT ME!
   */
  public ResultObject get(ClassMetadata cm, String id, Transaction txn) {
    MemStoreConnection                     msc     = (MemStoreConnection) txn.getConnection();
    Storage                                storage = msc.getStorage();

    Map<String, Map<String, List<String>>> values  =
      new HashMap<String, Map<String, List<String>>>();
    Map<String, List<String>>              value   = new HashMap<String, List<String>>();

    values.put(id, value);

    String model = cm.getModel();

    for (Mapper p : cm.getFields()) {
      String uri = p.getUri();

      if (!p.hasInverseUri())
        value.put(uri, new ArrayList<String>(storage.getProperty(model, id, uri)));
      else {
        String inverseUri = inverseMap.get(uri);

        if (inverseUri == null)
          throw new RuntimeException("No inverse uri defined for " + uri);

        String inverseModel = p.getInverseModel();

        if (inverseModel == null)
          inverseModel = model;

        Set<String> invProps = storage.getInverseProperty(inverseModel, id, inverseUri);
        value.put(uri, new ArrayList<String>(invProps));

        for (String subj : invProps) {
          Map<String, List<String>> v = values.get(subj);

          if (v == null)
            values.put(subj, v = new HashMap<String, List<String>>());

          List<String> objs = v.get(inverseUri);

          if (objs == null)
            v.put(inverseUri, objs = new ArrayList<String>());

          objs.add(id);
        }
      }
    }

    return instantiate(txn.getSession().getSessionFactory(), cm.getSourceClass(), id, values);
  }

  /**
   * DOCUMENT ME!
   *
   * @param uri DOCUMENT ME!
   * @param inverse DOCUMENT ME!
   */
  public void setInverseUri(String uri, String inverse) {
    inverseMap.put(uri, inverse);
    inverseMap.put(inverse, uri);
  }

  /**
   * DOCUMENT ME!
   *
   * @param uri DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public String getInverseUri(String uri) {
    return inverseMap.get(uri);
  }

  /*
     public T <Collection<T>> find(Class<T> clazz, List<Criteria> criteria,
         List<Field> orderBy, long offset, long size);
   */
  private ResultObject instantiate(SessionFactory sessionFactory, Class clazz, String id,
                                   Map<String, Map<String, List<String>>> triples) {
    Map<String, List<String>> props = triples.get(id);
    List<String>              types = props.get(Rdf.rdf + "type");

    if (types.size() == 0)
      return null;

    clazz = sessionFactory.mostSpecificSubClass(clazz, types);

    ClassMetadata cm = sessionFactory.getClassMetadata(clazz);
    ResultObject  ro;

    try {
      ro             = new ResultObject(clazz.newInstance(), id);
    } catch (Exception e) {
      throw new RuntimeException("instantiation failed", e);
    }

    cm.getIdField().set(ro.o, Collections.singletonList(id));

    // A field could be of rdf:type. So remove the values used in class identification
    // from the set of rdf:type values before running through and setting field values. 
    types.removeAll(cm.getTypes());

    for (Mapper p : cm.getFields()) {
      if (p.getSerializer() != null)
        p.set(ro.o, props.get(p.getUri()));
      else
        ro.unresolvedAssocs.put(p, props.get(p.getUri()));
    }

    // Put back what we removed
    types.addAll(cm.getTypes());

    return ro;
  }

  private static class PropertyId {
    private String model;
    private String id;
    private String uri;

    public PropertyId(String model, String id, String uri) {
      this.model   = model;
      this.id      = id;
      this.uri     = uri;
    }

    public String getModel() {
      return model;
    }

    public String getId() {
      return id;
    }

    public String getUri() {
      return uri;
    }

    public boolean equals(Object other) {
      if (!(other instanceof PropertyId))
        return false;

      PropertyId o = (PropertyId) other;

      return model.equals(o.model) && id.equals(o.id) && uri.equals(o.uri);
    }

    public int hashCode() {
      return model.hashCode() + id.hashCode() + uri.hashCode();
    }
  }

  private static class Storage {
    private Map<String, Map<String, Map<String, Set<String>>>> data           =
      new HashMap<String, Map<String, Map<String, Set<String>>>>();
    private Storage                                            backingStore   = null;
    private Set<PropertyId>                                    pendingDeletes;
    private Set<PropertyId>                                    pendingInserts;

    public Storage() {
    }

    public Storage(Storage backingStore) {
      this.backingStore                                                       = backingStore;

      if (backingStore != null) {
        pendingDeletes   = new HashSet<PropertyId>();
        pendingInserts   = new HashSet<PropertyId>();
      }
    }

    public void insert(String model, String id, String uri, String[] val) {
      Set<String> data = getProperty(model, id, uri);

      for (String v : val)
        data.add(v);

      if (backingStore != null) {
        PropertyId p = new PropertyId(model, id, uri);
        pendingInserts.add(p);
      }
    }

    public void remove(String model, String id, String uri) {
      Set<String> data = getProperty(model, id, uri);
      data.clear();

      if (backingStore != null) {
        PropertyId p = new PropertyId(model, id, uri);
        pendingDeletes.add(new PropertyId(model, id, uri));
      }
    }

    public Set<String> getProperty(PropertyId prop) {
      return getProperty(prop.getModel(), prop.getId(), prop.getUri());
    }

    public Set<String> getProperty(String model, String id, String uri) {
      Map<String, Set<String>> subjectData = getSubjectData(model, id);
      Set<String>              val         = subjectData.get(uri);

      if (val == null) {
        val = new HashSet<String>();
        subjectData.put(uri, val);
      }

      if (backingStore != null) {
        if (!pendingDeletes.contains(new PropertyId(model, id, uri)))
          synchronized (backingStore) {
            val.addAll(backingStore.getProperty(model, id, uri));
          }
      }

      return val;
    }

    public Set<String> getInverseProperty(String model, String id, String uri) {
      Set<String>                           results   = new HashSet<String>();
      Map<String, Map<String, Set<String>>> modelData = data.get(model);

      if (modelData != null) {
        for (String subject : modelData.keySet()) {
          Set<String> objs = modelData.get(subject).get(uri);

          if ((objs != null) && objs.contains(id))
            results.add(subject);
        }
      }

      if (backingStore != null) {
        synchronized (backingStore) {
          results.addAll(backingStore.getInverseProperty(model, id, uri));
        }

        for (Iterator<String> it = results.iterator(); it.hasNext();) {
          if (pendingDeletes.contains(new PropertyId(model, it.next(), uri)))
            it.remove();
        }
      }

      return results;
    }

    public void commit() {
      if (backingStore == null)
        return;

      synchronized (backingStore) {
        for (PropertyId pd : pendingDeletes)
          backingStore.remove(pd.getModel(), pd.getId(), pd.getUri());

        for (PropertyId pd : pendingInserts)
          backingStore.insert(pd.getModel(), pd.getId(), pd.getUri(),
                              getProperty(pd).toArray(new String[0]));

        backingStore.commit();
      }

      pendingDeletes.clear();
      pendingInserts.clear();
      data.clear();
    }

    public void rollback() {
      if (backingStore == null)
        return;

      pendingDeletes.clear();
      pendingInserts.clear();
      data.clear();
    }

    private Map<String, Set<String>> getSubjectData(String model, String id) {
      Map<String, Map<String, Set<String>>> modelData   = getModelData(model);
      Map<String, Set<String>>              subjectData = modelData.get(id);

      if (subjectData == null) {
        subjectData = new HashMap<String, Set<String>>();
        modelData.put(id, subjectData);
      }

      return subjectData;
    }

    private Map<String, Map<String, Set<String>>> getModelData(String model) {
      Map<String, Map<String, Set<String>>> modelData = data.get(model);

      if (modelData == null) {
        modelData = new HashMap<String, Map<String, Set<String>>>();
        data.put(model, modelData);
      }

      return modelData;
    }
  }

  private static class MemStoreConnection implements Connection {
    private Storage storage;

    public MemStoreConnection(Storage backingStore) {
      this.storage = new Storage(backingStore);
    }

    public void beginTransaction() {
    }

    public void endTransaction() {
    }

    public void commit() {
      storage.commit();
    }

    public void rollback() {
      storage.rollback();
    }

    public Storage getStorage() {
      return storage;
    }
  }
}
