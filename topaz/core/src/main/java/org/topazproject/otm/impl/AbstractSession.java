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
   *
   * @throws OtmException on an error
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
   *
   * @throws OtmException on an error
   */
  public void close() throws OtmException {
    clear();

    if (txn != null)
      txn.rollback();

    txn = null;
  }

  /**
   * Sets the FlushMode for this session.
   *
   * @param flushMode the FlushMode value to set
   */
  public void setFlushMode(FlushMode flushMode) {
    this.flushMode = flushMode;
  }

  /**
   * Gets the current FlushMode.
   *
   * @return the current FlushMode
   */
  public FlushMode getFlushMode() {
    return this.flushMode;
  }

  /**
   * Creates the 'criteria' for retrieving a set of objects of a class.
   *
   * @param clazz the class
   *
   * @return a newly created Criteria object
   *
   * @throws OtmException on an error
   */
  public Criteria createCriteria(Class clazz) throws OtmException {
    return new Criteria(this, null, null, checkClass(clazz),
                        new ArrayList<Filter>(filters.values()));
  }

  /**
   * Creates a 'sub-criteria' for retrieving a set of objects for an association in a parent
   * class. Usually called by {@link
   * org.topazproject.otm.Criteria#createCriteria(java.lang.String) Criteria#createCriteria}
   *
   * @param criteria the parent class criteria
   * @param path path to the associatyion field
   *
   * @return a newly created Criteria.
   *
   * @throws OtmException on an error
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

  /**
   * Gets a list of objects based on a criteria.
   *
   * @param criteria the criteria
   *
   * @return the results list
   *
   * @throws OtmException on an error
   */
  public List list(Criteria criteria) throws OtmException {
    if (txn == null)
      throw new OtmException("No transaction active");

    if (flushMode == FlushMode.always)
      flush(); // so that mods are visible to queries

    TripleStore store = sessionFactory.getTripleStore();

    return store.list(criteria, txn);
  }

  /**
   * Create an OQL query.
   *
   * @param query the OQL query
   * @return the query object
   */
  public Query createQuery(String query) throws OtmException {
    return new QueryImpl(this, query, new ArrayList<Filter>(filters.values()));
  }

  /**
   * Execute a native(ITQL, SPARQL etc.) query.
   *
   * @param query the native query
   * @return the results object
   * @throws OtmException on an error
   */
  public Results doNativeQuery(String query) throws OtmException {
    if (txn == null)
      throw new OtmException("No transaction active");

    if (flushMode == FlushMode.always)
      flush(); // so that mods are visible to queries

    TripleStore store = sessionFactory.getTripleStore();

    return store.doNativeQuery(query, txn);
  }

  /**
   * Execute a native(ITQL, SPARQL etc.) update.
   *
   * @param command the native command(s) to execute
   * @throws OtmException on an error
   */
  public void doNativeUpdate(String command) throws OtmException {
    if (txn == null)
      throw new OtmException("No transaction active");

    if (flushMode == FlushMode.always)
      flush(); // so that ordering is preserved

    TripleStore store = sessionFactory.getTripleStore();
    store.doNativeUpdate(command, txn);
  }

  /**
   * Gets the ids for a list of objects.
   *
   * @param objs list of objects
   *
   * @return the list of ids
   *
   * @throws OtmException on an error
   */
  public List<String> getIds(List objs) throws OtmException {
    List<String> results = new ArrayList<String>(objs.size());

    for (Object o : objs)
      results.add(getId(o));

    return results;
  }

  /**
   * Gets the id of an object.
   *
   * @param o the object
   *
   * @return the id
   *
   * @throws OtmException on an error
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


  /** 
   * Enable the named filter. This does not affect existing queries or criteria.
   * 
   * @param name the name of the filter to enable
   * @return the enabled filter, or null if no filter definition can be found.
   * @throws OtmException on an error
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

  /** 
   * Enable the filter defined by the specified filter-definition. Unlike {@link
   * #enableFilter(java.lang.String) enableFilter(String)} this filter-definition does not have to
   * be pre-registered on the session-factory. The filter can be disabled by name, just like with
   * filters from pre-registered filter-definitions.
   *
   * <p>This does not affect existing queries or criteria.
   * 
   * @param fd the filter-definition whose filter to enable
   * @return the enabled filter
   * @throws OtmException if a filter-definition with the same name has been registered with the
   *                      session-factory or a filter with the same name is already enabled
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

  /** 
   * Disable the named filter. This does nothing if no filter by the given name has been enabled.
   * This does not affect existing queries or criteria.
   * 
   * @param name the name of the filter to disable
   */
  public void disableFilter(String name) throws OtmException {
    filters.remove(name);
  }

  /** 
   * Get the set of enabled filters' names. 
   * 
   * @return the names of the enabled filters
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
