/*
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.topazproject.ws.users;

import java.rmi.RemoteException;

/** 
 * This defines the interface by which a user account can be looked up. It is meant for use by
 * authentication services which need to translate an external authentication token to the internal
 * user id.
 * 
 * @see UserAccount
 * @author Ronald Tschalär
 */
public interface UserAccountLookup {
  /** 
   * Look up a user-id given an authentication id.
   * 
   * @param authId the user's authentication id
   * @return the user's internal id, or null if not found
   * @throws RemoteException if some lookup error occured
   */
  public String lookUpUserByAuthId(String authId) throws RemoteException;
}
