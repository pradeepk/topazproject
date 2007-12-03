/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.otm;

/**
 * Transaction object.
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
   * Gets a connection to the triplestore.
   *
   * @return the connection
   *
   * @throws OtmException on an error in opening a connection
   */
  public Connection getConnection() throws OtmException;

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
