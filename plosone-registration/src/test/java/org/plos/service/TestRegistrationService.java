package org.plos.service;

import junit.framework.TestCase;
import org.plos.User;

/**
 * $HeadURL$
 * @version: $Id$
 */
public class TestRegistrationService extends TestCase {
  private ServiceFactory serviceFactory;
  private RegistrationService registrationService;

  protected void setUp() throws Exception {
    serviceFactory = new ServiceFactory();
    registrationService = serviceFactory.getRegistrationService();
  }

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
    final String emailAddress = "susie@home.com";
    registrationService.createUser(emailAddress, "susan");
    User user = registrationService.getUser(emailAddress);
    registrationService.deactivate(user);
    assertFalse(user.isVerified());
    assertFalse(user.isActive());
  }

  public void testSameInstanceOfUserServiceIsReturnedByServiceFactory() {
    org.plos.service.RegistrationService registrationService1 = serviceFactory.getRegistrationService();
    org.plos.service.RegistrationService registrationService2 = serviceFactory.getRegistrationService();
    assertEquals(registrationService1, registrationService2);
  }
}
