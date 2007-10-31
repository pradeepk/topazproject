/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.topazproject.otm.spring;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;

import org.topazproject.otm.OtmException;
import org.topazproject.otm.Session;
import org.topazproject.otm.Transaction;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.TransactionUsageException;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.SmartTransactionObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A Spring transaction-manager that uses OTM transactions. Due to the current limitations on
 * Mulgara transactions, and hence current limitations on OTM transactions, this transaction
 * manager does not support suspending transactions or nested transactions (i.e. you can't use
 * propagation REQUIRES_NEW, NOT_SUPPORTED, or NESTED); however it does support re-using existing
 * transactions (i.e. you can use propagation REQUIRED, SUPPORTS, MANDATORY, and NEVER).
 *
 * <p>This transaction-manager has limited thread-safety: multiple threads may use the same
 * instance as long as there's only one thread per underlying session. "Underlying" here refers to
 * the fact that spring may inject a proxy for the Session object which actually delegates to one
 * of several real Session objects (this commonly happens when the transaction-manager has a longer
 * scope, such as singleton scope, than the Session objects).
 *
 * <p>Note that {@link #doGetTransaction doGetTransaction}, {@link #doBegin doBegin},
 * {@link #doCommit doCommit}, and {@link #doRollback doRollback} should really all be
 * <var>protected</var>, but are <var>public</var> instead to allow for the necessary tx stop/start
 * hacks in the publishing app. Once Mulgara supports multiple parallel transactions we can/should
 * change this back.
 *
 * @author Ronald Tschalär
 */
public class OtmTransactionManager extends AbstractPlatformTransactionManager {
  private static final Log log = LogFactory.getLog(OtmTransactionManager.class);

  private final Map<Transaction, WeakReference<TransactionObject>> txnList =
                                      new HashMap<Transaction, WeakReference<TransactionObject>>();
  private Session session;

  /** 
   * Create a new otm-transaction-manager instance. 
   */
  public OtmTransactionManager() {
    setRollbackOnCommitFailure(true);
  }

  @Override
  public Object doGetTransaction() {
    Transaction tx = session.getTransaction();
    if (tx == null)
      return new TransactionObject(session);

    /* share the transaction-object so that setRollbackOnly() sets rollback globally */
    synchronized (txnList) {
      WeakReference<TransactionObject> ref = txnList.get(tx);
      if (ref != null) {
        TransactionObject txObj = ref.get();
        if (txObj != null)
          return txObj;

        txnList.remove(tx);
      }
      return new TransactionObject(session);
    }
  }

  private void purgeStaleTx() {
    for (Iterator<Map.Entry<Transaction, WeakReference<TransactionObject>>> iter =
             txnList.entrySet().iterator(); iter.hasNext(); ) {
      Map.Entry<Transaction, WeakReference<TransactionObject>> e = iter.next();
      if (e.getValue().get() == null) {
        log.info("Cleaning up abandonded transaction " + e.getKey());
        iter.remove();

        try {
          e.getKey().rollback();
        } catch (OtmException oe) {
          log.warn("Error rolling back abandonded transaction", oe);
        }
      }
    }
  }

  @Override
  public void doBegin(Object transaction, TransactionDefinition definition)
      throws TransactionException {
    try {
      Transaction tx = ((TransactionObject) transaction).getSession().beginTransaction();

      synchronized (txnList) {
        purgeStaleTx();
        txnList.put(tx, new WeakReference((TransactionObject) transaction));
      }
    } catch (OtmException oe) {
      throw new TransactionSystemException("error beginning transaction", oe);
    }
  }

  @Override
  public void doCommit(DefaultTransactionStatus status) throws TransactionException {
    TransactionObject txObj = (TransactionObject) status.getTransaction();

    Transaction tx = txObj.getSession().getTransaction();
    if (tx == null)
      throw new TransactionUsageException("no transaction active");

    try {
      tx.commit();
    } catch (OtmException oe) {
      throw new TransactionSystemException("error committing transaction", oe);
    } finally {
      synchronized (txnList) {
        txnList.remove(tx);
      }
    }
  }

  @Override
  public void doRollback(DefaultTransactionStatus status) throws TransactionException {
    TransactionObject txObj = (TransactionObject) status.getTransaction();

    Transaction tx = txObj.getSession().getTransaction();
    if (tx == null)
      throw new TransactionUsageException("no transaction active");

    try {
      tx.rollback();
    } catch (OtmException oe) {
      throw new TransactionSystemException("error rolling back transaction", oe);
    } finally {
      synchronized (txnList) {
        txnList.remove(tx);
      }
    }
  }

  @Override
  protected boolean isExistingTransaction(Object transaction) {
    return ((TransactionObject) transaction).getSession().getTransaction() != null;
  }

  @Override
  protected void doSetRollbackOnly(DefaultTransactionStatus status) throws TransactionException {
    TransactionObject txObj = (TransactionObject) status.getTransaction();
    txObj.setRollbackOnly(true);
  }

  /**
   * Set the OTM session. Called by spring's bean wiring.
   *
   * @param session the otm session
   */
  @Required
  public void setOtmSession(Session session) {
    this.session = session;
  }

  private class TransactionObject implements SmartTransactionObject {
    private final Session session;
    private       boolean rollbackOnly = false;

    public TransactionObject(Session session) {
      this.session = session;
    }

    Session getSession() {
      return session;
    }

    void setRollbackOnly(boolean flag) {
      rollbackOnly = flag;
    }

    public boolean isRollbackOnly() {
      return rollbackOnly;
    }
  }
}
