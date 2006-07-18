package org.plos.web;

import com.opensymphony.xwork.ActionSupport;
import com.opensymphony.xwork.validator.annotations.EmailValidator;
import com.opensymphony.xwork.validator.annotations.FieldExpressionValidator;
import com.opensymphony.xwork.validator.annotations.RequiredStringValidator;
import com.opensymphony.xwork.validator.annotations.ValidatorType;
import org.plos.ApplicationException;
import org.plos.registration.User;
import org.plos.service.RegistrationService;
import org.plos.service.UserAlreadyExistsException;

/**
 * $HeadURL$
 * @version: $Id$
 */
public class RegisterAction extends ActionSupport {

  private String loginName1;
  private String loginName2;
  private String password1;
  private String password2;

  private RegistrationService registrationService;

  /**
   * @deprecated remove when we drop the link from the web page
   */
  private User user;

  public String execute() throws Exception {

    try {
      final User user
              = registrationService.createUser(loginName1, password1);

      sendMessage(user);

    } catch (final UserAlreadyExistsException e) {
      addFieldError("loginName1", "User already exists for the given email address");
      return ERROR;
    } catch (final ApplicationException e) {
      addFieldError("loginName1", e.getMessage());
      return ERROR;
    }
    return SUCCESS;

  }

  private void sendMessage(final User user) {
    this.user = user;
//    user.getLoginName(),
//    user.getEmailVerificationToken());
  }

  @EmailValidator(type=ValidatorType.SIMPLE, fieldName="loginName1", message="You must enter a valid email")
  @RequiredStringValidator(type=ValidatorType.FIELD, fieldName="loginName1", message="You must enter an email address")
  @FieldExpressionValidator(fieldName="loginName2", expression = "loginName1==loginName2", message="Email addresses must match")
  public String getLoginName1() {
    return loginName1;
  }

  public void setLoginName1(final String loginName1) {
    this.loginName1 = loginName1;
  }

  public String getLoginName2() {
    return loginName2;
  }

  public void setLoginName2(String loginName2) {
    this.loginName2 = loginName2;
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

  public User getUser() {
    return user;
  }

  public void setRegistrationService(final RegistrationService registrationService) {
    this.registrationService = registrationService;
  }
}
