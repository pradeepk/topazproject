/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.otm.stores;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.Session;
import org.topazproject.otm.SessionFactory;
import org.topazproject.otm.Transaction;
import org.topazproject.otm.TripleStore;
import org.topazproject.otm.criterion.CriterionBuilder;
import org.topazproject.otm.mapping.Mapper;

/**
 * A common base class for triple-store impls.
 *
 * @author Pradeep Krishnan
 */
public abstract class AbstractTripleStore implements TripleStore {
  /**
   * Map of Criterion Builders for store specific functions. 
   */
  protected Map<String, CriterionBuilder> critBuilders = new HashMap<String, CriterionBuilder>();

  /**
   * Instantiate an object based on the statements about it found in the triple-store
   *
   * @param session the session under which this object is instantiated
   * @param instance the instance to which values are to be set or null
   * @param cm the class metadata describing
   * @param id the object's id/subject-uri
   * @param fvalues p-o mapping with s being the id
   * @param rvalues p-s mapping with o being the id
   * @param types rdf:type look-ahead for o values (crosses rdf:List, rdf:Bag)
   *
   * @return the object instance
   *
   * @throws OtmException on an error
   */
  protected <T> T instantiate(Session session, T instance, ClassMetadata<T> cm, String id,
                              Map<String, List<String>> fvalues,
                              Map<String, List<String>> rvalues, Map<String, Set<String>> types)
                        throws OtmException {
    SessionFactory sf    = session.getSessionFactory();
    Class<T>       clazz = cm.getSourceClass();

    try {
      if (instance == null)
        instance = clazz.newInstance();
    } catch (Exception e) {
      throw new OtmException("Failed to instantiate " + clazz, e);
    }

    cm.getIdField().set(instance, Collections.singletonList(id));

    // re-map values based on the rdf:type look ahead
    Map<Mapper, List<String>> mvalues = new HashMap();
    boolean                   inverse = false;

    for (Map<String, List<String>> values : new Map[] { fvalues, rvalues }) {
      for (String p : values.keySet()) {
        for (String o : values.get(p)) {
          Mapper m = cm.getMapperByUri(sf, p, inverse, types.get(o));

          if (m != null) {
            List<String> v = mvalues.get(m);

            if (v == null)
              mvalues.put(m, v = new ArrayList<String>());

            v.add(o);
          }
        }
      }

      inverse = true;
    }

    // now assign values to fields
    for (Mapper m : mvalues.keySet()) {
      if (!m.isAssociation())
        m.set(instance, mvalues.get(m));
      else {
        List assocs = new ArrayList();

        for (String val : mvalues.get(m)) {
          // lazy load
          Class cls = m.getComponentType();

          Set<String> t = types.get(val);

          if ((t != null) && (t.size() > 0))
            cls = sf.mostSpecificSubClass(cls, t);
          else {
            ClassMetadata c = sf.getClassMetadata(cls);
            if ((c != null) && (c.getType() != null))
              cls = null;
          }

          if (cls != null) {
            Object a = session.load(cls, val);

            if (a != null)
              assocs.add(a);
          }
        }

        m.set(instance, assocs);
      }
    }

    boolean found = false;

    for (Mapper m : cm.getFields()) {
      if (m.getMapperType() == Mapper.MapperType.PREDICATE_MAP) {
        found = true;

        break;
      }
    }

    if (found) {
      Map<String, List<String>> map = new HashMap<String, List<String>>();
      map.putAll(fvalues);

      for (Mapper m : cm.getFields()) {
        if (m.getMapperType() == Mapper.MapperType.PREDICATE_MAP)
          continue;

        if (m.hasInverseUri())
          continue;

        map.remove(m.getUri());
      }

      for (Mapper m : cm.getFields()) {
        if (m.getMapperType() == Mapper.MapperType.PREDICATE_MAP)
          m.setRawValue(instance, map);
      }
    }

    return instance;
  }

  /*
   * inherited javadoc
   */
  public <T> void insert(ClassMetadata<T> cm, String id, T o, Transaction txn) throws OtmException {
    insert(cm, cm.getFields(), id, o, txn);
  }

  /*
   * inherited javadoc
   */
  public <T> void delete(ClassMetadata<T> cm, String id, T o, Transaction txn) throws OtmException {
    delete(cm, cm.getFields(), id, o, txn);
  }

  /*
   * inherited javadoc
   */
  public CriterionBuilder getCriterionBuilder(String func)
                                       throws OtmException {
    return critBuilders.get(func);
  }

  /*
   * inherited javadoc
   */
  public void setCriterionBuilder(String func, CriterionBuilder builder)
                           throws OtmException {
    critBuilders.put(func, builder);
  }
}
