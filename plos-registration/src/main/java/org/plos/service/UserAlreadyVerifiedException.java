package org.plos.service;

/**
 * To be used when a user who has been already verified requests a verification again
 * $HeadURL: $
 * @version: $Id: $
 */
public class UserAlreadyVerifiedException extends Exception {
  /**
   * Constructor with loginName
   * @param loginName loginName
   */
  public UserAlreadyVerifiedException(final String loginName) {
    super(loginName);
  }
}
