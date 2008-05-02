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
package org.topazproject.otm.impl;

import javax.transaction.Status;
import javax.transaction.Transaction;

import org.topazproject.otm.Session;
import org.topazproject.otm.OtmException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Implement local Transaction as a wrapper around a jta transaction.
 *
 * @author Pradeep Krishnan
 */
public class TransactionImpl implements org.topazproject.otm.Transaction {
  private static final Log  log = LogFactory.getLog(TransactionImpl.class);
  private static enum State {ACTIVE, COMMITTED, ROLLEDBACK, ERROR};

  private final Session     session;
  private final Transaction jtaTxn;
  private State state = State.ACTIVE;

  public int ctr = 0;           // temporary hack for problems with mulgara's blank-nodes

  /**
   * Creates a new Transaction object.
   *
   * @param session the owning session
   * @param jtaTxn  the underlying jta transaction
   */
  public TransactionImpl(Session session, Transaction jtaTxn) {
    this.session = session;
    this.jtaTxn  = jtaTxn;

    if (log.isDebugEnabled())
      log.debug("Started local transaction " + this + " for session " + session);
  }

  public Session getSession() {
    return session;
  }

  public void setRollbackOnly() throws OtmException {
    if (log.isDebugEnabled())
      log.debug("Setting rollback-only on transaction " + this);

    try {
      jtaTxn.setRollbackOnly();
    } catch (Exception e) {
      throw new OtmException("Error setting rollback-only", e);
    }
  }

  public boolean isRollbackOnly() throws OtmException {
    try {
      return (jtaTxn.getStatus() == Status.STATUS_MARKED_ROLLBACK);
    } catch (Exception e) {
      throw new OtmException("Error getting rollback-only status", e);
    }
  }

  public void commit() throws OtmException {
    if (state != State.ACTIVE)
      throw new OtmException("Transaction is not active");

    if (log.isDebugEnabled())
      log.debug("Committing transaction " + this);

    if (session.getInterceptor() != null)
      session.getInterceptor().beforeTransactionCompletion(this);

    OtmException error = null;
    try {
      jtaTxn.commit();
      state = State.COMMITTED;
    } catch (Exception e) {
      error = new OtmException("Error committing transaction", e);
      state = State.ERROR;
    }

    try {
      if (session.getInterceptor() != null)
       session.getInterceptor().afterTransactionCompletion(this);
    } catch (Exception e) {
      log.warn("Interceptor threw an exception. ", e);
    }

    if (error != null)
      throw error;
  }

  public void rollback() throws OtmException {
    if (log.isDebugEnabled())
      log.debug("Rolling back transaction " + this);

    OtmException error = null;
    boolean notify = (state == State.ACTIVE);
    try {
      jtaTxn.rollback();
      state = State.ROLLEDBACK;
    } catch (Exception e) {
      error = new OtmException("Error rolling back transaction", e);
      state = State.ERROR;
    }

    try {
      if (notify && (session.getInterceptor() != null))
       session.getInterceptor().afterTransactionCompletion(this);
    } catch (Exception e) {
      log.warn("Interceptor threw an exception. ", e);
    }

    if (error != null)
      throw error;
  }

  public boolean isActive() {
    return state == State.ACTIVE;
  }

  public boolean wasCommitted() {
    return state == State.COMMITTED;
  }

  public boolean wasRolledBack() {
    return state == State.ROLLEDBACK;
  }
}
