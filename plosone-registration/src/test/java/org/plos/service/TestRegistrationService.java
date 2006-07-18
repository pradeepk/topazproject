package org.plos.service;

import org.plos.BasePlosoneRegistrationTest;
import org.plos.registration.User;

/**
 * $HeadURL$
 * @version: $Id$
 */
public class TestRegistrationService extends BasePlosoneRegistrationTest {

  public void testNewUser() {
    User user = getRegistrationService().createUser("dave@home.com", "david");
    assertNotNull(user.getId());
    assertFalse(user.isVerified());
    assertFalse(user.isActive());
  }

  public void testVerifiedUser() {
    User user = getRegistrationService().createUser("viru@home.com", "virender");
    getRegistrationService().setVerified(user);
    assertTrue(user.isVerified());
    assertTrue(user.isActive());
  }

  public void testDeactivatedUser() {
    final String email = "susie@home.com";
    getRegistrationService().createUser(email, "susan");
    final User user = getRegistrationService().getUserWithLoginName(email);
    getRegistrationService().deactivate(user);
    assertFalse(user.isVerified());
    assertFalse(user.isActive());
  }

}
