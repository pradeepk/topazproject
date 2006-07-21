package org.plos.service;

import org.plos.ApplicationException;

/**
 * $HeadURL$
 * @version: $Id$
 * 
 * Indicates that multiple users have been found with the same loginName. 
 * 
 */
public class DuplicateLoginNameException extends ApplicationException {

  /**
   * @param loginName loginName for which exception occured
   */
  public DuplicateLoginNameException(final String loginName) {
    super(loginName);
  }
}
