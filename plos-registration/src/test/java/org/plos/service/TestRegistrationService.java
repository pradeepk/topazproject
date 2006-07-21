/* $HeadURL::                                                                            $
 * $Id$
 *
 */
package org.plos.service;

import org.plos.BasePlosoneRegistrationTestCase;
import org.plos.registration.User;

/**
 *
 */
public class TestRegistrationService extends BasePlosoneRegistrationTestCase {

  public void testNewUser() throws UserAlreadyExistsException {
    User user = getRegistrationService().createUser("dave@home.com", "david");
    assertNotNull(user.getId());
    assertFalse(user.isVerified());
    assertFalse(user.isActive());
  }

  public void testVerifiedUser() throws UserAlreadyExistsException {
    User user = getRegistrationService().createUser("viru@home.com", "virender");
    getRegistrationService().setVerified(user);
    assertTrue(user.isVerified());
    assertTrue(user.isActive());
  }

  public void testDeactivatedUser() throws UserAlreadyExistsException {
    final String email = "susie@home.com";
    getRegistrationService().createUser(email, "susan");
    final User user = getRegistrationService().getUserWithLoginName(email);
    getRegistrationService().deactivate(user);
    assertFalse(user.isVerified());
    assertFalse(user.isActive());
  }

}
