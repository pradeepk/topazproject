/* $HeadURL::                                                                            $
 * $Id$
 *
 */
package org.plos.util;

import org.plos.BasePlosoneRegistrationTestCase;
import org.plos.registration.User;
import org.plos.service.UserAlreadyExistsException;

import java.util.Random;

/**
 *
 */
public class TestPasswordDigestService extends BasePlosoneRegistrationTestCase {

  public void testHashingShouldGiveDifferentReturnValue() {
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

  public void testVerificationShouldFailForWrongPassword() {
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

//  public void testHashingOfSameStringShouldGiveDifferentResult() throws InterruptedException {
//    final PasswordDigestService passwordDigestService = getPasswordDigestService();
//
//    final Random random = new Random();
//
//    for (int count = 0; count < 100; count++) {
//      final StringBuilder sb = new StringBuilder();
//      for (int length = 1; length < random.nextInt(20); length++) {
//        final char ch = (char) (64 + random.nextInt(60));
//        sb.append(ch);
//      }
//
//      final String originalPassword = sb.toString();
//      final String digestPassword1 = passwordDigestService.getDigestPassword(originalPassword);
//      Thread.sleep(50);
//      final String digestPassword2 = passwordDigestService.getDigestPassword(originalPassword);
//      assertFalse(digestPassword1.equalsIgnoreCase(digestPassword2));
//
//    }
//  }

  public void testUserPasswdSavedInDatabaseShouldBeDifferentFromWhatUserEntered() throws UserAlreadyExistsException {
    final String email = "viru-verifying-for-password-digest@home.com";
    final String password = "virupasswd";
    final User saveUser = getRegistrationService().createUser(email, password);
    assertFalse(saveUser.getPassword().equalsIgnoreCase(password));
  }

}
