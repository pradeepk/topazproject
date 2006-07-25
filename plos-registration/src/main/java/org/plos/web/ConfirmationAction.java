/* $HeadURL::                                                                            $
 * $Id$
 *
 */
package org.plos.web;

import com.opensymphony.xwork.ActionSupport;
import com.opensymphony.xwork.validator.annotations.EmailValidator;
import com.opensymphony.xwork.validator.annotations.ValidatorType;
import com.opensymphony.xwork.validator.annotations.RequiredStringValidator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.ApplicationException;
import org.plos.service.RegistrationService;
import org.plos.service.UserAlreadyVerifiedException;
import org.plos.service.VerificationTokenInvalidException;
import org.plos.service.NoUserFoundWithGivenLoginNameException;

import java.util.ArrayList;
import java.util.Collection;

/**
 *
 */
public class ConfirmationAction extends ActionSupport {
  private String emailVerificationToken;
  private ArrayList<String> messages = new ArrayList<String>();
  private String loginName;
  private RegistrationService registrationService;

  private static final Log log = LogFactory.getLog(ConfirmationAction.class);

  public String execute() throws Exception {

    try {
      registrationService
              .verifyUser(loginName, emailVerificationToken);
    } catch (final UserAlreadyVerifiedException e) {
      final String message = "User already verified:" + loginName;
      messages.add(message);
      log.trace(message, e);
      return ERROR;
    } catch (final VerificationTokenInvalidException e) {
      final String message = "Verification token invalid:"+ emailVerificationToken+", email:" + loginName;
      messages.add(message);
      log.trace(message, e);
      return ERROR;
    } catch (final NoUserFoundWithGivenLoginNameException e) {
      final String message = "No user found with given email:"+ emailVerificationToken+", email:" + loginName;
      messages.add(message);
      log.trace(message, e);
      return ERROR;
    } catch (final ApplicationException e) {
      messages.add(e.getMessage());
      addFieldError("loginName", e.getMessage());
      log.warn(e, e);
      return ERROR;
    }
    return SUCCESS;
  }

  /**
   * Set registrationService
   * @param registrationService registrationService
   */
  public void setRegistrationService(final RegistrationService registrationService) {
    this.registrationService = registrationService;
  }

  /**
   * @return emailVerificationToken
   */
  @RequiredStringValidator(type=ValidatorType.FIELD, fieldName="emailVerificationToken", message="Verification token missing")
  public String getEmailVerificationToken() {
    return emailVerificationToken;
  }

  /**
   * Set emailVerificationToken
   * @param emailVerificationToken emailVerificationToken
   */
  public void setEmailVerificationToken(final String emailVerificationToken) {
    this.emailVerificationToken = emailVerificationToken;
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
}
