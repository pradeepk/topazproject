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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.otm.Transaction;
import org.topazproject.otm.Store;
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
  private Map<Store, Connection>   connections = new LinkedHashMap<Store, Connection>();
  private List<Connection>        managedConnections = null;

  /**
   * Creates a new Transaction object.
   *
   * @param session the owning session
   */
  public TransactionImpl(AbstractSession session) {
    this.session = session;
  }

  /*
   * inherited javadoc
   */
  public Session getSession() {
    return session;
  }

  /*
   * inherited javadoc
   */
  public Connection getConnection(Store store) throws OtmException {
    if (session == null)
      throw new OtmException("Attempt to use a closed transaction");

    Connection conn = connections.get(store);
    if (conn != null)
      return conn;

    if (managedConnections != null)
      throw new OtmException("commit/rollback in progress. Too late to participate.");

    conn = store.openConnection(session.getSessionFactory());
    connections.put(store, conn);

    return conn;
  }

  /*
   * inherited javadoc
   */
  public void commit() throws OtmException {
    if (session == null)
      throw new OtmException("Attempt to use a closed transaction");

    Session.FlushMode fm = session.getFlushMode();
    if (fm.implies(Session.FlushMode.commit))
      session.flush();

    createManagedList();

    boolean ok = false;
    try {
      for (Connection conn : managedConnections)
        conn.prepare();
      for (Connection conn : managedConnections)
        conn.commit();

      ok = true;
    } finally {
      try { if (!ok) rollback(); } catch (Throwable e) {}
      close();
    }

  }

  /*
   * inherited javadoc
   */
  public void rollback() throws OtmException {
    int idx = 0;
    try {
      if (managedConnections == null)
       createManagedList();

      while (idx < managedConnections.size())
        managedConnections.get(idx++).rollback();

    } finally {
      while (idx < managedConnections.size()) {
        try {
          managedConnections.get(idx++).rollback();
        } catch (Throwable t) {
        }
      }
      close();
    }
  }

  private void createManagedList() {
    managedConnections = new ArrayList<Connection>(connections.values());
    for (Store store : connections.keySet()) {
      List<Connection> childList = connections.get(store).getChildConnections();
      for (Store child : store.getChildStores()) {
        Connection conn = connections.get(child);
        if (conn != null) {
          managedConnections.remove(conn);
          childList.add(conn);
        }
      }
    }
  }

  private void close() {
    AbstractSession closed = session;
    session = null;
    if (closed != null)
      closed.endTransaction();
  }
}
