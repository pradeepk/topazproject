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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.otm.Transaction;
import org.topazproject.otm.Session;
import org.topazproject.otm.Connection;
import org.topazproject.otm.OtmException;

/**
 * Transaction object.
 *
 * @author Pradeep Krishnan
 */
class TransactionImpl implements Transaction {
  private static final Log log     = LogFactory.getLog(TransactionImpl.class);
  private AbstractSession          session;
  private Connection       conn    = null;

  /**
   * Creates a new Transaction object.
   *
   * @param session the owning session
   */
  public TransactionImpl(AbstractSession session) {
    this.session = session;
  }

  /**
   * Gets the session to which this transaction belongs.
   *
   * @return the session
   */
  public Session getSession() {
    return session;
  }

  /**
   * Gets a connection to the triplestore.
   *
   * @return the connection
   *
   * @throws OtmException on an error in opening a connection
   */
  public Connection getConnection() throws OtmException {
    if (conn != null)
      return conn;

    conn = session.getSessionFactory().getTripleStore().openConnection();
    conn.beginTransaction();

    return conn;
  }

  /**
   * Flush the session, commit and close the connection.
   *
   * @throws OtmException on an error in commit
   */
  public void commit() throws OtmException {
    if (session == null)
      throw new OtmException("Attempt to use a closed transaction");

    Session.FlushMode fm = session.getFlushMode();
    if ((fm == Session.FlushMode.commit) || (fm == Session.FlushMode.always))
      session.flush();

    if (conn != null)
      conn.commit();

    close();
  }

  /**
   * Rollback the transaction and close the connection. Session data is left alone.
   *
   * @throws OtmException on an error in roll-back
   */
  public void rollback() throws OtmException {
    try {
      if (conn != null)
        conn.rollback();
    } finally {
      close();
    }
  }

  private void close() throws OtmException {
    if (conn != null) {
      session.getSessionFactory().getTripleStore().closeConnection(conn);
      conn = null;
    }

    if (session != null) {
      session.endTransaction();
      session = null;
    }
  }
}
