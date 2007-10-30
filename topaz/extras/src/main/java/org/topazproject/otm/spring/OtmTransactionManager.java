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

/**
 * A Spring transaction-manager that uses OTM transactions.
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
  private Session session;

  @Override
  public Object doGetTransaction() {
    return session;
  }

  @Override
  public void doBegin(Object transaction, TransactionDefinition definition)
      throws TransactionException {
    try {
      ((Session) transaction).beginTransaction();
    } catch (OtmException oe) {
      throw new TransactionSystemException("error beginning transaction", oe);
    }
  }

  @Override
  public void doCommit(DefaultTransactionStatus status) throws TransactionException {
    Transaction tx = ((Session) status.getTransaction()).getTransaction();
    if (tx == null)
      throw new TransactionUsageException("no transaction active");

    try {
      tx.commit();
    } catch (OtmException oe) {
      throw new TransactionSystemException("error committing transaction", oe);
    }
  }

  @Override
  public void doRollback(DefaultTransactionStatus status) throws TransactionException {
    Transaction tx = ((Session) status.getTransaction()).getTransaction();
    if (tx == null)
      throw new TransactionUsageException("no transaction active");

    try {
      tx.rollback();
    } catch (OtmException oe) {
      throw new TransactionSystemException("error rolling back transaction", oe);
    }
  }

  @Override
  protected boolean isExistingTransaction(Object transaction) {
    return ((Session) transaction).getTransaction() != null;
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
}
