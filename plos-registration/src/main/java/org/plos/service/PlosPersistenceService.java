package org.plos.service;

/**
 * Plos implementation of the Persistence Service.
 * $HeadURL: $
 * @version: $Id: $
 */
public class PlosPersistenceService implements PersistenceService {
  private UserDAO userDAO;

  public void setUserDAO(final UserDAO userDAO) {
    this.userDAO = userDAO;
  }

  public UserDAO getUserDAO() {
    return userDAO;
  }
}
