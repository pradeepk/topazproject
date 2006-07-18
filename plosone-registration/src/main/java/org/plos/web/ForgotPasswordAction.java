package org.plos.web;

import com.opensymphony.xwork.ActionSupport;
import com.opensymphony.xwork.validator.annotations.EmailValidator;
import com.opensymphony.xwork.validator.annotations.RequiredStringValidator;
import com.opensymphony.xwork.validator.annotations.ValidatorType;
import org.plos.ApplicationException;
import org.plos.registration.User;
import org.plos.service.NoUserFoundWithGivenLoginNameException;
import org.plos.service.RegistrationService;

import java.util.ArrayList;
import java.util.Collection;

/**
 * $HeadURL$
 * @version: $Id$
 */
public class ForgotPasswordAction extends ActionSupport {

  private RegistrationService registrationService;
  private ArrayList<String> messages = new ArrayList<String>();
  private String loginName;

  /**
   * @deprecated
   * to be removed when we change the forgot-password-success.jsp so that it does not display the forgot password link
   */
  private User user;

  public String execute() throws Exception {
    try {
      registrationService.sendForgotPasswordMessage(loginName);
      final User user = registrationService.getUserWithLoginName(loginName);
      setUser(user);

    } catch (final NoUserFoundWithGivenLoginNameException noUserEx) {
      final String message = "No user found for the given email address:" + loginName;
      messages.add(message);
      addFieldError("loginName", message);
      return ERROR;
    } catch (final ApplicationException e) {
      messages.add(e.getMessage());
      addFieldError("loginName", e.getMessage());
      return ERROR;
    }
    return SUCCESS;
  }

  private void setUser(final User user) {
    this.user = user;
  }

  @EmailValidator(type = ValidatorType.SIMPLE, fieldName = "loginName", message = "Not a valid loginName")
  @RequiredStringValidator(type = ValidatorType.FIELD, fieldName = "loginName", message = "Email address is required")
  public void setLoginName(final String loginName) {
    this.loginName = loginName;
  }

  public Collection<String> getMessages() {
    return messages;
  }

  public User getUser() {
    return user;
  }

  public void setRegistrationService(final RegistrationService registrationService) {
    this.registrationService = registrationService;
  }
}
