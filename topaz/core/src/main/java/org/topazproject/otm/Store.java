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
 * An abstraction to represent triple or blob stores.
 *
 * @author Pradeep Krishnan
 */
public interface Store {
  /**
   * Opens a connection to the store.
   *
   * @param sf  the session factory
   *
   * @return the connection
   *
   * @throws OtmException on an error
   */
  public Connection openConnection(SessionFactory sf) throws OtmException;

  /**
   * Gets the child stores in a hierarchy set up by the Session Factory.
   *
   * @return a modifiable list maintained by Session Factory
   * 
   */
  public List<Store> getChildStores();

}
