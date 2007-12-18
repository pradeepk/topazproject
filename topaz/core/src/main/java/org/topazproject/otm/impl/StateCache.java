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

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.Session;
import org.topazproject.otm.mapping.Mapper;

/**
 * A cache used to track modificationsto an object instance.
 *
 * @author Pradeep Krishnan
 */
class StateCache {
  private static final Log                       log    = LogFactory.getLog(StateCache.class);
  private Map<ObjectReference<?>, InstanceState> states =
    new HashMap<ObjectReference<?>, InstanceState>(1001);
  private ReferenceQueue<ObjectReference<?>>     queue  = new ReferenceQueue<ObjectReference<?>>();

  /**
   * Insert an object into the cache.
   *
   * @param o the object to insert
   * @param cm it's class metadata
   * @param session the Session that is requesting the insert
   */
  public void insert(Object o, ClassMetadata cm, Session session) throws OtmException {
    expunge();
    states.put(new ObjectReference(o, queue), new InstanceState(o, cm, session));
  }

  /**
   * Update an object's state. If the object is in the cache, the fields that are updated are
   * returned, Otherwise the object is inserted and a null is returned to indicate this condition.
   *
   * @param o the object to update/insert
   * @param cm it's class metaday
   * @param session the Session that is requestin the update
   *
   * @return the collection of fields that were updated or null
   */
  public Collection<Mapper> update(Object o, ClassMetadata cm, Session session)
      throws OtmException {
    expunge();

    InstanceState is = states.get(new ObjectReference(o));

    if (is != null)
      return is.update(o, cm, session);

    states.put(new ObjectReference(o, queue), new InstanceState(o, cm, session));

    return null;
  }

  /**
   * Removes an object from the cache.
   *
   * @param o the object to remove
   */
  public void remove(Object o) {
    expunge();
    states.remove(new ObjectReference(o));
  }

  private void expunge() {
    int                count = 0;
    ObjectReference<?> ref;

    while ((ref = (ObjectReference<?>) queue.poll()) != null) {
      states.remove(ref);
      count++;
    }

    if (log.isDebugEnabled() && (count > 0))
      log.debug("Expunged " + count + " objects from states-cache. Size is now " + states.size());
  }

  /**
   * The state of an object as last seen by a Session.
   */
  private static class InstanceState {
    private final Map<Mapper, List<String>> vmap; // serialized field values
    private Map<String, List<String>>       pmap; // serialized predicate map values

    public <T>InstanceState(T instance, ClassMetadata<T> cm, Session session) throws OtmException {
      vmap                   = new HashMap<Mapper, List<String>>();

      for (Mapper m : cm.getFields()) {
        if (m.getMapperType() == Mapper.MapperType.PREDICATE_MAP)
          pmap = (Map<String, List<String>>) m.getRawValue(instance, true);
        else {
          List<String> nv =
            !m.isAssociation() ? m.get(instance) : session.getIds(m.get(instance));
          vmap.put(m, nv);
        }
      }
    }

    public <T> Collection<Mapper> update(T instance, ClassMetadata<T> cm, Session session)
        throws OtmException {
      Collection<Mapper> mappers = new ArrayList<Mapper>();

      for (Mapper m : cm.getFields()) {
        if (m.getMapperType() == Mapper.MapperType.PREDICATE_MAP) {
          Map<String, List<String>> nv = (Map<String, List<String>>) m.getRawValue(instance, true);
          boolean                   eq = (pmap == null) ? (nv == null) : pmap.equals(nv);

          if (!eq) {
            pmap      = nv;
            mappers   = cm.getFields(); // all fields since predicate-map is a wild-card

            break;
          }
        } else {
          List<String> ov = vmap.get(m);
          List<String> nv =
            !m.isAssociation() ? m.get(instance) : session.getIds(m.get(instance));
          boolean      eq = (ov == null) ? (nv == null) : ov.equals(nv);

          if (!eq) {
            vmap.put(m, nv);
            mappers.add(m);
          }
        }
      }

      return mappers;
    }
  }

  /**
   * A weak reference to the object with identity hash code and instance equality.
   * This reference is used as the key for the cache so that when the application
   * stops refering the object, it can be removed from our cache also.
   */
  private static class ObjectReference<T> extends WeakReference<T> {
    private final int hash;

    public ObjectReference(T o) {
      super(o);
      hash = System.identityHashCode(o);
    }

    public ObjectReference(T o, ReferenceQueue<?super T> queue) {
      super(o, queue);
      hash = System.identityHashCode(o);
    }

    public boolean equals(Object o) {
      return (o instanceof ObjectReference)
              && ((o == this) || (get() == ((ObjectReference<T>) o).get()));
    }

    public int hashCode() {
      return hash;
    }
  }
}
