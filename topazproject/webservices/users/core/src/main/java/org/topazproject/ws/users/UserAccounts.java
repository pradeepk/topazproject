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
 * This defines the user-account service. It allows for the management of the core user accounts.
 * The interal topaz user-id is created and assigned here; it is this id that must be passed to
 * various other services whenever a userId is required. The id should be treated as an opaque
 * string.
 * 
 * <P>Each account may have any number of authentication id's associated with it. These are used to
 * find the account associated with a given authentication id (token). The ids are treated as opaque
 * strings by this service.
 *
 * @author Ronald Tschalär
 */
public interface UserAccounts {
  /** 
   * Create a new user account and associate a single authentication id with it.
   * 
   * @param authId  the user's authentication id
   * @return the user's internal id
   * @throws RemoteException if some other error occured
   */
  public String createUser(String authId) throws RemoteException;

  /** 
   * Delete a user's account. Note that the application should ensure that all other information
   * related to this account (e.g. profile and preferences) have been deleted first - this method
   * will not do that.
   * 
   * @param userId  the user's internal id
   * @throws NoSuchIdException if the user account does not exist
   * @throws RemoteException if some other error occured
   */
  public void deleteUser(String userId) throws NoSuchIdException, RemoteException;

  /** 
   * Get the list of currently known authentication id's for the specified user account.
   * 
   * @param userId  the user account's id
   * @return the list of authentication id's; this may be empty. Note that the order of the entries
   *         will be arbitrary.
   * @throws NoSuchIdException if the user account does not exist
   * @throws RemoteException if some other error occured
   */
  public String[] getAuthenticationIds(String userId) throws NoSuchIdException, RemoteException;

  /** 
   * Set the list of currently known authentication id's for the specified user account. The list
   * may be empty, which will probably have the effect of disabling logins to the account.
   * 
   * @param userId  the user account's id
   * @param authId  the list of authentication id's; this may be empty. Note that the order will not
   *                be preserved.
   * @throws NoSuchIdException if the user account does not exist
   * @throws RemoteException if some other error occured
   */
  public void setAuthenticationIds(String userId, String[] authIds)
      throws NoSuchIdException, RemoteException;

  /** 
   * Look up a user-id given an authentication id.
   * 
   * @param authId the user's authentication id
   * @return the user's internal id, or null if not found
   * @throws RemoteException if some lookup error occured
   */
  public String lookUpUserByAuthId(String authId) throws RemoteException;
}
