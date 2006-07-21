package org.plos.service;

/**
 * $HeadURL$
 * @version: $Id$
 * 
 * No user found with given login name exception.
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
