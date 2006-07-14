package org.plos.web;

import com.opensymphony.xwork.ActionSupport;
import com.opensymphony.xwork.validator.annotations.EmailValidator;
import com.opensymphony.xwork.validator.annotations.FieldExpressionValidator;
import com.opensymphony.xwork.validator.annotations.RequiredStringValidator;
import com.opensymphony.xwork.validator.annotations.ValidatorType;
import org.plos.ApplicationException;
import org.plos.registration.User;
import org.plos.service.RegistrationService;
import org.plos.service.ServiceFactory;

import java.util.ArrayList;
import java.util.Collection;

/**
 * $HeadURL$
 * @version: $Id$
 */
public class ForgotPasswordChangePasswordAction extends ActionSupport {

  private ServiceFactory serviceFactory;
  private String loginName;
  private String resetPasswordToken;
  private String password1;
  private String password2;

  private ArrayList<String> messages = new ArrayList<String>();

  public String execute() throws Exception {
    try {
      final RegistrationService registrationService = getServiceFactory().getRegistrationService();
      final User user = registrationService.getUserWithLoginName(loginName);
      if (user.getResetPasswordToken().equals(resetPasswordToken)) {
        registrationService.changePassword(loginName, password1);
      } else {
        addFieldError("password1",  "Changing password failed");
      }
    } catch (final ApplicationException e) {
      messages.add(e.getMessage());
      addFieldError("password1",  e.getMessage());
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

  @EmailValidator(type= ValidatorType.SIMPLE, fieldName="loginName", message="Not a valid email address")
  @RequiredStringValidator(type=ValidatorType.FIELD, fieldName="loginName", message="Email address not specified")
  public void setLoginName(final String loginName) {
    this.loginName = loginName;
  }

  @RequiredStringValidator(type= ValidatorType.FIELD, fieldName="resetPasswordToken", message="Verification token missing")
  public void setResetPasswordToken(final String resetPasswordToken) {
    this.resetPasswordToken = resetPasswordToken;
  }

  @RequiredStringValidator(type=ValidatorType.FIELD, fieldName="password1", message="You must enter a password")
  @FieldExpressionValidator(fieldName="password2", expression = "password1==password2", message="Passwords must match")
  public String getPassword1() {
    return password1;
  }

  public void setPassword1(String password1) {
    this.password1 = password1;
  }

  public String getPassword2() {
    return password2;
  }

  public void setPassword2(String password2) {
    this.password2 = password2;
  }

  public Collection<String> getMessages() {
    return messages;
  }

}
