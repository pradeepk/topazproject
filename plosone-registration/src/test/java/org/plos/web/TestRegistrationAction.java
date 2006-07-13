package org.plos.web;

import junit.framework.TestCase;
import org.plos.service.RegistrationService;
import org.plos.service.ServiceFactory;
import org.plos.User;
import org.plos.web.ConfirmationAction;

/**
 */
public class TestRegistrationAction extends TestCase {
  private ServiceFactory serviceFactory;
  private RegistrationService registrationService;

  protected void setUp() throws Exception {
    serviceFactory = new ServiceFactory();
    registrationService = serviceFactory.getRegistrationService();
  }

  public void testShouldSetUserAsVerified() throws Exception {
    final String emailAddress = "viru-verifying@home.com";
    final User beforeVerificationUser = registrationService.createUser(emailAddress, "virupasswd");

    assertFalse(beforeVerificationUser.isVerified());
    final String emailVerificationToken = beforeVerificationUser.getEmailVerificationToken();

    assertNotNull(emailVerificationToken);
    assertTrue(emailVerificationToken.length() > 0);

    final ConfirmationAction confirmationAction = new ConfirmationAction();
    confirmationAction.setServiceFactory(new ServiceFactory());
    confirmationAction.setEmail(emailAddress);
    confirmationAction.setEmailVerificationToken(emailVerificationToken);
    confirmationAction.execute();

    assertTrue(confirmationAction.getMessages().isEmpty());
    final User verifiedUser = registrationService.getUser(emailAddress);
    assertTrue(verifiedUser.isVerified());
  }

  public void testShouldNotVerifyUser() throws Exception {
    final String emailAddress = "viru-verifying-another-time@home.com";
    final User beforeVerificationUser = registrationService.createUser(emailAddress, "virupasswd");

    assertFalse(beforeVerificationUser.isVerified());
    final String emailVerificationToken = beforeVerificationUser.getEmailVerificationToken();

    assertNotNull(emailVerificationToken);
    assertTrue(emailVerificationToken.length() > 0);

    final ConfirmationAction confirmationAction = new ConfirmationAction();
    confirmationAction.setServiceFactory(new ServiceFactory());
    confirmationAction.setEmail(emailAddress);
    //change the verification token
    confirmationAction.setEmailVerificationToken(emailVerificationToken+"11");
    confirmationAction.execute();

    assertFalse(confirmationAction.getMessages().isEmpty());
    final User verifiedUser = registrationService.getUser(emailAddress);
    assertFalse(verifiedUser.isVerified());
  }

  public void testShouldGiveErrorMessageAsUserIsAlreadyVerified() throws Exception {
    final String emailAddress = "viru-verifying-again@home.com";
    final String password = "virupasswd";

    createUser(emailAddress, password);
    final User beforeVerificationUser = registrationService.getUser(emailAddress);
    assertFalse(beforeVerificationUser.isVerified());
    final String emailVerificationToken = beforeVerificationUser.getEmailVerificationToken();

    assertNotNull(emailVerificationToken);
    assertTrue(emailVerificationToken.length() > 0);

    final ConfirmationAction confirmationAction = new ConfirmationAction();
    confirmationAction.setServiceFactory(serviceFactory);
    confirmationAction.setEmail(emailAddress);
    confirmationAction.setEmailVerificationToken(emailVerificationToken);
    confirmationAction.execute();

    //try to verify the email address again
    confirmationAction.execute();

    assertFalse(confirmationAction.getMessages().isEmpty());
    final User verifiedUser = registrationService.getUser(emailAddress);
    assertTrue(verifiedUser.isVerified());
  }

  private void createUser(String emailAddress, String password) throws Exception {
    final RegisterAction registerAction = new RegisterAction();

    registerAction.setServiceFactory(serviceFactory);
    registerAction.setEmail1(emailAddress);
    registerAction.setEmail2(emailAddress);
    registerAction.setPassword1(password);
    registerAction.setPassword2(password);
    registerAction.execute();
  }

