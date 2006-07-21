package org.plos.service;

/**
 * $HeadURL$
 * @version: $Id$
 *
 * User already exists exception if the same user is being created again. 
 */
public class UserAlreadyExistsException extends Exception {
  /**
   * Constructor with loginName.
   * @param loginName name that already exists.
   */
  public UserAlreadyExistsException(final String loginName) {
    super(loginName);
  }
}
