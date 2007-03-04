package org.topazproject.otm;

/**
 * A triple store connection handle.
 *
 * @author Pradeep Krishnan
  */
public interface Connection {
  /**
   * Begin a transaction on this connection.
   */
  public void beginTransaction();

  /**
   * End a transaction on this connection.
   */
  public void endTransaction();

  /**
   * Commit the current transaction and end it.
   */
  public void commit();

  /**
   * Rollback the current transaction and end it.
   */
  public void rollback();
}
