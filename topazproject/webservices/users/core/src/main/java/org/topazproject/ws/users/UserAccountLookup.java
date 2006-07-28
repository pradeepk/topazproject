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
 * This defines the interface by which a user account can be looked up, without any access-controls
 * on the operation. It is meant for use by authentication services which need to translate an
 * external authentication token to the internal user id. This class' raison-d'être for is to
 * prevent poultry problems in such services.
 * 
 * @author Ronald Tschalär
 */
public interface UserAccountLookup {
  /** 
   * Look up a user-id given an authentication id. This is identical to {@link
   * UserAccounts#lookUpUserByAuthId UserAccounts.lookUpUserByAuthId} except that it bypasses all
   * access-controls.
   * 
   * @param authId the user's authentication id
   * @return the user's internal id, or null if not found
   * @throws RemoteException if some lookup error occured
   */
  public String lookUpUserByAuthIdNoAC(String authId) throws RemoteException;
}
