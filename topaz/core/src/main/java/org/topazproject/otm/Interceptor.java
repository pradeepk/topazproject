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

import java.util.Collection;

import org.topazproject.otm.mapping.RdfMapper;
import org.topazproject.otm.mapping.BlobMapper;

/**
 * Allows user code to inspect and/or change property values. 
 * <p>
 * <i>This is similar to a Hibernate Interceptor and most of the Hibernate documentation
 * is applicable here too. The following description is mostly copied from Hibernate.</i>
 * <p>
 * Inspection occurs before property values are written and after they are read from the database.
 * <p>
 * There might be a single instance of Interceptor for a SessionFactory, or a new instance might 
 * be specified for each Session. 
 * <p>
 * The Session may not be invoked from a callback (nor may a callback cause a collection or proxy 
 * to be lazily initialized).
 *
 * @author Pradeep Krishnan
 */
public interface Interceptor {
   public static String NULL = "NULL";
  /**
   * Called after a transaction is begun via the Session#beginTransaction.
   *
   * @param txn the transaction
   */
  public void afterTransactionBegin(Transaction txn);

  /**
   * Called after a transaction is complete.
   *
   * @param txn the transaction
   */
  public void afterTransactionCompletion(Transaction txn);

  /**
   * Called before a transaction is committed. It is not called before a rollback.
   *
   * @param txn the transaction
   */
  public void beforeTransactionCompletion(Transaction txn);

  /**
   * Gets a fully loaded entity instance that is cached externally,
   *
   * @param session the session that is doing the lookup
   * @param cm class metadata for the entity
   * @param id the id of the instance
   * @param instance an instance object to refresh or null
   *
   * @return a cached instance or null if not in cache or @link{#NULL} 
   *         if an instance does not exist.
   */
  public Object getEntity(Session session, ClassMetadata cm, String id, Object instance);

  /**
   * Called after an entity instance is loaded from the store. Interceptor may
   * modify the instance value.
   *
   * @param session the session that is reporting this event
   * @param cm class metadata for the entity
   * @param id the id of the instance
   * @param instance the instance that was loaded or nul if the object does not exist
   */
  public void onPostRead(Session session, ClassMetadata cm, String id, Object instance);

  /**
   * Called after an entity instance is written out to the store.
   *
   * @param session the session that is reporting this event
   * @param cm class metadata for the entity
   * @param id the id of the instance
   * @param instance the instance that was written out
   * @param fields the collection of fields that was written out. 
   * @param blob the blob field that was written out or null if no change to the blob. 
   */
  public void onPostWrite(Session session, ClassMetadata cm, String id, Object instance, 
                           Collection<RdfMapper> fields, BlobMapper blob);

  /**
   * Called after an entity instance is deleted from the store.
   *
   * @param session the session that is reporting this event
   * @param cm class metadata for the entity
   * @param id the id of the instance
   * @param instance the instance that was inserted
   */
  public void onPostDelete(Session session, ClassMetadata cm, String id, Object instance);
}
