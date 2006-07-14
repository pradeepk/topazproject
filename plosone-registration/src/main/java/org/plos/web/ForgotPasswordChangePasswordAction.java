package org.plos.web;

import com.opensymphony.xwork.ActionSupport;
import com.opensymphony.xwork.validator.annotations.EmailValidator;
import com.opensymphony.xwork.validator.annotations.RequiredStringValidator;
import com.opensymphony.xwork.validator.annotations.ValidatorType;
import org.plos.ApplicationException;
import org.plos.service.ServiceFactory;

import java.util.ArrayList;
import java.util.Collection;

/**
 * $HeadURL$
 * @version: $Id$
 */
public class ForgotPasswordChangePasswordAction extends ActionSupport {

  private ServiceFactory serviceFactory;
  private String resetPasswordToken;
  private ArrayList<String> messages = new ArrayList<String>();
  private String loginName;

  public String execute() throws Exception {

    try {
      getServiceFactory()
              .getRegistrationService()
              .verifyUser(loginName, resetPasswordToken);

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

  @EmailValidator(type= ValidatorType.SIMPLE, fieldName="loginName", message="Not a valid email address")
  @RequiredStringValidator(type=ValidatorType.FIELD, fieldName="loginName", message="Email address not specified")
  public void setLoginName(final String loginName) {
    this.loginName = loginName;
  }

  public Collection<String> getMessages() {
    return messages;
  }
}
