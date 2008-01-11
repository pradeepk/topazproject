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

import java.util.List;

/**
 * A connection handle to a triple store or blob store.
 *
 * @author Pradeep Krishnan
  */
public interface Connection {
  /**
   * Prepare to commit the writes.
   */
  public void prepare() throws OtmException;

  /**
   * Commit the writes made via this connection.
   */
  public void commit() throws OtmException;

  /**
   * Rollback/undo writes made via this connection.
   */
  public void rollback() throws OtmException;

  /**
   * Get the list of child connections participating in a Tree 2-Phase commit.
   *
   * @return a modifiable list of connections that a transaction cordinator
   *         can manipulate to set up the list of Connections this Connection 
   *         must coordinate.
   */
  public List<Connection> getChildConnections();
}
