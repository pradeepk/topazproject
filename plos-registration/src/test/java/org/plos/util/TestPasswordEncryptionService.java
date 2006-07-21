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
    final PasswordEncryptionService passwordEncryptionService = new PlosPasswordEncryptionService();

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

    }
  }

  public void testUserPasswdShouldBeEncrypted() throws UserAlreadyExistsException {
    final String email = "viru-verifying-for-password-encryption@home.com";
    final String password = "virupasswd";
    final User saveUser = getRegistrationService().createUser(email, password);
    assertFalse(saveUser.getPassword().equalsIgnoreCase(password));
  }
}
