package org.plos.web;

import org.plos.BasePlosoneRegistrationTestCase;
import org.plos.registration.User;

public class TestConfirmationAction extends BasePlosoneRegistrationTestCase {
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

  public void testShouldNotVerifyUserAsVerificationTokenIsInvalid() throws Exception {
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

  public void testVerifyUserShouldFailAsLoginNameDoesNotExist() throws Exception {
    final String email = "viru-verifying-a-loginnamethatdoes-notexist@home.com";

    final User user = getRegistrationService().getUserWithLoginName(email);
    assertNull(user);

    final ConfirmationAction confirmationAction = getConfirmationAction();
    confirmationAction.setLoginName(email);
    confirmationAction.setEmailVerificationToken("emailVerificationToken");
    confirmationAction.execute();

    assertFalse(confirmationAction.getMessages().isEmpty());
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

}
