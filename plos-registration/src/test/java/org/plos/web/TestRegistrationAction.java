package org.plos.web;

import org.plos.BasePlosoneRegistrationTestCase;
import org.plos.registration.User;

/**
 * $HeadURL$
 * @version: $Id$
 */
public class TestRegistrationAction extends BasePlosoneRegistrationTestCase {

  public void testShouldSetUserAsVerified() throws Exception {
    final String email = "viru-verifying@home.com";
    final String password = "virupasswd";
    final User beforeVerificationUser = getRegistrationService().createUser(email, password);

    assertFalse(beforeVerificationUser.isVerified());
    final String emailVerificationToken = beforeVerificationUser.getEmailVerificationToken();

    assertNotNull(emailVerificationToken);
    assertTrue(emailVerificationToken.length() > 0);

    final ConfirmationAction confirmationAction = getConfirmationAction();
    confirmationAction.setLoginName(email);
    confirmationAction.setEmailVerificationToken(emailVerificationToken);
    confirmationAction.execute();

    assertTrue(confirmationAction.getMessages().isEmpty());
    final User verifiedUser = getRegistrationService().getUserWithLoginName(email);
    assertTrue(verifiedUser.isVerified());
  }

  public void testShouldNotVerifyUser() throws Exception {
    final String email = "viru-verifying-another-time@home.com";
    final User beforeVerificationUser = getRegistrationService().createUser(email, "virupasswd");

    assertFalse(beforeVerificationUser.isVerified());
    final String emailVerificationToken = beforeVerificationUser.getEmailVerificationToken();

    assertNotNull(emailVerificationToken);
    assertTrue(emailVerificationToken.length() > 0);

    final ConfirmationAction confirmationAction = getConfirmationAction();
    confirmationAction.setLoginName(email);
    //change the verification token
    confirmationAction.setEmailVerificationToken(emailVerificationToken+"11");
    confirmationAction.execute();

    assertFalse(confirmationAction.getMessages().isEmpty());
    final User verifiedUser = getRegistrationService().getUserWithLoginName(email);
    assertFalse(verifiedUser.isVerified());
  }

  public void testShouldGiveErrorMessageAsUserIsAlreadyVerified() throws Exception {
    final String email = "viru-verifying-again@home.com";
    final String password = "virupasswd";

    createUser(email, password);
    final User beforeVerificationUser = getRegistrationService().getUserWithLoginName(email);
    assertFalse(beforeVerificationUser.isVerified());
    final String emailVerificationToken = beforeVerificationUser.getEmailVerificationToken();

    assertNotNull(emailVerificationToken);
    assertTrue(emailVerificationToken.length() > 0);

    final ConfirmationAction confirmationAction = getConfirmationAction();
    confirmationAction.setLoginName(email);
    confirmationAction.setEmailVerificationToken(emailVerificationToken);
    confirmationAction.execute();

    //try to verify the email address again
    confirmationAction.execute();

    assertFalse(confirmationAction.getMessages().isEmpty());
    final User verifiedUser = getRegistrationService().getUserWithLoginName(email);
    assertTrue(verifiedUser.isVerified());
  }

  private void createUser(String email, String password) throws Exception {
    final RegisterAction registerAction = getRegistrationAction();
    registerAction.setLoginName1(email);
    registerAction.setLoginName2(email);
    registerAction.setPassword1(password);
    registerAction.setPassword2(password);
    registerAction.execute();
  }

  public void testShouldFailToAcceptForgotPasswordEmailAsItIsNotRegistered() throws Exception {
    final String email = "viru-forgot-password-not-registered@home.com";

    final ForgotPasswordAction forgotPasswordAction = getForgotPasswordAction();
    forgotPasswordAction.setLoginName(email);
    forgotPasswordAction.execute();
    assertFalse(forgotPasswordAction.getMessages().isEmpty());
  }

  public void testShouldSendEmailForForgotPasswordEmailEvenIfTheEmailItIsNotVerified() throws Exception {
    final String email = "viru-forgot-password-not-verified-yet@home.com";

    createUser(email, "virupasswd");
    final User beforeVerificationUser = getRegistrationService().getUserWithLoginName(email);
    assertFalse(beforeVerificationUser.isVerified());

    final ForgotPasswordAction forgotPasswordAction = getForgotPasswordAction();
    forgotPasswordAction.setLoginName(email);
    forgotPasswordAction.execute();
    assertTrue(forgotPasswordAction.getMessages().isEmpty());
  }

  public void testShouldAcceptForgotPasswordRequestIfItIsNotActive() throws Exception {
    final String email = "viru-forgot-password-not-active-yet@home.com";
    createUser(email, "virupasswd");
    final User beforeVerificationUser = getRegistrationService().getUserWithLoginName(email);
    assertFalse(beforeVerificationUser.isActive());

    final ForgotPasswordAction forgotPasswordAction = getForgotPasswordAction();
    forgotPasswordAction.setLoginName(email);
    forgotPasswordAction.execute();
    assertTrue(forgotPasswordAction.getMessages().isEmpty());
  }

  public void testShouldSendEmailForForgotPasswordEmailIfTheEmailIsVerifiedAndActive() throws Exception {
    final String email = "viru-forgot-password-verified-and-active@home.com";
    createUser(email, "virupasswd");

    final User beforeVerificationUser = getRegistrationService().getUserWithLoginName(email);

    final ConfirmationAction confirmationAction = getConfirmationAction();
    confirmationAction.setLoginName(email);
    confirmationAction.setEmailVerificationToken(beforeVerificationUser.getEmailVerificationToken());
    confirmationAction.execute();
    assertTrue(confirmationAction.getMessages().isEmpty());

    final ForgotPasswordAction forgotPasswordAction = getForgotPasswordAction();
    forgotPasswordAction.setLoginName(email);
    forgotPasswordAction.execute();
    assertTrue(forgotPasswordAction.getMessages().isEmpty());
  }

  public void testShouldSendFailToVerifyForgotPasswordTokenIfItIsWrong() throws Exception {
    final String email = "viru-forgot-password-verified-and-active-number2@home.com";
    createUser(email, "virupasswd");

    final User beforeVerificationUser = getRegistrationService().getUserWithLoginName(email);

    final ConfirmationAction confirmationAction = getConfirmationAction();
    confirmationAction.setLoginName(email);
    confirmationAction.setEmailVerificationToken(beforeVerificationUser.getEmailVerificationToken());
    confirmationAction.execute();
    assertTrue(confirmationAction.getMessages().isEmpty());

    final ForgotPasswordAction forgotPasswordAction = getForgotPasswordAction();
    forgotPasswordAction.setLoginName(email);
    forgotPasswordAction.execute();
    assertTrue(forgotPasswordAction.getMessages().isEmpty());


    final User forgotPasswordUser = getRegistrationService().getUserWithLoginName(email);
    assertNotNull(forgotPasswordUser.getResetPasswordToken());
    assertTrue(forgotPasswordUser.getResetPasswordToken().length() > 0);
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
