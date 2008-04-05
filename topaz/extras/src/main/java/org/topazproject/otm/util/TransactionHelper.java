/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2008 by Topaz, Inc.
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
package org.topazproject.otm.util;

import org.topazproject.otm.OtmException;
import org.topazproject.otm.Session;
import org.topazproject.otm.Transaction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Helper to execute code inside of an OTM transaction. If a transaction is already active, it is
 * reused; otherwise a new transaction is started (and stopped).
 *
 * <p>This should probably get replaced by a spring TransactionManager.
 *
 * <p>Example usage:
 * <pre>
 *    Foo f =
 *      TransactionHelper.doInTx(session, new TransactionHelper.Action<Foo>() {
 *        public Foo run(Transaction tx) {
 *          List l = tx.getSession().createCriteria(Foo.class)...list();
 *          return (Foo) l(0);
 *        }
 *      });
 * </pre>
 *
 * @author Ronald Tschalär
 */
public class TransactionHelper {
  private static Log log = LogFactory.getLog(TransactionHelper.class);

  /** 
   * Not meant to be instantiated. 
   */
  private TransactionHelper() {
  }

  /** 
   * Run the given action within a transaction.
   * 
   * @param s      the otm session to use    
   * @param action the action to run
   * @return the value returned by the action
   */
  public static <T> T doInTx(Session s, Action<T> action) {
    Transaction tx    = null;
    boolean     isNew = true;

    try {
      tx = s.getTransaction();

      if (tx == null)
        tx = s.beginTransaction();
      else
        isNew = false;

      T res = action.run(tx);

      if (isNew)
        tx.commit();
      tx = null;

      return res;
    } finally {
      try {
        if (isNew && tx != null)
          tx.rollback();
      } catch (OtmException oe) {
        log.warn("rollback failed", oe);
      }
    }
  }

  /** 
   * Run the given action within a transaction.
   * 
   * @param s      the otm session to use    
   * @param action the action to run
   * @return the value returned by the action
   */
  public static <T, E extends Throwable> T doInTxE(Session s, ActionE<T, E> action) throws E {
    Transaction tx    = null;
    boolean     isNew = true;

    try {
      tx = s.getTransaction();

      if (tx == null)
        tx = s.beginTransaction();
      else
        isNew = false;

      T res = action.run(tx);

      if (isNew)
        tx.commit();
      tx = null;

      return res;
    } finally {
      try {
        if (isNew && tx != null)
          tx.rollback();
      } catch (OtmException oe) {
        log.warn("rollback failed", oe);
      }
    }
  }

  /**
   * The interface actions must implement.
   */
  public static interface Action<T> {
    /** 
     * This is run within the context of a transaction.
     * 
     * @param tx the current transaction
     * @return anything you want
     */
    T run(Transaction tx);
  }

  /**
   * The interface actions which throw an exception must implement.
   */
  public static interface ActionE<T, E extends Throwable> {
    /** 
     * This is run within the context of a transaction.
     * 
     * @param tx the current transaction
     * @return anything you want
     */
    T run(Transaction tx) throws E;
  }
}
