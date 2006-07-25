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

  /**
   * Return a one-way encryption of the password also known as hashing. Use the salt as provided to make the deduction of the original password more time consuming.
   * @param password password
   * @param salt salt
   * @return encrypted password
   */
  String getEncryptedPassword(String password, String salt);

  /**
   * Verify that the user password matached the encrypted password
   * @param originalPassword user's password
   * @param encryptedPassword encypted password
   * @return true if verified successfully, false otherwise
   */
  boolean verifyPassword(String originalPassword, String encryptedPassword);
}
