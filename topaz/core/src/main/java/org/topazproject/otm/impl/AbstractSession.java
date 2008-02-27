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

import javax.transaction.Synchronization;
import javax.transaction.TransactionManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.otm.filter.FilterDefinition;
import org.topazproject.otm.mapping.Binder;
import org.topazproject.otm.mapping.Mapper;
import org.topazproject.otm.query.Results;

import org.topazproject.otm.OtmException;
import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.Connection;
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
  private static final Log                      log = LogFactory.getLog(AbstractSession.class);

  protected final SessionFactoryImpl            sessionFactory;
  protected       TransactionImpl               locTxn         = null;
  protected       javax.transaction.Transaction jtaTxn         = null;
  protected       boolean                       txnIsRO        = false;
  protected       Connection                    tsCon          = null;
  protected       Connection                    bsCon          = null;
  protected       FlushMode                     flushMode      = FlushMode.always;
  protected final Map<String, Filter>           filters        = new HashMap<String, Filter>();

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
    return beginTransaction(false);
  }

  /*
   * inherited javadoc
   */
  public Transaction beginTransaction(boolean readOnly) throws OtmException {
    if (jtaTxn != null)
      throw new OtmException("A transaction is already active on this session");

    ensureTxActive(true);
    locTxn = new TransactionImpl(this, jtaTxn);

    txnIsRO = readOnly;

    return locTxn;
  }

  /**
   * Clears the transaction flags when committed or rolled back.
   */
  private void endTransaction() {
    locTxn = null;
    jtaTxn = null;

    if (tsCon != null)
      tsCon.close();
    tsCon  = null;

    if (bsCon != null)
      bsCon.close();
    bsCon  = null;
  }

  /*
   * inherited javadoc
   */
  public Transaction getTransaction() {
    if (jtaTxn != null && locTxn == null)
      locTxn = new TransactionImpl(this, jtaTxn);

    return locTxn;
  }

  /*
   * inherited javadoc
   */
  public void close() throws OtmException {
    clear();

    if (jtaTxn != null) {
      try {
        jtaTxn.rollback();
      } catch (Exception e) {
        throw new OtmException("Error setting rollback-only", e);
      }
    }
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

    return new Criteria(this, criteria, m, checkClass(m.getAssociatedEntity()),
                        new ArrayList<Filter>(filters.values()));
  }

  /*
   * inherited javadoc
   */
  public List list(Criteria criteria) throws OtmException {
    if (flushMode.implies(FlushMode.always))
      flush(); // so that mods are visible to queries

    TripleStore store = sessionFactory.getTripleStore();

    return store.list(criteria, getTripleStoreCon());
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
    if (flushMode.implies(FlushMode.always))
      flush(); // so that mods are visible to queries

    TripleStore store = sessionFactory.getTripleStore();

    return store.doNativeQuery(query, getTripleStoreCon());
  }

  /*
   * inherited javadoc
   */
  public void doNativeUpdate(String command) throws OtmException {
    if (flushMode.implies(FlushMode.always))
      flush(); // so that ordering is preserved

    TripleStore store = sessionFactory.getTripleStore();
    store.doNativeUpdate(command, getTripleStoreCon());
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

    Binder  b = idField.getBinder(getEntityMode());
    List          ids     = b.get(o);
    if (ids.size() == 0)
      throw new OtmException("No id set for " + clazz + " instance " + o);

    return (String) ids.get(0);
  }

  /** 
   * Get the connection to the triple-store. If the connection does not exist yet it is created;
   * otherwise the existing one is returned. 
   * 
   * @return the triple-store connection
   */
  protected Connection getTripleStoreCon() throws OtmException {
    if (tsCon == null) {
      ensureTxActive(false);
      tsCon = sessionFactory.getTripleStore().openConnection(this, txnIsRO);
    }

    return tsCon;
  }

  /** 
   * Get the connection to the blob-store. If the connection does not exist yet it is created;
   * otherwise the existing one is returned. 
   * 
   * @return the blob-store connection
   */
  protected Connection getBlobStoreCon() throws OtmException {
    if (bsCon == null) {
      ensureTxActive(false);
      bsCon = sessionFactory.getBlobStore().openConnection(this, txnIsRO);
    }

    return bsCon;
  }

  private void ensureTxActive(boolean start) throws OtmException {
    if (jtaTxn != null)
      return;

    try {
      TransactionManager tm = getSessionFactory().getTransactionManager();

      jtaTxn = tm.getTransaction();
      if (jtaTxn == null) {
        if (!start)
          throw new OtmException("No active transaction");

        tm.begin();
        jtaTxn = tm.getTransaction();
      }

      jtaTxn.registerSynchronization(new Synchronization() {
        public void beforeCompletion() {
          if (getFlushMode().implies(FlushMode.commit))
            flush();
        }

        public void afterCompletion(int status) {
          endTransaction();
        }
      });
    } catch (OtmException oe) {
      throw oe;
    } catch (Exception e) {
      throw new OtmException("Error setting up transaction", e);
    }
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

    if ((cm.getModel() == null) && !cm.isView() && (cm.getBlobField() == null))
      throw new OtmException("No graph/model found for " + clazz);

    return cm;
  }

  protected ClassMetadata checkClass(String entityName) throws OtmException {
    ClassMetadata cm = sessionFactory.getClassMetadata(entityName);

    if (cm == null)
      throw new OtmException("No class metadata found for " + entityName);

    if ((cm.getModel() == null) && !cm.isView() && (cm.getBlobField() == null))
      throw new OtmException("No graph/model found for " + entityName);

    return cm;
  }

}
