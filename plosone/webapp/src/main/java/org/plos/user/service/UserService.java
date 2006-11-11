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

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.ApplicationException;
import org.plos.Constants;
import org.plos.permission.service.PermissionWebService;
import org.plos.service.BaseConfigurableService;
import org.plos.user.PlosOneUser;
import org.plos.user.ProfileGrantEnum;
import org.topazproject.common.DuplicateIdException;
import org.topazproject.common.NoSuchIdException;
import org.topazproject.ws.pap.UserPreference;
import org.topazproject.ws.pap.UserProfile;
import org.topazproject.ws.users.NoSuchUserIdException;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Class to roll up web services that a user needs in PLoS ONE. Rest of application should generally
 * use PlosOneUser to
 * 
 * @author Stephen Cheng
 * 
 */
public class UserService extends BaseConfigurableService {

  private UserWebService userWebService;
  private UserRoleWebService userRoleWebService;
  private ProfileWebService profileWebService;
  private PermissionWebService permissionWebService;
  private PreferencesWebService preferencesWebService;

  private String applicationId;
  private String emailAddressUrl;

  private final static String[] allUserProfileFieldGrants = getAllUserProfileFieldGrants();

  private static final Log log = LogFactory.getLog(UserService.class);
  private final String[] ALL_PRINCIPALS = new String[]{Constants.Permission.ALL_PRINCIPALS};

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
    } catch (DuplicateIdException e) {
      throw new ApplicationException(e);
    } catch (RemoteException e) {
      throw new ApplicationException(e);
    }
  }

  /**
   * Deletes the given user from Topaz. Currently, does not remove the entries in the system
   * associated with the ID. May not want to make this visible as this can affect other applications
   * 
   * @param topazUserId
   *          the Topaz User ID
   * @throws ApplicationException ApplicationException
   */
  private void deleteUser(final String topazUserId) throws ApplicationException {
    try {
      userWebService.deleteUser(topazUserId);
    } catch (NoSuchIdException ne) {
      throw new ApplicationException(ne);
    } catch (RemoteException re) {
      throw new ApplicationException(re);
    }
  }

  /**
   * Gets the user specified by the Topaz userID passed in
   * 
   * @param topazUserId
   *          Topaz User ID
   * @return user associated with the topazUserId
   * @throws ApplicationException ApplicationException
   */
  public PlosOneUser getUserByTopazId(final String topazUserId) throws ApplicationException {
    PlosOneUser pou = new PlosOneUser();
    pou.setUserProfile(getProfile(topazUserId));
    pou.setUserId(topazUserId);
    UserPreference[] userPrefs = getPreferences(applicationId, topazUserId);
    pou.setUserPrefs(userPrefs);
    return pou;
  }

  /**
   * Gets the user specified by the authentication ID (CAS ID currently)
   * 
   * @param authId
   *          authentication ID
   * @return the user associated with the authID
   * @throws ApplicationException ApplicationException
   */
  public PlosOneUser getUserByAuthId(final String authId) throws ApplicationException {
    final String topazUserId = lookUpUserByAuthId(authId);
    return topazUserId == null ? null : getUserByTopazId(topazUserId);
  }

  /**
   * Sets the state of the user account
   * 
   * @param userId
   *          Topaz User ID
   * @param state
   *          new state of the user
   * @throws ApplicationException ApplicationException
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
   * @throws ApplicationException ApplicationException
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
   * @throws ApplicationException ApplicationException
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
   * @throws ApplicationException ApplicationException
   */
  public String lookUpUserByAuthId(final String authId) throws ApplicationException {
    try {
      return userWebService.lookUpUserByAuthId(authId);
    } catch (RemoteException re) {
      throw new ApplicationException("lookUpUserByAuthId failed for authId:" + authId, re);
    }
  }

  /**
   * Retrieves the profile for the given Topaz User ID
   * 
   * @param topazUserId
   *          Topaz userID
   * @return user profile of Topaz user
   * @throws ApplicationException ApplicationException
   */
  public UserProfile getProfile(final String topazUserId) throws ApplicationException {
    try {
      return profileWebService.getProfile(topazUserId);
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
   * @throws ApplicationException ApplicationException
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
   * @param topazUserId
   *          Topaz User ID
   * @param profile
   *          profile to be written
   * @throws ApplicationException ApplicationException
   */
  protected void setProfile(final String topazUserId, final UserProfile profile)
      throws ApplicationException {
    try {
      profileWebService.setProfile(topazUserId, profile);
    } catch (NoSuchIdException ne) {
      throw new ApplicationException(ne);
    } catch (RemoteException re) {
      throw new ApplicationException(re);
    }
  }

  /**
   * Get the ProfileGrantEnum for the profile fields that are public
   * @param topazId topazId
   * @throws ApplicationException ApplicationException
   * @return the collection of ProfileGrantEnum
   */
  public Collection<ProfileGrantEnum> getProfileFieldsThatArePublic(final String topazId) throws ApplicationException {
    try {
      final String[] grants = permissionWebService.listGrants(topazId, Constants.Permission.ALL_PRINCIPALS);

      final Collection<String> result = new ArrayList<String>(grants.length);
      for (final String grant : grants) {
        if (ArrayUtils.contains(allUserProfileFieldGrants, grant)) {
          result.add(grant);
        }
      }

      return ProfileGrantEnum.getUserProfilePermissionsForGrants(result.toArray(new String[result.size()]));

    } catch (RemoteException ex) {
      throw new ApplicationException("Unable to fetch the access rights on the profile fields", ex);
    }
  }

  /**
   * Set the selected profileGrantEnums on the given topaz profile to be private for the user making this call.
   * All the other fields will be set as public.
   *
   * @param topazId topazId
   * @param profileGrantEnums profileGrantEnums
   * @throws ApplicationException ApplicationException
   */
  public void setProfileFieldsPrivate(final String topazId, final ProfileGrantEnum[] profileGrantEnums) throws ApplicationException {
    final String[] grants = getGrants(profileGrantEnums);

    final ArrayList<String> allGrants = getAllUserProfileGrants();
    allGrants.removeAll(Arrays.asList(grants));

    setProfileFieldsPublic(topazId, grants);
  }

  /**
   * Set the selected profileGrantEnums on the given topaz profile for all the users as public.
   * All the other fields will be set as private.
   *
   * @param topazId topazId
   * @param profileGrantEnums profileGrantEnums
   * @throws ApplicationException ApplicationException
   */
  public void setProfileFieldsPublic(final String topazId, final ProfileGrantEnum[] profileGrantEnums) throws ApplicationException {
    final String[] grants = getGrants(profileGrantEnums);
    setProfileFieldsPublic(topazId, grants);
  }

  private void setProfileFieldsPublic(final String topazId, final String[] grants) throws ApplicationException {
    try {
      //Cancel all grants first
      permissionWebService.cancelGrants(topazId, allUserProfileFieldGrants, ALL_PRINCIPALS);
      //Now add the grants as requested
      permissionWebService.grant(topazId, grants, ALL_PRINCIPALS);
    } catch (RemoteException e) {
      throw new ApplicationException("Failed to set the userProfilePermission on the profile fields", e);
    }
  }

  private ArrayList<String> getAllUserProfileGrants() {
    return new ArrayList<String>(Arrays.asList(allUserProfileFieldGrants));
  }

  private String[] getGrants(final ProfileGrantEnum[] grantEnums) {
    final List<String> grantsList = new ArrayList<String>(grantEnums.length);
    for (final ProfileGrantEnum grantEnum : grantEnums) {
      grantsList.add(grantEnum.getGrant());
    }

    return grantsList.toArray(new String[grantsList.size()]);
  }

  private static String[] getAllUserProfileFieldGrants() {
    final ProfileGrantEnum[] profileGrantEnums = ProfileGrantEnum.values();
    final Collection<String> allGrantsList =  new ArrayList<String>(profileGrantEnums.length);
    for (final ProfileGrantEnum allProfileGrantEnum : profileGrantEnums) {
      allGrantsList.add(allProfileGrantEnum.getGrant());
    }

    return allGrantsList.toArray(new String[profileGrantEnums.length]);
  }

  /**
   * Retrieves user preferences for this application and this Topaz user ID
   *
   * @param appId
   *          application ID
   * @param topazUserId
   *          Topaz User ID
   * @return array of user preferences
   * @throws ApplicationException ApplicationException
   */
  public UserPreference[] getPreferences(final String appId, final String topazUserId)
      throws ApplicationException {
    try {
      return preferencesWebService.getPreferences(appId, topazUserId);
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
   * @throws ApplicationException ApplicationException
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
   * @param topazUserId
   *          Topaz User ID
   * @param prefs
   *          User preferences to write
   * @throws ApplicationException ApplicationException
   */
  protected void setPreferences(final String appId, final String topazUserId, UserPreference[] prefs)
      throws ApplicationException {
    try {
      preferencesWebService.setPreferences(appId, topazUserId, prefs);
    } catch (NoSuchIdException ne) {
      throw new ApplicationException(ne);
    } catch (RemoteException re) {
      throw new ApplicationException(re);
    }
  }

  /**
   * @see org.plos.user.service.UserService#setRole(String, String[])
   */
  public void setRole(final String topazId, final String roleId) throws ApplicationException {
    setRole(topazId, new String[]{roleId});
  }

  /**
   * @see org.plos.user.service.UserRoleWebService#setRole(String, String[])
   */
  public void setRole(final String topazId, final String[] roleIds) throws ApplicationException {
    try {
      userRoleWebService.setRole(topazId, roleIds);
    } catch (RemoteException re) {
      throw new ApplicationException("topazId:" + topazId + ", roleIds:" + Arrays.toString(roleIds), re);
    } catch (NoSuchUserIdException nsuie) {
      throw new ApplicationException(nsuie);
    }
  }

  /**
   * Get the roles for the user.
   * @param topazId topazId
   * @see org.plos.user.service.UserRoleWebService#getRoles(String)
   */
  public String[] getRole(final String topazId) throws ApplicationException {
    try {
      return userRoleWebService.getRoles(topazId);
    } catch (RemoteException re) {
      throw new ApplicationException(re);
    } catch (NoSuchUserIdException nsuie) {
      throw new ApplicationException(nsuie);
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
   * @return the userRoleWebService
   */
  public UserRoleWebService getUserRoleWebService() {
    return userRoleWebService;
  }

  /**
   * Set the userRoleWebService
   * @param userRoleWebService userRoleWebService
   */
  public void setUserRoleWebService(final UserRoleWebService userRoleWebService) {
    this.userRoleWebService = userRoleWebService;
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

  /**
   * @return the url from which the email address of the given guid can be fetched
   */
  public String getEmailAddressUrl() {
    return emailAddressUrl;
  }

  /**
   * @param emailAddressUrl
   *          The url from which the email address of the given guid can be fetched
   */
  public void setEmailAddressUrl(final String emailAddressUrl) {
    this.emailAddressUrl = emailAddressUrl;
  }

  /**
   * Getter for property 'permissionWebService'.
   *
   * @return Value for property 'permissionWebService'.
   */
  public PermissionWebService getPermissionWebService() {
    return permissionWebService;
  }

  /**
   * Setter for property 'permissionWebService'.
   *
   * @param permissionWebService Value to set for property 'permissionWebService'.
   */
  public void setPermissionWebService(final PermissionWebService permissionWebService) {
    this.permissionWebService = permissionWebService;
  }
}
