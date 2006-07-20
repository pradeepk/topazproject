package org.plos.service;

/**
 * No user found with given login name exception.
 *
 * $HeadURL: $
 * @version: $Id: $
 */
public class NoUserFoundWithGivenLoginNameException extends Exception {
  /**
   * Constructor with loginName
   * @param loginName name of the user
   */
  public NoUserFoundWithGivenLoginNameException(final String loginName) {
    super(loginName);
  }
}
