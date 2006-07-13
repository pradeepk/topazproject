package org.plos.web;

import com.opensymphony.xwork.ActionSupport;
import com.opensymphony.xwork.validator.annotations.RequiredStringValidator;
import com.opensymphony.xwork.validator.annotations.ValidatorType;
import com.opensymphony.xwork.validator.annotations.EmailValidator;
import org.plos.service.ServiceFactory;
import org.plos.ApplicationException;

import java.util.ArrayList;
import java.util.Collection;

/**
 * $HeadURL: $
 * @version: $Id: $
 */
public class ForgotPasswordChangePasswordAction extends ActionSupport {

  private ServiceFactory serviceFactory;
  private String resetPasswordToken;
  private ArrayList messages = new ArrayList();
  private String emailAddress;

  public String execute() throws Exception {

    try {
      getServiceFactory()
              .getRegistrationService()
              .verifyUser(emailAddress, resetPasswordToken);

    } catch (ApplicationException e) {
      messages.add(e.getMessage());
      return ERROR;
    }
    return SUCCESS;
  }

  private ServiceFactory getServiceFactory() {
    return serviceFactory;
  }

  public void setServiceFactory(final ServiceFactory serviceFactory) {
    this.serviceFactory = serviceFactory;
  }

  @RequiredStringValidator(type= ValidatorType.FIELD, fieldName="resetPasswordToken", message="Verification token missing")
  public void setResetPasswordToken(final String resetPasswordToken) {
    this.resetPasswordToken = resetPasswordToken;
  }

  @EmailValidator(type= ValidatorType.SIMPLE, fieldName="email", message="Not a valid email")
  @RequiredStringValidator(type=ValidatorType.FIELD, fieldName="email1", message="Email address not specified")
  public void setEmail(final String emailAddress) {
    this.emailAddress = emailAddress;
  }

  public Collection getMessages() {
    return messages;
  }
}
