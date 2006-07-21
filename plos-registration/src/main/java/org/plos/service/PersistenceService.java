package org.plos.service;

/**
 * $HeadURL$
 * @version: $Id$
 * 
 * To manage the interactions with the persistence service, including database persistence.
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
