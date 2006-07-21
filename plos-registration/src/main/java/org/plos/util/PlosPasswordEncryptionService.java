package org.plos.util;

/**
 * $HeadURL$
 * @version: $Id$
 * 
 * Plos implementation of the PasswordEncryptionService
 */
public class PlosPasswordEncryptionService implements PasswordEncryptionService {
  /**
   * @see PasswordEncryptionService#getEncryptedPassword(String)
   */
  public String getEncryptedPassword(final String password) {
    return String.valueOf(password.hashCode());
  }
}
