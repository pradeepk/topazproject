/* $HeadURL::                                                                            $
 * $Id$
 *
 */
package org.plos.web;

import com.opensymphony.xwork.ActionSupport;
import com.opensymphony.xwork.validator.annotations.EmailValidator;
import com.opensymphony.xwork.validator.annotations.RequiredStringValidator;
import com.opensymphony.xwork.validator.annotations.ValidatorType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.ApplicationException;
import org.plos.registration.User;
import org.plos.service.NoUserFoundWithGivenLoginNameException;
import org.plos.service.RegistrationService;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Used when a user makes a forgot password request.
 */
public class ForgotPasswordAction extends ActionSupport {

  private RegistrationService registrationService;
  private ArrayList<String> messages = new ArrayList<String>();

  private String loginName;

  private static final Log log = LogFactory.getLog(ForgotPasswordAction.class);

  /**
   * @deprecated
   * to be removed when we change the forgot-password-success.jsp so that it does not display the forgot password link
   */
  private User user;

  public String execute() throws Exception {
    try {
      registrationService.sendForgotPasswordMessage(loginName);

      //TODO to be removed when we change the forgot-password-success.jsp so that it does not display the forgot password link
      {
        final User user = registrationService.getUserWithLoginName(loginName);
        setUser(user);
      }

    } catch (final NoUserFoundWithGivenLoginNameException noUserEx) {
      final String message = "No user found for the given email address:" + loginName;
      messages.add(noUserEx.getMessage());
      log.trace(message, noUserEx);
      addFieldError("loginName", message);
      return ERROR;
    } catch (final ApplicationException e) {
      messages.add(e.getMessage());
      log.error(e, e);
      addFieldError("loginName", e.getMessage());
      return ERROR;
    }
    return SUCCESS;
  }

  /**
   * @return loginName
   */
  @EmailValidator(type=ValidatorType.SIMPLE, fieldName="loginName", message="Not a valid email address")
  @RequiredStringValidator(type=ValidatorType.FIELD, fieldName="loginName", message="Email address is required")
  public String getLoginName() {
    return loginName;
  }

  /**
   * Set loginName
   * @param loginName loginName
   */
  public void setLoginName(final String loginName) {
    this.loginName = loginName;
  }

  /**
   * @return Error or warning messages to be shown to the user.
   */
  public Collection<String> getMessages() {
    return messages;
  }

  /**
   * Set the user.
   * @param user user
   */
  // TODO to be removed when we change the forgot-password-success.jsp so that it does not display the forgot password link
  private void setUser(final User user) {
    this.user = user;
  }

  /**
   * @return user
   */
  // TODO to be removed when we change the forgot-password-success.jsp so that it does not display the forgot password link
  public User getUser() {
    return user;
  }

  /**
   * Set registrationService
   * @param registrationService registrationService
   */
  public void setRegistrationService(final RegistrationService registrationService) {
    this.registrationService = registrationService;
  }
}
