package org.plos.service;

/**
 * Plos implementation of the Persistence Service.
 * $HeadURL: $
 * @version: $Id: $
 */
public class PlosPersistenceService implements PersistenceService {
  private UserDAO userDAO;

  /**
   * @see PersistenceService#setUserDAO(UserDAO)
   */
  public void setUserDAO(final UserDAO userDAO) {
    this.userDAO = userDAO;
  }

  /**
   * @see org.plos.service.PersistenceService#getUserDAO() 
   */
  public UserDAO getUserDAO() {
    return userDAO;
  }
}
