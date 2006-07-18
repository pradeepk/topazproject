package org.plos.service;

import org.plos.registration.User;

/**
 * Contract for all User DAO's.
 * $HeadURL$
 * @version: $Id$
 */
 public interface UserDAO {

  /**
   * Save or update the user
   * @param user User
   */
  void saveOrUpdate(final User user);

  /**
   * Find user with a given login name.
   * @param loginName
   * @return User
   */
  User findUserWithLoginName(final String loginName);

}

