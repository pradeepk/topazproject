/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.plos.user.service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.rmi.RemoteException;

import javax.xml.rpc.ServiceException;

import org.plos.service.BaseConfigurableService;
import org.topazproject.authentication.ProtectedService;
import org.topazproject.common.NoSuchIdException;
import org.topazproject.ws.users.UserAccounts;
import org.topazproject.ws.users.UserAccountsClientFactory;

/**
 * Wrapper around Topaz User Accounts web service
 * 
 * @author Stephen Cheng
 * 
 */
public class UserWebService extends BaseConfigurableService {

  private UserAccounts userService;

  private String applicationId;

  /**
   * Creates web service
   * 
   * @throws IOException
   * @throws URISyntaxException
   * @throws ServiceException
   */
  public void init() throws IOException, URISyntaxException, ServiceException {
    final ProtectedService protectedService = createProtectedService(getConfiguration());
    userService = UserAccountsClientFactory.create(protectedService);
  }

  /**
   * Create a new user account and associate a single authentication id with it.
   * 
   * @param authId
   *          the user's authentication id
   * @return the user's internal id
   * @throws RemoteException
   *           if some other error occured
   */
  public String createUser(final String authId) throws RemoteException {
    return userService.createUser(authId);
  }

  /**
   * Deletes given user ID from system. Does not delete any resources with the user, only the user
   * itself.
   * 
   * @param userId
   *          Topaz User ID to delete
   * @throws NoSuchIdException
   * @throws RemoteException
   */
  public void deleteUser(final String userId) throws NoSuchIdException, RemoteException {
    userService.deleteUser(userId);
  }

  /**
   * Sets the state of a given user account
   * 
   * @param userId
   *          Topaz User ID
   * @param state
   *          new state of user
   * @throws NoSuchIdException
   * @throws RemoteException
   */
  public void setState(final String userId, int state) throws NoSuchIdException, RemoteException {
    userService.setState(userId, state);
  }

  /**
   * Gets the current state of the user
   * 
   * @param userId
   *          Topaz User ID to set
   * @return current state of user
   * @throws NoSuchIdException
   * @throws RemoteException
   */
  public int getState(final String userId) throws NoSuchIdException, RemoteException {
    return userService.getState(userId);
  }

  /**
   * Gets the authentication IDs for a given Topaz user ID
   * 
   * @param userId
   *          Topaz user ID
   * @return array of authentication ids
   * @throws NoSuchIdException
   * @throws RemoteException
   */
  public String[] getAuthenticationIds(final String userId) throws NoSuchIdException,
      RemoteException {
    return userService.getAuthenticationIds(userId);
  }

  /**
   * Returns the Topaz User ID the authId is associated with
   * 
   * @param authId
   *          authentication ID to lookup
   * @return Topaz User ID
   * @throws RemoteException
   */
  public String lookUpUserByAuthId(final String authId) throws RemoteException {
    return userService.lookUpUserByAuthId(authId);
  }

  /**
   * @return Returns the applicationId.
   */
  public String getApplicationId() {
    return applicationId;
  }

  /**
   * @param applicationId
   *          The applicationId to set.
   */
  public void setApplicationId(String applicationId) {
    this.applicationId = applicationId;
  }
}
