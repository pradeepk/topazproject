/* $HeadURL::                                                                            $
 * $Id$
 */
package org.plos.web;

import org.plos.BasePlosoneRegistrationTestCase;
import org.plos.registration.User;

/**
 *
 */
public class TestRegistrationAction extends BasePlosoneRegistrationTestCase {
  public void testShouldCreateAUserAccount() throws Exception {
    final String email = "viru-creating-a-user-account@home.com";
    final String password = "virupasswd";

    createUser(email, password);
    final User persistedUser = getRegistrationService().getUserWithLoginName(email);
    assertNotNull(persistedUser);
    assertEquals(persistedUser.getLoginName(), email);
    assertNotNull(persistedUser.getPassword());
    assertFalse(persistedUser.isActive());
    assertFalse(persistedUser.isVerified());
  }

  public void testShouldFailToCreateAnotherAccountWithSameEmail() throws Exception {
    final String email = "viru-creating-a-account-twice@home.com";
    final String password = "virupasswd";

    createUser(email, password);
    final User beforeVerificationUser = getRegistrationService().getUserWithLoginName(email);
    assertNotNull(beforeVerificationUser);

    final RegisterAction registerAction = getRegistrationAction();
    registerAction.setLoginName1(email);
    registerAction.setLoginName2(email);
    registerAction.setPassword1(password);
    registerAction.setPassword2(password);
    registerAction.execute();
    assertTrue(registerAction.getFieldErrors().size() > 0);
  }

}
