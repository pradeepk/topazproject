/*
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.topazproject.ws.pap;

import java.rmi.RemoteException;

/** 
 * This defines the profiles service. It allows a single profile to be associated with each user.
 * 
 * @author Ronald Tschalär
 */
public interface Profiles {
  /** 
   * Get a user's profile.
   * 
   * @param userId  the user's internal id
   * @return the user's profile, or null if the user does not have a profile
   * @throws NoSuchIdException if the user does not exist
   * @throws RemoteException if some other error occured
   */
  public UserProfile getProfile(String userId) throws NoSuchIdException, RemoteException;

  /** 
   * Set a user's profile. This completely replaces the current profile (if any) with the given one.
   * 
   * @param userId  the user's internal id
   * @param profile the user's new profile; may be null in which case the profile is erased
   * @throws NoSuchIdException if the user does not exist
   * @throws RemoteException if some other error occured
   */
  public void setProfile(String userId, UserProfile profile)
      throws NoSuchIdException, RemoteException;
}
