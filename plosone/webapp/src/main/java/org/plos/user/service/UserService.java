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

import java.rmi.RemoteException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.plos.ApplicationException;
import org.plos.service.BaseConfigurableService;
import org.plos.user.PlosOneUser;

import org.topazproject.common.NoSuchIdException;
import org.topazproject.ws.pap.UserPreference;
import org.topazproject.ws.pap.UserProfile;

/**
 * Class to roll up web services that a user needs in PLoS ONE. Rest of application should generally
 * use PlosOneUser to
 * 
 * @author Stephen Cheng
 * 
 */
public class UserService extends BaseConfigurableService {

  private UserWebService userWebService;

  private ProfileWebService profileWebService;

  private PreferencesWebService preferencesWebService;

  private String applicationId;

  private static final Log log = LogFactory.getLog(UserService.class);

  /**
   * Create a new user account and associate a single authentication id with it.
   * 
   * @param authId
   *          the user's authentication id from CAS
   * @return the user's internal id
   * @throws ApplicationException
   *           if an error occured
   */
  public String createUser(final String authId) throws ApplicationException {
    try {
      return userWebService.createUser(authId);
    } catch (RemoteException e) {
      throw new ApplicationException(e);
    }
  }

  /**
   * Deletes the given user from Topaz. Currently, does not remove the entries in the system
   * associated with the ID. May not want to make this visible as this can affect other applications
   * 
   * @param userId
   *          the Topaz User ID
   * @throws ApplicationException
   */
  private void deleteUser(final String userId) throws ApplicationException {
    try {
      userWebService.deleteUser(userId);
    } catch (NoSuchIdException ne) {
      throw new ApplicationException(ne);
    } catch (RemoteException re) {
      throw new ApplicationException(re);
    }
  }

  /**
   * Gets the user specified by the Topaz userID passed in
   * 
   * @param userId
   *          Topaz User ID
   * @return user associated with the userID
   * @throws ApplicationException
   */
  public PlosOneUser getUser(final String userId) throws ApplicationException {
    PlosOneUser pou = new PlosOneUser();
    pou.setUserProfile(getProfile(userId));
    UserPreference[] userPrefs = getPreferences(applicationId, userId);
    pou.setUserPrefs(userPrefs);
    return pou;
  }

  /**
   * Gets the user specified by the authentication ID (CAS ID currently)
   * 
   * @param authId
   *          authentication ID
   * @return the user associated with the authID
   * @throws ApplicationException
   */
  public PlosOneUser getUserByAuthId(final String authId) throws ApplicationException {
    return getUser(lookUpUserByAuthId(authId));
  }

  /**
   * Sets the state of the user account
   * 
   * @param userId
   *          Topaz User ID
   * @param state
   *          new state of the user
   * @throws ApplicationException
   */
  public void setState(final String userId, int state) throws ApplicationException {
    try {
      userWebService.setState(userId, state);
    } catch (NoSuchIdException ne) {
      throw new ApplicationException(ne);
    } catch (RemoteException re) {
      throw new ApplicationException(re);
    }
  }

  /**
   * Gets the current state of the user
   * 
   * @param userId
   *          Topaz userID
   * @return current state of the user
   * @throws ApplicationException
   */
  public int getState(final String userId) throws ApplicationException {
    try {
      return userWebService.getState(userId);
    } catch (NoSuchIdException ne) {
      throw new ApplicationException(ne);
    } catch (RemoteException re) {
      throw new ApplicationException(re);
    }
  }

  /**
   * Retrieves all authentication IDs for a given Topaz userID
   * 
   * @param userId
   *          Topaz userID
   * @return array of all authentiation IDs associated with the Topaz userID
   * @throws ApplicationException
   */
  public String[] getAuthenticationIds(final String userId) throws ApplicationException {
    try {
      return userWebService.getAuthenticationIds(userId);
    } catch (NoSuchIdException ne) {
      throw new ApplicationException(ne);
    } catch (RemoteException re) {
      throw new ApplicationException(re);
    }
  }

  /**
   * Returns the Topaz userID the authentiation ID passed in is associated with
   * 
   * @param authId
   *          authentication ID you are looking up
   * @return Topaz userID for a given authentication ID
   * @throws ApplicationException
   */
  public String lookUpUserByAuthId(final String authId) throws ApplicationException {
    try {
      return userWebService.lookUpUserByAuthId(authId);
    } catch (RemoteException re) {
      throw new ApplicationException(re);
    }
  }

