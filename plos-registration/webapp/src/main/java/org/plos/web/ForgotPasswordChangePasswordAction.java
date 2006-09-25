/* $HeadURL::                                                                            $
 * $Id$
 *
 */
package org.plos.web;

import com.opensymphony.xwork.ActionSupport;
import com.opensymphony.xwork.validator.annotations.EmailValidator;
import com.opensymphony.xwork.validator.annotations.FieldExpressionValidator;
import com.opensymphony.xwork.validator.annotations.RequiredStringValidator;
import com.opensymphony.xwork.validator.annotations.ValidatorType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.ApplicationException;
import org.plos.service.RegistrationService;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Used to present the user with reset password facility after having forgotten their password.
 */
public class ForgotPasswordChangePasswordAction extends ActionSupport {

  private String loginName;
  private String resetPasswordToken;
  private String password1;
  private String password2;

  private ArrayList<String> messages = new ArrayList<String>();
  private RegistrationService registrationService;

  private static final Log log = LogFactory.getLog(ForgotPasswordChangePasswordAction.class);

  public String execute() throws Exception {
    try {
      registrationService
              .resetPassword(loginName, resetPasswordToken, password1);
    } catch (final ApplicationException e) {
      log.warn("Error changing password", e);
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
      registrationService.getUserWithResetPasswordToken(loginName, resetPasswordToken);
    } catch (final ApplicationException e) {
      log.warn("Error validating password request", e);
      addFieldError("password1",  e.getMessage());
      return ERROR;
    }
    return SUCCESS;
  }

  /**
   * @return loginName
   */
  @EmailValidator(type= ValidatorType.SIMPLE, fieldName="loginName", message="Not a valid email address")
  @RequiredStringValidator(type=ValidatorType.FIELD, fieldName="loginName", message="Email address not specified")
  public String getLoginName() {
    return loginName;
  }

  /**
   * Set loginName.
   * @param loginName loginName
   */
  public void setLoginName(final String loginName) {
    this.loginName = loginName;
  }

  /**
   * @return the reset password token
   */
  @RequiredStringValidator(type= ValidatorType.FIELD, fieldName="resetPasswordToken", message="Verification token missing")
  public String getResetPasswordToken() {
    return resetPasswordToken;
  }

  /**
   * Set resetPasswordToken.
   * @param resetPasswordToken token used to verify the reset the password request.
   */
  public void setResetPasswordToken(final String resetPasswordToken) {
    this.resetPasswordToken = resetPasswordToken;
  }

  /**
   * @return password1
   */
  @RequiredStringValidator(type=ValidatorType.FIELD, fieldName="password1", message="You must enter a password")
  @FieldExpressionValidator(fieldName="password2", expression = "password1==password2", message="Passwords must match")
  public String getPassword1() {
    return password1;
  }

  /**
   * Set password1
   * @param password1 password1
   */
  public void setPassword1(final String password1) {
    this.password1 = password1;
  }

  /**
   * @return password2
   */
  public String getPassword2() {
    return password2;
  }

  /**
   * Set password2
   * @param password2 password2
   */
  public void setPassword2(final String password2) {
    this.password2 = password2;
  }

  /**
   * @return Error or warning messages to be shown to the user.
   */
  public Collection<String> getMessages() {
    return messages;
  }

  /**
   * Set registrationService
   * @param registrationService registrationService
   */
  public void setRegistrationService(final RegistrationService registrationService) {
    this.registrationService = registrationService;
  }
}
