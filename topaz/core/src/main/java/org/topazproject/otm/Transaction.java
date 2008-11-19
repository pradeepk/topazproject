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

/**
 * Allows the application to define units of work, while
 * maintaining abstraction from the underlying transaction
 * implementations on top of both a <tt>TripleStore</tt> and <tt>BlobStore</tt>.<br>
 * <br>
 * A transaction is associated with a <tt>Session</tt> and is
 * usually instantiated by a call to <tt>Session.beginTransaction()</tt>.
 * A single session might span multiple transactions since
 * the notion of a session (a conversation between the application
 * and the triplestore and blobstore) is of coarser granularity than the notion of
 * a transaction. However, it is intended that there be at most one
 * uncommitted <tt>Transaction</tt> associated with a particular
 * <tt>Session</tt> at any time.<br>
 * <br>
 * Implementors are not intended to be threadsafe.
 *
 * @see Session#beginTransaction()
 * @see org.hibernate.transaction.TransactionFactory
 *
 * @author Pradeep Krishnan
 */
public interface Transaction {
  /**
   * Gets the session to which this transaction belongs.
   *
   * @return the session
   */
  public Session getSession();

  /**
   * Gets the underling JTA transaction.
   *
   * @return the jta transaction
   */
  public javax.transaction.Transaction getJtaTransaction();

  /**
   * Mark the transaction for rollback.
   */
  public void setRollbackOnly() throws OtmException;

  /**
   * Test whether this transaction has been marked for rollback only.
   *
   * @return true if this transaction will be rolled back
   */
  public boolean isRollbackOnly() throws OtmException;

  /**
   * Flush the session, commit and close the connection.
   *
   * @throws OtmException on an error in commit
   */
  public void commit() throws OtmException;

  /**
   * Rollback the transaction and close the connection. Session data is left alone.
   *
   * @throws OtmException on an error in roll-back
   */
  public void rollback() throws OtmException;
}
