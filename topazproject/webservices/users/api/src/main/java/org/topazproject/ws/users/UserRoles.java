/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.topazproject.ws.users;

import java.rmi.RemoteException;

/** 
 * This defines the user-roles service. It allows for the management of the security roles assigned
 * to users.
 * 
 * @author Ronald Tschalär
 */
public interface UserRoles {
  /** 
   * Set the list of currently assigned security roles for the specified user.
   * 
   * @param userId  the user's id
   * @param roles   the list of roles to assign this user; may be null. Note that the order will not
   *                be preserved.
   * @throws RemoteException if some other error occured
   */
  public void setRoles(String userId, String[] roles) throws NoSuchIdException, RemoteException;

  /** 
   * Get the list of currently assigned security roles for the specified user.
   * 
   * @param userId  the user's id
   * @return the list of roles; this may be null. Note that the order of the entries will be
   *         arbitrary.
   * @throws NoSuchIdException if the user does not exist
   * @throws RemoteException if some other error occured
   */
  public String[] getRoles(String userId) throws NoSuchIdException, RemoteException;
}
