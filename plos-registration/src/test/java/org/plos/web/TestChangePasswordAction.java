/* $HeadURL::                                                                            $
 * $Id$
 */
package org.plos.web;

import org.plos.registration.User;
import org.plos.BasePlosoneRegistrationTestCase;
import com.opensymphony.xwork.Action;

public class TestChangePasswordAction extends BasePlosoneRegistrationTestCase {

  public void testShouldChangeUserPassword() throws Exception {
    final String email = "user-changing-their-oldPassword@home.com";
    final String oldPassword = "changethispassword";

    createUser(email, oldPassword);
    final User beforeVerificationUser = getRegistrationService().getUserWithLoginName(email);
    assertNotNull(beforeVerificationUser);

    getRegistrationService().verifyUser(email, beforeVerificationUser.getEmailVerificationToken());

    final ChangePasswordAction changePasswordAction = getChangePasswordAction();
    changePasswordAction.setLoginName(email);
    changePasswordAction.setOldPassword(oldPassword);
    changePasswordAction.setNewPassword1("newPassword1");
    assertEquals(Action.SUCCESS, changePasswordAction.execute());
    assertEquals(0, changePasswordAction.getFieldErrors().size());
  }

  public void testShouldFailToChangeUserPasswordIfUserNotVerified() throws Exception {
    final String email = "unverified-user-changing-their-oldPassword@home.com";
    final String oldPassword = "changethispassword";

    createUser(email, oldPassword);

    final ChangePasswordAction changePasswordAction = getChangePasswordAction();
    changePasswordAction.setLoginName(email);
    changePasswordAction.setOldPassword(oldPassword);
    changePasswordAction.setNewPassword1("newPassword1");
    assertEquals(Action.ERROR, changePasswordAction.execute());
    assertEquals(1, changePasswordAction.getFieldErrors().size());
  }

  public void testShouldFailToChangeUserPasswordIfOldPasswordIsWrong() throws Exception {
    final String email = "verified-user-changing-their-wrong-oldPassword@home.com";
    final String oldPassword = "changethispassword";

    createUser(email, oldPassword);
    final User beforeVerificationUser = getRegistrationService().getUserWithLoginName(email);
    getRegistrationService().verifyUser(email, beforeVerificationUser.getEmailVerificationToken());

    final ChangePasswordAction changePasswordAction = getChangePasswordAction();
    changePasswordAction.setLoginName(email);
    changePasswordAction.setOldPassword(oldPassword+"11");
    changePasswordAction.setNewPassword1("newPassword1");
    assertEquals(Action.ERROR, changePasswordAction.execute());
    assertEquals(1, changePasswordAction.getFieldErrors().size());
  }

  public void testShouldFailToChangeUserPasswordIfUserNotFound() throws Exception {
    final String email = "not-found-user-changing-their-wrong-oldPassword@home.com";
    final String oldPassword = "changethispassword";

    final ChangePasswordAction changePasswordAction = getChangePasswordAction();
    changePasswordAction.setLoginName(email);
    changePasswordAction.setOldPassword(oldPassword);
    changePasswordAction.setNewPassword1("newPassword1");
    assertEquals(Action.ERROR, changePasswordAction.execute());
    assertEquals(1, changePasswordAction.getFieldErrors().size());
  }

}
