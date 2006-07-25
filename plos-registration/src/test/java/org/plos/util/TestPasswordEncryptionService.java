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
public class TestPasswordEncryptionService extends BasePlosoneRegistrationTestCase {

  public void testEncryptionShouldGiveDifferentReturnValue() {
    final PasswordEncryptionService passwordEncryptionService = getPasswordEncryptionService();

    final Random random = new Random();

    for (int count = 0; count < 100; count++) {
      final StringBuilder sb = new StringBuilder();
      for (int length = 1; length < random.nextInt(20); length++ ) {
        final char ch = (char)(64 + random.nextInt(60));
        sb.append(ch);
      }

      final String originalPassword = sb.toString();
      final String encryptedPassword = passwordEncryptionService.getEncryptedPassword(originalPassword);
      assertFalse(originalPassword.equalsIgnoreCase(encryptedPassword));
      assertTrue(passwordEncryptionService.verifyPassword(originalPassword, encryptedPassword));
    }
  }

  public void testVerificationShouldFailForWrongPassword() {
    final PasswordEncryptionService passwordEncryptionService = getPasswordEncryptionService();

    final Random random = new Random();

    for (int count = 0; count < 100; count++) {
      final StringBuilder sb = new StringBuilder();
      for (int length = 1; length < random.nextInt(20); length++ ) {
        final char ch = (char)(64 + random.nextInt(60));
        sb.append(ch);
      }

      final String originalPassword = sb.toString();
      final String encryptedPassword = passwordEncryptionService.getEncryptedPassword(originalPassword);
      assertFalse(originalPassword.equalsIgnoreCase(encryptedPassword));
      assertFalse(passwordEncryptionService.verifyPassword(originalPassword+"1", encryptedPassword));
    }
  }

//  public void testEncryptionOfSameStringShouldGiveDifferentEncryptionResult() throws InterruptedException {
//    final PasswordEncryptionService passwordEncryptionService = getPasswordEncryptionService();
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
//      final String encryptedPassword1 = passwordEncryptionService.getEncryptedPassword(originalPassword);
//      Thread.sleep(50);
//      final String encryptedPassword2 = passwordEncryptionService.getEncryptedPassword(originalPassword);
//      assertFalse(encryptedPassword1.equalsIgnoreCase(encryptedPassword2));
//
//    }
//  }

  public void testUserPasswdSavedInDatabaseShouldBeDifferentFromWhatUserEntered() throws UserAlreadyExistsException {
    final String email = "viru-verifying-for-password-encryption@home.com";
    final String password = "virupasswd";
    final User saveUser = getRegistrationService().createUser(email, password);
    assertFalse(saveUser.getPassword().equalsIgnoreCase(password));
  }

}
