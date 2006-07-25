/* $HeadURL::                                                                            $
 * $Id$
 *
 */
package org.plos.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.math.random.RandomDataImpl;
import org.plos.ApplicationException;

import java.security.MessageDigest;
import java.io.ByteArrayOutputStream;
import java.util.StringTokenizer;

/**
 * Plos implementation of the PasswordEncryptionService
 */
public class PlosPasswordEncryptionService implements PasswordEncryptionService {
  private String algorithm;
  private final String ERROR_MESSAGE = "Password digesting failed";

  private static final Log log = LogFactory.getLog(PlosPasswordEncryptionService.class);

  /**
   * Set the algorithm.
   * @param algorithm algorithm for hashing
   */
  public void setAlgorithm(final String algorithm) {
    this.algorithm = algorithm;
  }

  /**
   * @see PasswordEncryptionService#getEncryptedPassword(String)
   */
  public String getEncryptedPassword(final String password) {
    final String randomSalt = createRandomSalt();

    return getEncryptedPassword(password, randomSalt);
  }

  /**
   * @see PasswordEncryptionService#getEncryptedPassword(String, String)
   */
  public String getEncryptedPassword(final String password, final String salt) {
    try {
      final MessageDigest md = MessageDigest.getInstance(algorithm);
      final byte[] bytes = md.digest((password + salt).getBytes());
      return getString(bytes) + salt;
    }
    catch (final Exception ex) {
      log.error(ERROR_MESSAGE, ex);
      throw new ApplicationException(ERROR_MESSAGE, ex);
    }
  }

  /**
   * @see PasswordEncryptionService#verifyPassword(String, String)
   */
  public boolean verifyPassword(final String password, final String printableEncryptedPassword) {
    final String encryptedPassword = new String(getBytes(printableEncryptedPassword));
    final int saltSuffixIndex = encryptedPassword.length() - getSaltLength();
    final String salt = getSalt(encryptedPassword, saltSuffixIndex);
    final String newEncryptedPassword = getEncryptedPassword(password, salt);

    return printableEncryptedPassword.equals(newEncryptedPassword);
  }

  /**
   * Extract the salt from the input
   * @param encryptedPassword encryptedPassword
   * @param saltSuffixIndex saltSuffixIndex
   * @return the salt
   */
  private String getSalt(final String encryptedPassword, final int saltSuffixIndex) {
    return encryptedPassword.substring(saltSuffixIndex, encryptedPassword.length());
  }

  private String createRandomSalt() {
    final RandomDataImpl random = new RandomDataImpl();
    final StringBuilder sb = new StringBuilder();

    for (int length = 0; length < getSaltLength(); length++ ) {
      //todo Problem area
      final char ch = (char)(random.nextInt(0, 16));
      sb.append(ch);
    }

    return sb.toString();
  }

  /**
   * Convert the bytes to hex notational printable characters
   * @param bytes bytes
   * @return the printable bytes
   */
  private static String getString(final byte[] bytes) {
    final StringBuilder sb = new StringBuilder();
    for (int i = 0; i < bytes.length; i++) {
      final byte b = bytes[i];
      final int intByte = (int) (0x00FF & b);
      sb.append(Integer.toHexString(intByte));
      if (i + 1 < bytes.length) {
        sb.append("-");
      }
    }
    return sb.toString();
  }

  /**
   * @param str source string delimited with hex characters that are "-" delimited
   * @return A binary sequence of characters converted from the input hex string.
   */
  private static byte[] getBytes(final String str) {
    final ByteArrayOutputStream bos = new ByteArrayOutputStream();
    final StringTokenizer st = new StringTokenizer(str, "-", false);
    while (st.hasMoreTokens()) {
      final int i = Integer.parseInt(st.nextToken(), 16);
      bos.write((byte) i);
    }
    return bos.toByteArray();
  }

  private int getSaltLength() {
//    return 6;
    return 0;
  }

}
