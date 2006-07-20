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
 * This defines the profiles service.
 * 
 * @author Ronald Tschalär
 */
public interface Profiles {
  /** 
   * Create a new profile for a user.
   * 
   * @param userId  the user's internal id
   * @param profile the user's initial profile; may be null to create an empty profile
   * @throws DuplicateIdException if the user already has a profile
   * @throws RemoteException if some other error occured
   */
  public void createProfile(String userId, UserProfile profile)
      throws DuplicateIdException, RemoteException;

  /** 
   * Get a user's profile.
   * 
   * @param userId  the user's internal id
   * @return the user's profile
   * @throws NoSuchIdException if the user does not have a profile
   * @throws RemoteException if some other error occured
   */
  public UserProfile getProfile(String userId) throws NoSuchIdException, RemoteException;

  /** 
   * Update a user's profile. This completely replaces the profile with the given one.
   * 
   * @param userId  the user's internal id
   * @param profile the user's new profile
   * @throws NoSuchIdException if the user does not have a profile
   * @throws RemoteException if some other error occured
   */
  public void updateProfile(String userId, UserProfile profile)
      throws NoSuchIdException, RemoteException;

  /** 
   * Delete a user's profile.
   * 
   * @param userId  the user's internal id
   * @throws NoSuchIdException if the user does not have a profile
   * @throws RemoteException if some other error occured
   */
  public void deleteProfile(String userId) throws NoSuchIdException, RemoteException;
}
