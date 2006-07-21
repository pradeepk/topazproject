/* $HeadURL::                                                                            $
 * $Id$
 *
 */
package org.plos.web;

import com.opensymphony.xwork.ActionSupport;
import com.opensymphony.xwork.validator.annotations.EmailValidator;
import com.opensymphony.xwork.validator.annotations.ValidatorType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.ApplicationException;
import org.plos.service.RegistrationService;
import org.plos.service.UserAlreadyVerifiedException;
import org.plos.service.VerificationTokenInvalidException;

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
      final String message = "UserAlreadyVerified:" + loginName;
      messages.add(message);
      log.trace(message, e);
      return ERROR;
    } catch (final VerificationTokenInvalidException e) {
      final String message = "VerificationTokenInvalid:"+ emailVerificationToken+", loginName:" + loginName;
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
   * Set emailVerificationToken
   * @param emailVerificationToken emailVerificationToken
   */
//  @RequiredStringValidator(type=ValidatorType.FIELD, fieldName="emailVerificationToken", message="Verification token missing")
  public void setEmailVerificationToken(final String emailVerificationToken) {
    this.emailVerificationToken = emailVerificationToken;
  }

  /**
   * Set loginName
   * @param loginName loginName
   */
  @EmailValidator(type= ValidatorType.SIMPLE, fieldName="loginName", message="Not a valid email address")
//  @RequiredStringValidator(type=ValidatorType.FIELD, fieldName="loginName", message="Email address not specified")
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
