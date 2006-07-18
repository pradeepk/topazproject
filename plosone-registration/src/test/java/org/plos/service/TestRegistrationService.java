package org.plos.service;

import org.plos.registration.User;

/**
 * $HeadURL$
 * @version: $Id$
 */
public class TestRegistrationService extends BasePlosoneRegistrationTest {
  private RegistrationService registrationService;

  public void testNewUser() {
    User user = registrationService.createUser("dave@home.com", "david");
    assertNotNull(user.getId());
    assertFalse(user.isVerified());
    assertFalse(user.isActive());
  }

  public void testVerifiedUser() {
    User user = registrationService.createUser("viru@home.com", "virender");
    registrationService.setVerified(user);
    assertTrue(user.isVerified());
    assertTrue(user.isActive());
  }

  public void testDeactivatedUser() {
    final String email = "susie@home.com";
    registrationService.createUser(email, "susan");
    User user = registrationService.getUserWithLoginName(email);
    registrationService.deactivate(user);
    assertFalse(user.isVerified());
    assertFalse(user.isActive());
  }

  public void setRegistrationService(final RegistrationService registrationService) {
    this.registrationService = registrationService;
  }
}
