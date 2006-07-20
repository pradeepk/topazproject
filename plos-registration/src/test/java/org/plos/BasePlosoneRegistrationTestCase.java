package org.plos;

import org.plos.service.RegistrationService;
import org.plos.web.ConfirmationAction;
import org.plos.web.ForgotPasswordAction;
import org.plos.web.RegisterAction;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

/**
 * Base test case for the registration unit tests. It provides spring injection from one of its superclasses.
 *
 * $HeadURL: $
 * @version: $Id: $
 */
public abstract class BasePlosoneRegistrationTestCase extends AbstractDependencyInjectionSpringContextTests {
  protected RegistrationService registrationService;
  private ConfirmationAction confirmationAction;
  private RegisterAction registerAction;

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

}
