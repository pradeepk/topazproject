/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007-2008 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.topazproject.otm;

import java.util.List;
import java.util.Set;

import org.topazproject.otm.filter.FilterDefinition;
import org.topazproject.otm.mapping.Mapper;
import org.topazproject.otm.query.Results;

/**
 * The main runtime interface between a Java application and OTM. This is the central API
 * class abstracting the notion of a persistence service.<br>
 * <br><i>
 * An OTM session is similar to a Hibernate session and most of the Hibernate documentation
 * is applicable to OTM also. The following description is mostly copied from Hibernate.</i><br>
 *<br>
 * The lifecycle of a <tt>Session</tt> is bounded by the beginning and end of a logical
 * transaction. (Long transactions might span several database transactions.)<br>
 * <br>
 * The main function of the <tt>Session</tt> is to offer create, read and delete operations
 * for instances of mapped entity classes. Instances may exist in one of three states:<br>
 * <br>
 * <i>transient:</i> never persistent, not associated with any <tt>Session</tt><br>
 * <i>persistent:</i> associated with a unique <tt>Session</tt><br>
 * <i>detached:</i> previously persistent, not associated with any <tt>Session</tt><br>
 * <br>
 * Transient instances may be made persistent by calling <tt>saveOrUpdate()</tt>.
 * Persistent instances may be made transient by calling<tt> delete()</tt>. Any instance returned
 * by a <tt>get()</tt> or <tt>load()</tt> method is persistent. Detached instances may be made
 * persistent by calling <tt>saveOrUpdate()</tt>. The state of a transient or detached instance
 * may also be made persistent as a new persistent instance by calling <tt>merge()</tt>.<br>
 * <br>
 * Changes to <i>persistent</i> instances are detected at flush time and also result in a database
 * update. There is no need to call a <tt>saveOrUpdate()</tt> again to trigger the update.<br>
 * <br>
 * It is not intended that implementors be threadsafe. Instead each thread/transaction
 * should obtain its own instance from a <tt>SessionFactory</tt>.<br>
 * <br>
 * A typical transaction should use the following idiom:
 * <pre>
 * Session sess = factory.openSession();
 * Transaction tx;
 * try {
 *     tx = sess.beginTransaction();
 *     //do some work
 *     ...
 *     tx.commit();
 * }
 * catch (Exception e) {
 *     if (tx!=null) tx.rollback();
 *     throw e;
 * }
 * finally {
 *     sess.close();
 * }
 * </pre>
 * <br>
 * If the <tt>Session</tt> throws an exception, the transaction must be rolled back
 * and the session discarded. The internal state of the <tt>Session</tt> might not
 * be consistent with the database after the exception occurs.
 *
 * @see SessionFactory
 * @author Pradeep Krishnan
 */
public interface Session {
  /**
   * Represents a flushing strategy. 'always' will flush before queries and transaction commit.
   * 'commit' will flush on a transaction commit only. 'manual' will require the user to call
   * flush.
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
   * Gets the interceptor that is listening to changes in object state.
   *
   * @return the interceptor
   */
  public Interceptor getInterceptor();

  /**
   * Begins a new read-write transaction with a default timeout. All session usage is within
   * transaction scope.
   *
   * @return the transaction
   *
   * @throws OtmException on an error
   */
  public Transaction beginTransaction() throws OtmException;

  /**
   * Begins a new transaction. All session usage is within transaction scope.
   *
   * @param readOnly  true if this should be a read-only transaction, false if a read-write
   *                  transaction
   * @param txTimeout the transaction timeout, in seconds; if &lt;= 0 then use the default timeout
   * @return the transaction
   *
   * @throws OtmException if a transaction is already active or if an error occurred starting
   *                      a new transaction
   */
  public Transaction beginTransaction(boolean readOnly, int txTimeout) throws OtmException;

  /**
   * Gets the current transaction.
   *
   * @return the transaction, or null if there is none
   * @throws OtmException on an error
   */
  public Transaction getTransaction() throws OtmException;

  /**
   * Close and release all resources.
   *
   * @throws OtmException on an error
   */
  public void close() throws OtmException;

  /**
   * Sets the FlushMode for this session. If not set, the default is 'always'.
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
   * @see #setFlushMode
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
   * Loads an object from the session or a newly created dynamic proxy for it. Does not hit
   * the triplestore.
   *
   * @param entity the entity name of the object
   * @param oid the id of the object
   *
   * @return the object or null if deleted from session
   *
   * @throws OtmException on an error
   */
  public Object load(String entity, String oid) throws OtmException;

