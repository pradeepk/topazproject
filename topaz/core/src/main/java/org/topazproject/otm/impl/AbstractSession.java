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


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.otm.filter.FilterDefinition;
import org.topazproject.otm.mapping.Mapper;
import org.topazproject.otm.query.Results;

import org.topazproject.otm.OtmException;
import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.Filter;
import org.topazproject.otm.Transaction;
import org.topazproject.otm.SessionFactory;
import org.topazproject.otm.TripleStore;
import org.topazproject.otm.Session;
import org.topazproject.otm.Query;
import org.topazproject.otm.Criteria;

/**
 * An otm session (similar to hibernate session). And similar to hibernate session, not thread
 * safe.
 *
 * @author Pradeep Krishnan
 */
abstract class AbstractSession implements Session {
  private static final Log                    log            = LogFactory.getLog(AbstractSession.class);
  protected final SessionFactoryImpl           sessionFactory;
  protected       Transaction                  txn            = null;
  protected FlushMode flushMode = FlushMode.always;
  protected final Map<String, Filter>            filters        = new HashMap<String, Filter>();

  /**
   * Creates a new Session object.
   *
   * @param sessionFactory the session factory that created this session
   */
  protected AbstractSession(SessionFactoryImpl sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  /*
   * inherited javadoc
   */
  public SessionFactory getSessionFactory() {
    return sessionFactory;
  }

  /*
   * inherited javadoc
   */
  public Transaction beginTransaction() throws OtmException {
    if (txn == null)
      txn = new TransactionImpl(this);
    else
      throw new OtmException("A transaction is already active on this session");

    return txn;
  }

  /**
   * Clears the txn when committed or rolled back.
   */
  void endTransaction() {
    txn = null;
  }

  /*
   * inherited javadoc
   */
  public Transaction getTransaction() {
    return txn;
  }

  /*
   * inherited javadoc
   */
  public void close() throws OtmException {
    clear();

    if (txn != null)
      txn.rollback();

    txn = null;
  }

  /*
   * inherited javadoc
   */
  public void setFlushMode(FlushMode flushMode) {
    this.flushMode = flushMode;
  }

  /*
   * inherited javadoc
   */
  public FlushMode getFlushMode() {
    return this.flushMode;
  }

  /*
   * inherited javadoc
   */
  public Criteria createCriteria(Class clazz) throws OtmException {
    return new Criteria(this, null, null, checkClass(clazz),
                        new ArrayList<Filter>(filters.values()));
  }

  /*
   * inherited javadoc
   */
  public Criteria createCriteria(Criteria criteria, String path)
                          throws OtmException {
    ClassMetadata<?> cm = criteria.getClassMetadata();
    Mapper           m  = cm.getMapperByName(path);

    if (m == null)
      throw new OtmException(path + " is not a valid field name for " + cm);

    return new Criteria(this, criteria, m, checkClass(m.getComponentType()),
                        new ArrayList<Filter>(filters.values()));
  }

  /*
   * inherited javadoc
   */
  public List list(Criteria criteria) throws OtmException {
    if (txn == null)
      throw new OtmException("No transaction active");

    if (flushMode.implies(FlushMode.always))
      flush(); // so that mods are visible to queries

    TripleStore store = sessionFactory.getTripleStore();

    return store.list(criteria, txn);
  }

  /*
   * inherited javadoc
   */
  public Query createQuery(String query) throws OtmException {
    return new QueryImpl(this, query, new ArrayList<Filter>(filters.values()));
  }

  /*
   * inherited javadoc
   */
  public Results doNativeQuery(String query) throws OtmException {
    if (txn == null)
      throw new OtmException("No transaction active");

    if (flushMode.implies(FlushMode.always))
      flush(); // so that mods are visible to queries

    TripleStore store = sessionFactory.getTripleStore();

    return store.doNativeQuery(query, txn);
  }

  /*
   * inherited javadoc
   */
  public void doNativeUpdate(String command) throws OtmException {
    if (txn == null)
      throw new OtmException("No transaction active");

    if (flushMode.implies(FlushMode.always))
      flush(); // so that ordering is preserved

    TripleStore store = sessionFactory.getTripleStore();
    store.doNativeUpdate(command, txn);
  }

  /*
   * inherited javadoc
   */
  public List<String> getIds(List objs) throws OtmException {
    List<String> results = new ArrayList<String>(objs.size());

    for (Object o : objs)
      results.add(getId(o));

    return results;
  }

  /*
   * inherited javadoc
   */
  public String getId(Object o) throws OtmException {
    if (o == null)
      throw new NullPointerException("Null object");

    Class<?> clazz = o.getClass();
    ClassMetadata<?> cm = sessionFactory.getClassMetadata(clazz);

    if (cm == null)
      throw new OtmException("No class metadata found for " + clazz);

    Mapper           idField = cm.getIdField();
    if (idField == null)
      throw new OtmException("No id-field found for " + clazz);

    List          ids     = idField.get(o);
    if (ids.size() == 0)
      throw new OtmException("No id set for " + clazz + " instance " + o);

    return (String) ids.get(0);
  }


  /*
   * inherited javadoc
   */
  public Filter enableFilter(String name) throws OtmException {
    if (filters.containsKey(name))
      return filters.get(name);

    FilterDefinition fd = sessionFactory.getFilterDefinition(name);
    if (fd == null)
      return null;

    Filter f = fd.createFilter(this);
    filters.put(f.getName(), f);
    return f;
  }

  /*
   * inherited javadoc
   */
  public Filter enableFilter(FilterDefinition fd) throws OtmException {
    if (sessionFactory.getFilterDefinition(fd.getFilterName()) != null)
      throw new OtmException("a filter with the name '" + fd.getFilterName() +
                             "' is registered with the session-factory");
    if (filters.containsKey(fd.getFilterName()))
      throw new OtmException("a filter with the name '" + fd.getFilterName() +
                             "' has already been enabled");

    Filter f = fd.createFilter(this);
    filters.put(f.getName(), f);
    return f;
  }

  /*
   * inherited javadoc
   */
  public void disableFilter(String name) throws OtmException {
    filters.remove(name);
  }

  /*
   * inherited javadoc
   */
  public Set<String> listFilters() {
    return new HashSet<String>(filters.keySet());
  }

  protected <T> ClassMetadata<T> checkClass(Class<? extends T> clazz) throws OtmException {
    ClassMetadata<T> cm = sessionFactory.getClassMetadata(clazz);

    if (cm == null)
      throw new OtmException("No class metadata found for " + clazz);

    if (cm.getModel() == null && !cm.isView())
      throw new OtmException("No graph/model found for " + clazz);

    return cm;
  }
}
