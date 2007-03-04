package org.topazproject.otm;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Transaction object.
 *
 * @author Pradeep Krishnan
 */
public class Transaction {
  private static final Log log     = LogFactory.getLog(Transaction.class);
  private Session          session;
  private Connection       conn    = null;

/**
   * Creates a new Transaction object.
   *
   * @param session the owning session
   */
  public Transaction(Session session) {
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
   */
  public Connection getConnection() {
    if (conn != null)
      return conn;

    conn = session.getSessionFactory().getTripleStore().openConnection();
    conn.beginTransaction();

    return conn;
  }

  /**
   * Flush the session, commit and close the connection.
   */
  public void commit() {
    session.flush();

    if (conn != null) {
      conn.commit();
      close();
    }
  }

  /**
   * Rollback the transaction and close the connection. Session data is left alone.
   */
  public void rollback() {
    if (conn != null) {
      conn.rollback();
      close();
    }
  }

  /**
   * End a transaction and close the connection. Session data is left alone.
   */
  public void endTransaction() {
    if (conn != null) {
      conn.endTransaction();
      close();
    }
  }

  private void close() {
    if (conn != null) {
      session.getSessionFactory().getTripleStore().closeConnection(conn);
      conn = null;
    }
  }
}