  /**
   * Loads an object from the session or a newly created dynamic proxy for it. Does not hit
   * the triplestore.
   *
   * @param cm the entity metdata for the object
   * @param oid the id of the object
   *
   * @return the object or null if deleted from session
   *
   * @throws OtmException on an error
   */
  public Object load(ClassMetadata cm, String oid) throws OtmException;

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
   * Gets an object from the session or from the triple store.
   *
   * @param entity the entity type of the object
   * @param oid the id of the object
   *
   * @return the object or null if deleted or does not exist in store
   *
   * @throws OtmException on an error
   */
  public Object get(String entity, String oid) throws OtmException;

  /**
   * Internal method. DO NOT USE.
   *
   * @see #get(java.lang.Class, java.lang.String)
   */
  public Object get(ClassMetadata cm, String oid, boolean filterObj) throws OtmException;

  /**
   * Replaces the contents any attached object (of the same id) with the given object's contents;
   * if there is no object with the same id attached to the session then one is created with the
   * given object's contents and attached to the session. The returned object in all cases is an
   * attached object with the state info the same as the given object <var>o</var>. If the supplied
   * object is a detached object, it will remain detached even after the call.
   *
   * @param o the detached object
   *
   * @return an attached object with merged values
   *
   * @throws OtmException on an error
   */
  public <T> T merge(T o) throws OtmException;

  /**
   * Refreshes an attached object with values from the database. Note that the results are
   * dependent on the {@link setFlushMode} in effect, as this will load the last-flushed data
   * (or the original data if no flush has occurred so far in the current transaction).
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
   * Creates the 'criteria' for retrieving a set of objects for an entity.
   *
   * @param entity the entity
   *
   * @return a newly created Criteria object
   *
   * @throws OtmException on an error
   */
  public Criteria createCriteria(String entity) throws OtmException;

  /**
   * Creates a 'sub-criteria' for retrieving a set of objects for an association in a parent
   * class. Usually called by {@link
   * org.topazproject.otm.Criteria#createCriteria(java.lang.String) Criteria#createCriteria}
   *
   * @param parent    the parent criteria of the sub-criteria to create
   * @param referrer  if null, then <var>path</var> is a field in the parent's entity; if not null,
   *                  then <var>path</var> is a field in this entity and this is the newly created
   *                  criteria's entity.
   * @param path      path to the association field
   * @param assocType if not null then it overrides the type of other end of the association; i.e.
   *                  this can be viewed as a cast. If <var>referrer</var> is not null then this
   *                  type must be assignment compatible with <var>parent</var>'s type (i.e. the
   *                  <var>parent</var>'s type is a subtype of <var>assocType</var>), but it is
   *                  otherwise ignored; if <var>referrer</var> is null then this is the created
   *                  child criteria's type.
   *
   * @return a newly created Criteria.
   *
   * @throws OtmException on an error
   */
  public Criteria createCriteria(Criteria parent, String referrer, String path, String assocType)
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
   * Creates a graph in the underlying TripleStore. This is an idempotent operation.
   *
   * @param graph The name of the graph to be created.
   * @throws OtmException on an error.
   */
  public void createGraph(String graph) throws OtmException;

  /**
   * Removes a graph from the underlying TripleStore. This is an idempotent operation.
   *
   * @param graph The name of the graph to be removed.
   * @throws OtmException if the graph does not exist or some other error occurred.
   */
  public void dropGraph(String graph) throws OtmException;

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

  /**
   * Get the entity mode of this Session.
   *
   * @return the entity mode
   */
  public EntityMode getEntityMode();

  /**
   * Internal method. DO NOT USE. Called by field loaders to notify when a
   * lazy loaded field is loaded completely. The session can now start
   * doing change-track monitoring and orphan-delete tracking on this field
   * too.
   *
   * @param o the object whose field was lazy loaded
   * @param field the field that is lazy loaded
   */
  public void delayedLoadComplete(Object o, Mapper field) throws OtmException;

  /**
   * Internal method. DO NOT USE. Get the connection to the triple-store.
   * If the connection does not exist yet it is created; otherwise the existing one is returned.
   *
   * @return the triple-store connection
   */
  public Connection getTripleStoreCon() throws OtmException;

  /**
   * Internal method. DO NOT USE. Get the connection to the blob-store.
   * If the connection does not exist yet it is created; otherwise the existing one is returned.
   *
   * @return the blob-store connection
   */
  public Connection getBlobStoreCon() throws OtmException;
}
