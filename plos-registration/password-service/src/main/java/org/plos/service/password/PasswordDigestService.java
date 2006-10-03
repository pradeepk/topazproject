/* $HeadURL:: http://gandalf.topazproject.org/svn/head/plos-registration/src/main/java/o#$
 * $Id: PasswordDigestService.java 680 2006-09-25 17:51:50Z viru $
 *
 */
package org.plos.service.password;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.math.random.RandomDataImpl;

import java.security.MessageDigest;

/**
 * Password digest service. It should hash the password such that it should be
 * difficult to get the original password back from it.
 */
public class PasswordDigestService {
  private String algorithm;
  private final String ERROR_MESSAGE = "Password digesting failed";

  private static final Log log = LogFactory.getLog(PasswordDigestService.class);
  private String encodingCharset;

  /**
   * Set the algorithm.
   * @param algorithm algorithm for hashing
   */
  public void setAlgorithm(final String algorithm) {
    this.algorithm = algorithm;
  }

  /**
   * Return the digest for a given password
   * @param password password
   * @return digest of the password
   * @throws PasswordServiceException on failure
   */
  public String getDigestPassword(final String password) throws PasswordServiceException {
    return getDigestPassword(password, createRandomSalt());
  }


  /**
   * Return a digest of the password also known as hashing. Use the salt as provided to make the deduction of the original password more time consuming.
   * @param password password
   * @param salt salt
   * @return digest password
   * @throws PasswordServiceException on failure
   */
  private String getDigestPassword(final String password, final String salt) throws PasswordServiceException {
    try {
      final MessageDigest md = MessageDigest.getInstance(algorithm);
      final byte[] bytes = md.digest((salt + password).getBytes(encodingCharset));
      return salt + getString(bytes);
    }
    catch (final Exception ex) {
      log.error(ERROR_MESSAGE, ex);
      throw new PasswordServiceException(ERROR_MESSAGE, ex);
    }
  }

  /**
   * Verify that the user password matched the digest password
   * @param passwordToVerify user's password
   * @param savedPassword digest password
   * @return true if verified successfully, false otherwise
   * @throws PasswordServiceException on failure
   */
  public boolean verifyPassword(final String passwordToVerify, final String savedPassword) throws PasswordServiceException {
    final String salt = getSalt(savedPassword);
    final String newPasswordDigest = getDigestPassword(passwordToVerify, salt);

    return savedPassword.equals(newPasswordDigest);
  }

  private String getSalt(final String digestPassword) {
    return digestPassword.substring(0, getSaltLength());
  }

  private String createRandomSalt() {
    final RandomDataImpl random = new RandomDataImpl();
    return random.nextHexString(getSaltLength());
  }

  /**
   * Convert the bytes to hex notational printable characters
   * @param bytes bytes
   * @return the printable bytes
   */
  private static String getString(final byte[] bytes) {
    final StringBuilder sb = new StringBuilder();
    for (final byte bite : bytes) {
      final int intByte = (int) (0x00FF & bite);
      sb.append(getPaddedHexString(intByte));
    }
    return sb.toString();
  }

  /**
   * Ensures that the hex string returned is of length 2.
   * @param intByte byte
   * @return padded hex string
   */
  private static String getPaddedHexString(final int intByte) {
    String hexString = Integer.toHexString(intByte);
    if (hexString.length() == 1) {
      hexString = "0" + hexString;
    }
    return hexString;
  }

  /**
   * Keeping the salt length hard coded for now, thinking that it might be better for security than
   * if plainly visible in the spring configuration
   * @return the length of salt
   */
  private int getSaltLength() {
    return 6;
  }

  /**
   * Set the encoding charset for the password
   * @param encodingCharset encodingCharset
   */
  public void setEncodingCharset(final String encodingCharset) {
    this.encodingCharset = encodingCharset;
  }
}
