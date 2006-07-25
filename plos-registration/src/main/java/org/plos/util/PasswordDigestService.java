/* $HeadURL::                                                                            $
 * $Id$
 *
 */
package org.plos.util;

/**
 * Password digest service. It should hash the password such that it should be difficult to get the original password back from it.
 */
public interface PasswordDigestService {
  /**
   * Return the digest for a given password
   * @param password password
   * @return digest of the password
   */
  String getDigestPassword(final String password);

  /**
   * Return a digest of the password also known as hashing. Use the salt as provided to make the deduction of the original password more time consuming.
   * @param password password
   * @param salt salt
   * @return digest password
   */
  String getDigestPassword(final String password, final String salt);

  /**
   * Verify that the user password matched the digest password
   * @param originalPassword user's password
   * @param printableDigestPassword digest password
   * @return true if verified successfully, false otherwise
   */
  boolean verifyPassword(final String originalPassword, final String printableDigestPassword);
}
