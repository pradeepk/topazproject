package org.plos.service;

/**
 * To manage the interactions with the persistence service, including database persistence.
 *
 * $HeadURL: $
 * @version: $Id: $
 */
public interface PersistenceService {
  /**
   * Sets the UserDAO
   * @param userDAO UserDAO
   */
  void setUserDAO(final UserDAO userDAO);

  /**
   * @return UserDAO
   */
  UserDAO getUserDAO();
}
