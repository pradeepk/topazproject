/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.topazproject.ws.pap;

import java.rmi.Remote;
import java.rmi.RemoteException;
import org.topazproject.ws.users.NoSuchUserIdException;

/** 
 * This defines the profiles service. It allows a single profile to be associated with each user.
 * 
 * @author Ronald Tschalär
 */
public interface Profiles extends Remote {
  /**
   * Permissions associated with the profiles service.
   */
  public static interface Permissions {
    /** The action that represents a get-display-name operation in XACML policies: {@value}. */
    public static final String GET_DISP_NAME = "profiles:getDisplayName";

    /** The action that represents a get-real-name operation in XACML policies: {@value}. */
    public static final String GET_REAL_NAME = "profiles:getRealName";

    /** The action that represents a get-title operation in XACML policies: {@value}. */
    public static final String GET_TITLE = "profiles:getTitle";

    /** The action that represents a get-gender operation in XACML policies: {@value}. */
    public static final String GET_GENDER = "profiles:getGender";

    /** The action that represents a get-email operation in XACML policies: {@value}. */
    public static final String GET_EMAIL = "profiles:getEmail";

    /** The action that represents a get-home-page operation in XACML policies: {@value}. */
    public static final String GET_HOME_PAGE = "profiles:getHomePage";

    /** The action that represents a get-weblog operation in XACML policies: {@value}. */
    public static final String GET_WEBLOG = "profiles:getWeblog";

    /** The action that represents a get-biography operation in XACML policies: {@value}. */
    public static final String GET_BIOGRAPHY = "profiles:getBiography";

    /** The action that represents a get-interests operation in XACML policies: {@value}. */
    public static final String GET_INTERESTS = "profiles:getInterests";

    /** The action that represents a get-publications operation in XACML policies: {@value}. */
    public static final String GET_PUBLICATIONS = "profiles:getPublications";

    /** The action that represents a set-profile operation in XACML policies: {@value}. */
    public static final String SET_PROFILE = "profiles:setProfile";
  }

  /** 
   * Get a user's profile. Note that all fields in the profile are subject to access-control checks,
   * and any field to which the user calling this service does not have access will be null'd out.
   * 
   * @param userId  the user's internal id
   * @return the user's profile, or null if the user does not have a profile
   * @throws NoSuchUserIdException if the user does not exist
   * @throws RemoteException if some other error occured
   */
  public UserProfile getProfile(String userId) throws NoSuchUserIdException, RemoteException;

  /** 
   * Set a user's profile. This completely replaces the current profile (if any) with the given one.
   * 
   * @param userId  the user's internal id
   * @param profile the user's new profile; may be null in which case the profile is erased
   * @throws NoSuchUserIdException if the user does not exist
   * @throws RemoteException if some other error occured
   */
  public void setProfile(String userId, UserProfile profile)
      throws NoSuchUserIdException, RemoteException;
}
