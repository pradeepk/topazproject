/* $HeadURL::                                                                            $
 * $Id$
 *
 */
package org.plos;

import org.plos.service.RegistrationService;
import org.plos.web.ConfirmationAction;
import org.plos.web.ForgotPasswordAction;
import org.plos.web.RegisterAction;
import org.plos.util.PasswordDigestService;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

/**
 * Base test case for the registration unit tests. It provides spring injection from one of its superclasses.
 *
 */
public abstract class BasePlosoneRegistrationTestCase extends AbstractDependencyInjectionSpringContextTests {
  protected RegistrationService registrationService;
  private ConfirmationAction confirmationAction;
  private RegisterAction registerAction;
  private PasswordDigestService passwordDigestService;

  protected String[] getConfigLocations() {
    return new String[] {"nonJmxApplicationContext.xml"};
  }

  public final void setRegistrationService(final RegistrationService registrationService) {
    this.registrationService = registrationService;
  }

  protected ConfirmationAction getConfirmationAction() {
    return confirmationAction;
  }

  public void setConfirmationAction(final ConfirmationAction confirmationAction) {
    this.confirmationAction = confirmationAction;
  }

  public RegistrationService getRegistrationService() {
    return registrationService;
  }

  protected RegisterAction getRegistrationAction() {
    return registerAction;
  }

  public void setRegisterAction(final RegisterAction registerAction) {
    this.registerAction = registerAction;
  }

  protected ForgotPasswordAction getForgotPasswordAction() {
    final ForgotPasswordAction forgotPasswordAction = new ForgotPasswordAction();
    forgotPasswordAction.setRegistrationService(getRegistrationService());
    return forgotPasswordAction;
  }

  public void setPasswordDigestService(final PasswordDigestService passwordDigestService) {
    this.passwordDigestService = passwordDigestService;
  }

  protected PasswordDigestService getPasswordDigestService() {
    return passwordDigestService;
  }

  protected final void createUser(final String email, final String password) throws Exception {
    final RegisterAction registerAction = getRegistrationAction();
    registerAction.setLoginName1(email);
    registerAction.setLoginName2(email);
    registerAction.setPassword1(password);
    registerAction.setPassword2(password);
    registerAction.execute();
  }
}
