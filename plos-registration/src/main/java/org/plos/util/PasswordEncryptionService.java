/* $HeadURL::                                                                            $
 * $Id$
 *
 */
package org.plos.util;

/**
 * Password encryption service. It should encrypt the password such that it should be difficult to get the original password back from it.
 */
public interface PasswordEncryptionService {
  /**
   * Return the encrypted string for a given password
   * @param password password
   * @return encrypted password
   */
  String getEncryptedPassword(final String password);
}
