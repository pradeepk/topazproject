/* $HeadURL::                                                                            $
 * $Id$
 *
 */
package org.plos.util;

/**
 * Password digest service. It should hash the password such that it should be
 * difficult to get the original password back from it.
 */
public interface PasswordDigestService {
  /**
   * Return the digest for a given password
   * @param password password
   * @return digest of the password
   */
  String getDigestPassword(final String password);

  /**
   * Verify that the user password matched the digest password
   * @param passwordToVerify user's password
   * @param savedPassword digest password
   * @return true if verified successfully, false otherwise
   */
  boolean verifyPassword(final String passwordToVerify, final String savedPassword);
}