  public void testShouldFailToAcceptForgotPasswordEmailAsItIsNotRegistered() throws Exception {
    final String emailAddress = "viru-forgot-password-not-registered@home.com";

    final ForgotPasswordAction forgotPasswordAction = new ForgotPasswordAction();
    forgotPasswordAction.setServiceFactory(serviceFactory);
    forgotPasswordAction.setEmail(emailAddress);
    forgotPasswordAction.execute();
    assertFalse(forgotPasswordAction.getMessages().isEmpty());
  }

  public void testShouldSendEmailForForgotPasswordEmailEvenIfTheEmailItIsNotVerified() throws Exception {
    final String emailAddress = "viru-forgot-password-not-verified-yet@home.com";

    createUser(emailAddress, "virupasswd");
    final User beforeVerificationUser = registrationService.getUser(emailAddress);
    assertFalse(beforeVerificationUser.isVerified());

    final ForgotPasswordAction forgotPasswordAction = new ForgotPasswordAction();
    forgotPasswordAction.setServiceFactory(serviceFactory);
    forgotPasswordAction.setEmail(emailAddress);
    forgotPasswordAction.execute();
    assertTrue(forgotPasswordAction.getMessages().isEmpty());
  }

  public void testShouldAcceptForgotPasswordRequestIfItIsNotActive() throws Exception {
    final String emailAddress = "viru-forgot-password-not-active-yet@home.com";
    createUser(emailAddress, "virupasswd");
    final User beforeVerificationUser = registrationService.getUser(emailAddress);
    assertFalse(beforeVerificationUser.isActive());

    final ForgotPasswordAction forgotPasswordAction = new ForgotPasswordAction();
    forgotPasswordAction.setServiceFactory(serviceFactory);
    forgotPasswordAction.setEmail(emailAddress);
    forgotPasswordAction.execute();
    assertTrue(forgotPasswordAction.getMessages().isEmpty());
  }

  public void testShouldSendEmailForForgotPasswordEmailIfTheEmailIsVerifiedAndActive() throws Exception {
    final String emailAddress = "viru-forgot-password-verified-and-active@home.com";
    createUser(emailAddress, "virupasswd");

    final User beforeVerificationUser = registrationService.getUser(emailAddress);

    final ConfirmationAction confirmationAction = new ConfirmationAction();
    confirmationAction.setServiceFactory(serviceFactory);
    confirmationAction.setEmail(emailAddress);
    confirmationAction.setEmailVerificationToken(beforeVerificationUser.getEmailVerificationToken());
    confirmationAction.execute();
    assertTrue(confirmationAction.getMessages().isEmpty());

    final ForgotPasswordAction forgotPasswordAction = new ForgotPasswordAction();
    forgotPasswordAction.setServiceFactory(serviceFactory);
    forgotPasswordAction.setEmail(emailAddress);
    forgotPasswordAction.execute();
    assertTrue(forgotPasswordAction.getMessages().isEmpty());
  }

  public void testShouldSendFailToVerifyForgotPasswordTokenIfItIsWrong() throws Exception {
    final String emailAddress = "viru-forgot-password-verified-and-active@home.com";
    createUser(emailAddress, "virupasswd");

    final User beforeVerificationUser = registrationService.getUser(emailAddress);

    final ConfirmationAction confirmationAction = new ConfirmationAction();
    confirmationAction.setServiceFactory(serviceFactory);
    confirmationAction.setEmail(emailAddress);
    confirmationAction.setEmailVerificationToken(beforeVerificationUser.getEmailVerificationToken());
    confirmationAction.execute();
    assertTrue(confirmationAction.getMessages().isEmpty());

    final ForgotPasswordAction forgotPasswordAction = new ForgotPasswordAction();
    forgotPasswordAction.setServiceFactory(serviceFactory);
    forgotPasswordAction.setEmail(emailAddress);
    forgotPasswordAction.execute();
    assertTrue(forgotPasswordAction.getMessages().isEmpty());


    final User forgotPasswordUser = registrationService.getUser(emailAddress);
    assertNotNull(forgotPasswordUser.getResetPasswordToken());
    assertTrue(forgotPasswordUser.getResetPasswordToken().length() > 0);




  }


}
