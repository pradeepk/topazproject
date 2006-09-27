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
import org.topazproject.ws.pap.Profiles;
import org.topazproject.ws.pap.ProfilesClientFactory;
import org.topazproject.ws.pap.UserProfile;

/**
 * Wrapper class for Topaz profile web service
 * 
 * @author Stephen Cheng
 * 
 */
public class ProfileWebService extends BaseConfigurableService {

  private Profiles profileService;

  /**
   * Creates the profiles web service
   * 
   * @throws IOException
   * @throws URISyntaxException
   * @throws ServiceException
   */
  public void init() throws IOException, URISyntaxException, ServiceException {
    final ProtectedService protectedService = createProtectedService(getConfiguration());
    profileService = ProfilesClientFactory.create(protectedService);
  }

  /**
   * Retrieves UserProfile for a given Topaz UserId
   * 
   * @param userId Topaz User ID
   * @return profile of given user
   * @throws NoSuchIdException
   * @throws RemoteException
   */
  public UserProfile getProfile(final String userId) throws NoSuchIdException, RemoteException {
    return profileService.getProfile(userId);
  }

  /**
   * Store UserProfile for a given Topaz UserID
   * 
   * @param userId Topaz User ID
   * @param profile Profile to store
   * @throws NoSuchIdException
   * @throws RemoteException
   */
  public void setProfile(final String userId, final UserProfile profile) throws NoSuchIdException,
      RemoteException {
    profileService.setProfile(userId, profile);
  }
}
