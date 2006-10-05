/* $HeadURL:: http://gandalf.topazproject.org/svn/head/plos-registration/src/test/java/o#$
 * $Id: TestPasswordDigestService.java 424 2006-08-09 23:16:31Z viru $
 *
 */
package org.plos.service.password;

import java.util.Random;

import junit.framework.TestCase;

public class TestPasswordDigestService extends TestCase {

  public void testHashingShouldGiveDifferentReturnValue() throws PasswordServiceException {
    final PasswordDigestService passwordDigestService = getPasswordDigestService();

    final Random random = new Random();

    for (int count = 0; count < 100; count++) {
      final StringBuilder sb = new StringBuilder();
      for (int length = 1; length < random.nextInt(20); length++ ) {
        final char ch = (char)(64 + random.nextInt(60));
        sb.append(ch);
      }

      final String originalPassword = sb.toString();
      final String digestPassword = passwordDigestService.getDigestPassword(originalPassword);
      assertFalse(originalPassword.equalsIgnoreCase(digestPassword));
      assertTrue(passwordDigestService.verifyPassword(originalPassword, digestPassword));
    }
  }

  public void testVerifyPassportService() throws PasswordServiceException {
    final PasswordDigestService passwordDigestService = getPasswordDigestService();
    //Quick check of the password service with a value from the database copied over from expected
    final String expected = "dbf86cea944e947c7d077797230ab98df4d0e6093b1ee871f93de4c3d2abcd7123d4d1";
    final String password = "fedoraAdmin";
    final String digest = passwordDigestService.getDigestPassword(password);
//    assertTrue(passwordDigestService.verifyPassword(password, expected));
  }

  public void testVerificationShouldFailForWrongPassword() throws PasswordServiceException {
    final PasswordDigestService passwordDigestService = getPasswordDigestService();

    final Random random = new Random();

    for (int count = 0; count < 100; count++) {
      final StringBuilder sb = new StringBuilder();
      for (int length = 1; length < random.nextInt(20); length++ ) {
        final char ch = (char)(64 + random.nextInt(60));
        sb.append(ch);
      }

      final String originalPassword = sb.toString();
      final String digestPassword = passwordDigestService.getDigestPassword(originalPassword);
      assertFalse(originalPassword.equalsIgnoreCase(digestPassword));
      assertFalse(passwordDigestService.verifyPassword(originalPassword+"1", digestPassword));
    }
  }

  public void testHashingOfSameStringShouldGiveDifferentResult() throws InterruptedException, PasswordServiceException {
    final PasswordDigestService passwordDigestService = getPasswordDigestService();

    final Random random = new Random();

    for (int count = 0; count < 100; count++) {
      final StringBuilder sb = new StringBuilder();
      for (int length = 1; length < random.nextInt(20); length++) {
        final char ch = (char) (64 + random.nextInt(60));
        sb.append(ch);
      }

      final String originalPassword = sb.toString();
      final String digestPassword1 = passwordDigestService.getDigestPassword(originalPassword);
      Thread.sleep(40);
      final String digestPassword2 = passwordDigestService.getDigestPassword(originalPassword);
      assertFalse(digestPassword1.equalsIgnoreCase(digestPassword2));

    }
  }

  private PasswordDigestService getPasswordDigestService() {
    final PasswordDigestService passwordDigestService = new PasswordDigestService();
    passwordDigestService.setAlgorithm("SHA-256");
    passwordDigestService.setEncodingCharset("UTF-8");
    return passwordDigestService;
  }
}
