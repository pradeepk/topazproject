/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.otm;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

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
import org.topazproject.otm.filter.FilterDefinition;
import org.topazproject.otm.id.IdentifierGenerator;
import org.topazproject.otm.mapping.Mapper;
import org.topazproject.otm.mapping.Mapper.CascadeType;
import org.topazproject.otm.mapping.Mapper.FetchType;
import org.topazproject.otm.query.Results;

/**
 * An otm session (similar to hibernate session). And similar to hibernate session, not thread
 * safe.
 *
 * @author Pradeep Krishnan
 */
public interface Session {
  /**
   * Represents a flushing strategy. 'always' will flush before queries. 'commit' will flush
   * on a transaction commit. 'manual' will require the user to call flush. Default is 'always'.
   */
  public static enum FlushMode {
    always { 
      public boolean implies(FlushMode fm) {
        return this.equals(fm) || FlushMode.commit.implies(fm);
      }
    }, commit, manual;
    
    public boolean implies(FlushMode fm) {
      return this.equals(fm);
    }
  };

  /**
   * Gets the session factory that created this session.
   *
   * @return the session factory
   */
  public SessionFactory getSessionFactory();

  /**
   * Begins a new transaction. All session usage is within transaction scope.
   *
   * @return the transaction
   *
   * @throws OtmException on an error
   */
  public Transaction beginTransaction() throws OtmException;

  /**
   * Gets the current transaction.
   *
   * @return the transaction
   */
  public Transaction getTransaction();

  /**
   * Close and release all resources.
   *
   * @throws OtmException on an error
   */
  public void close() throws OtmException;

  /**
   * Sets the FlushMode for this session.
   *
   * @param flushMode the FlushMode value to set
   */
  public void setFlushMode(FlushMode flushMode);

  /**
   * Gets the current FlushMode.
   *
   * @return the current FlushMode
   */
  public FlushMode getFlushMode();

  /**
   * Flushes all modified objects to the triple-store. Usually called automatically on a
   * transaction commit.
   *
   * @throws OtmException on an error
   */
  public void flush() throws OtmException;

  /**
   * Clear persistence state of all objects.
   */
  public void clear();

  /**
   * Marks an object for storage. All associated objects are stored too.
   *
   * @param o the object to store
   *
   * @return the object id
   *
   * @throws OtmException on an error
   */
  public String saveOrUpdate(Object o) throws OtmException;

  /**
   * Mark an object for deletion from storage. All associated objects are deleted too.
   *
   * @param o the object to delete
   *
   * @return the object id.
   *
   * @throws OtmException on an error
   */
  public String delete(Object o) throws OtmException;

  /**
   * Evict an object from this Session.
   *
   * @param o the object to evict
   *
   * @return the object id.
   *
   * @throws OtmException on an error
   */
  public String evict(Object o) throws OtmException;

  /**
   * Check if the object is contained in the Session. Only tests for 
   * objects in 'Persistent' state. Does not contain objects in the
   * 'Removed' state.
   *
   * @param o the object to evict
   *
   * @return true if this session contains this object
   *
   * @throws OtmException on an error
   */
  public boolean contains(Object o) throws OtmException;

    /**
   * Loads an object from the session or a newly created dynamic proxy for it. Does not hit
   * the triplestore.
   *
   * @param <T> the type of object
   * @param clazz the class of the object
   * @param oid the id of the object
   *
   * @return the object or null if deleted from session
   *
   * @throws OtmException on an error
   */
  public <T> T load(Class<T> clazz, String oid) throws OtmException;

  /**
   * Gets an object from the session or from the triple store.
   *
   * @param <T> the type of the object
   * @param clazz the class of the object
   * @param oid the id of the object
   *
   * @return the object or null if deleted or does not exist in store
   *
   * @throws OtmException on an error
   */
  public <T> T get(Class<T> clazz, String oid) throws OtmException;

  /**
   * Internal method. DO NOT USE.
   *
   * @see #get(java.lang.Class, java.lang.String)
   */
  public <T> T get(Class<T> clazz, String oid, boolean filterObj) throws OtmException;

  /**
   * Merges the given object. The returned object in all cases is an attached object with the
   * state info merged. If the  supplied object is a detached object, it will remain detached even
   * after the call.
   *
   * @param <T> the type of object
   * @param o the detached object
   *
   * @return an attached object with merged values
   *
   * @throws OtmException on an error
   */
  public <T> T merge(T o) throws OtmException;

  /**
   * Refreshes an attached object with values from the database.
   *
   * @param o the attached object to refresh
   *
   * @throws OtmException on an error
   */
  public void refresh(Object o) throws OtmException;

  /**
   * Creates the 'criteria' for retrieving a set of objects of a class.
   *
   * @param clazz the class
   *
   * @return a newly created Criteria object
   *
   * @throws OtmException on an error
   */
  public Criteria createCriteria(Class clazz) throws OtmException;

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
                          throws OtmException;

  /**
   * Gets a list of objects based on a criteria.
   *
   * @param criteria the criteria
   *
   * @return the results list
   *
   * @throws OtmException on an error
   */
  public List list(Criteria criteria) throws OtmException;

  /**
   * Create an OQL query.
   *
   * @param query the OQL query
   * @return the query object
   */
  public Query createQuery(String query) throws OtmException;

  /**
   * Execute a native(ITQL, SPARQL etc.) query.
   *
   * @param query the native query
   * @return the results object
   * @throws OtmException on an error
   */
  public Results doNativeQuery(String query) throws OtmException;

  /**
   * Execute a native(ITQL, SPARQL etc.) update.
   *
   * @param command the native command(s) to execute
   * @throws OtmException on an error
   */
  public void doNativeUpdate(String command) throws OtmException;

  /**
   * Gets the ids for a list of objects.
   *
   * @param objs list of objects
   *
   * @return the list of ids
   *
   * @throws OtmException on an error
   */
  public List<String> getIds(List objs) throws OtmException;

  /** 
   * Enable the named filter. This does not affect existing queries or criteria.
   * 
   * @param name the name of the filter to enable
   * @return the enabled filter, or null if no filter definition can be found.
   * @throws OtmException on an error
   */
  public Filter enableFilter(String name) throws OtmException;

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
  public Filter enableFilter(FilterDefinition fd) throws OtmException;

  /** 
   * Disable the named filter. This does nothing if no filter by the given name has been enabled.
   * This does not affect existing queries or criteria.
   * 
   * @param name the name of the filter to disable
   */
  public void disableFilter(String name) throws OtmException;

  /** 
   * Get the set of enabled filters' names. 
   * 
   * @return the names of the enabled filters
   */
  public Set<String> listFilters();
}
