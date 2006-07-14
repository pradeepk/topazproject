package org.plos.web;

import com.opensymphony.xwork.ActionSupport;
import com.opensymphony.xwork.validator.annotations.EmailValidator;
import com.opensymphony.xwork.validator.annotations.FieldExpressionValidator;
import com.opensymphony.xwork.validator.annotations.RequiredStringValidator;
import com.opensymphony.xwork.validator.annotations.ValidatorType;
import org.plos.ApplicationException;
import org.plos.registration.User;
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
      getServiceFactory()
              .getRegistrationService()
              .changePassword(loginName, password1, resetPasswordToken);
    } catch (final ApplicationException e) {
      addFieldError("password1",  e.getMessage());
      return ERROR;
    }
    return SUCCESS;
  }

  /**
   * Validate the reset password request.
   * @return {@link #SUCCESS} if request is valid, else {@link #ERROR}
   * @throws Exception
   */
  public String validateResetPasswordRequest() throws Exception {
    try {
      final User user
              = getServiceFactory()
                  .getRegistrationService()
                  .getUserWithResetPasswordToken(loginName, resetPasswordToken);
      if (null == user) {
        messages.add("No user found for the given email address and password token combination");
      }
    } catch (final ApplicationException e) {
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

  @EmailValidator(type= ValidatorType.SIMPLE, fieldName="loginName", message="Not a valid email address")
  @RequiredStringValidator(type=ValidatorType.FIELD, fieldName="loginName", message="Email address not specified")
  public void setLoginName(final String loginName) {
    this.loginName = loginName;
  }

  /**
   * @return login name
   */
  public String getLoginName() {
    return loginName;
  }

  /**
   * @return the reset password token
   */
  public String getResetPasswordToken() {
    return resetPasswordToken;
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

  public void setPassword1(final String password1) {
    this.password1 = password1;
  }

  public String getPassword2() {
    return password2;
  }

  public void setPassword2(final String password2) {
    this.password2 = password2;
  }

  public Collection<String> getMessages() {
    return messages;
  }
}