  /**
   * Retrieves the profile for the given Topaz User ID
   * 
   * @param userId
   *          Topaz userID
   * @return user profile of Topaz user
   * @throws ApplicationException
   */
  public UserProfile getProfile(final String userId) throws ApplicationException {
    try {
      return profileWebService.getProfile(userId);
    } catch (NoSuchIdException ne) {
      throw new ApplicationException(ne);
    } catch (RemoteException re) {
      throw new ApplicationException(re);
    }

  }

  /**
   * Takes in a PLoS ONE user and write the profile to the store
   * 
   * @param inUser
   *          write profile of this user to the store
   * @throws ApplicationException
   */
  public void setProfile(final PlosOneUser inUser) throws ApplicationException {
    if (inUser != null) {
      setProfile(inUser.getUserId(), inUser.getUserProfile());
    } else {
      throw new ApplicationException("User is null");
    }
  }

  /**
   * Write the specified user profile and associates it with the specified user ID
   * 
   * @param userId
   *          Topaz User ID
   * @param profile
   *          profile to be written
   * @throws ApplicationException
   */
  protected void setProfile(final String userId, final UserProfile profile)
      throws ApplicationException {
    try {
      profileWebService.setProfile(userId, profile);
    } catch (NoSuchIdException ne) {
      throw new ApplicationException(ne);
    } catch (RemoteException re) {
      throw new ApplicationException(re);
    }
  }

  /**
   * Retrieves user preferences for this application and this Topaz user ID
   * 
   * @param appId
   *          application ID
   * @param userId
   *          Topaz User ID
   * @return array of user preferences
   * @throws ApplicationException
   */
  public UserPreference[] getPreferences(final String appId, final String userId)
      throws ApplicationException {
    try {
      return preferencesWebService.getPreferences(appId, userId);
    } catch (NoSuchIdException ne) {
      throw new ApplicationException(ne);
    } catch (RemoteException re) {
      throw new ApplicationException(re);
    }
  }

  /**
   * Writes the preferences for the given user to the store
   * 
   * @param inUser
   *          User whose preferences should be written
   * @throws ApplicationException
   */
  public void setPreferences(final PlosOneUser inUser) throws ApplicationException {
    if (inUser != null) {
      setPreferences(applicationId, inUser.getUserId(), inUser.getUserPrefs());
    } else {
      throw new ApplicationException("User is null");
    }
  }

  /**
   * Writes the preferences for the user ID and application ID to the store.
   * 
   * @param appId
   *          application ID
   * @param userId
   *          Topaz User ID
   * @param prefs
   *          User preferences to write
   * @throws ApplicationException
   */
  protected void setPreferences(final String appId, final String userId, UserPreference[] prefs)
      throws ApplicationException {
    try {
      preferencesWebService.setPreferences(appId, userId, prefs);
    } catch (NoSuchIdException ne) {
      throw new ApplicationException(ne);
    } catch (RemoteException re) {
      throw new ApplicationException(re);
    }
  }

  /**
   * @return Returns the preferencesWebService.
   */
  public PreferencesWebService getPreferencesWebService() {
    return preferencesWebService;
  }

  /**
   * @param preferencesWebService
   *          The preferencesWebService to set.
   */
  public void setPreferencesWebService(PreferencesWebService preferencesWebService) {
    this.preferencesWebService = preferencesWebService;
  }

  /**
   * @return Returns the profileWebService.
   */
  public ProfileWebService getProfileWebService() {
    return profileWebService;
  }

  /**
   * @param profileWebService
   *          The profileWebService to set.
   */
  public void setProfileWebService(ProfileWebService profileWebService) {
    this.profileWebService = profileWebService;
  }

  /**
   * @return Returns the userWebService.
   */
  public UserWebService getUserWebService() {
    return userWebService;
  }

  /**
   * @param userWebService
   *          The userWebService to set.
   */
  public void setUserWebService(UserWebService userWebService) {
    this.userWebService = userWebService;
  }

  /**
   * @return Returns the appId.
   */
  public String getApplicationId() {
    return applicationId;
  }

  /**
   * @param appId
   *          The appId to set.
   */
  public void setApplicationId(String appId) {
    this.applicationId = appId;
  }
}
