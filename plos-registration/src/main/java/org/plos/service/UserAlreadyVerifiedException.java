package org.plos.service;

/**
 * To be used when a user verification is requested again
 * $HeadURL: $
 * @version: $Id: $
 */
public class UserAlreadyVerifiedException extends Exception {
  /**
   * Constructor with loginName
   * @param loginName
   */
  public UserAlreadyVerifiedException(final String loginName) {
    super(loginName);
  }
}
