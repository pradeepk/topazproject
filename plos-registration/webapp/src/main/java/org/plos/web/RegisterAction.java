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
import com.opensymphony.xwork.validator.annotations.StringLengthFieldValidator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.ApplicationException;
import org.plos.registration.User;
import org.plos.service.RegistrationService;
import org.plos.service.UserAlreadyExistsException;

/**
 * Uses use this to register as a new user. Verification stage is separate from this.
 */
public class RegisterAction extends ActionSupport {

  private String loginName1;
  private String loginName2;
  private String password1;
  private String password2;

  private RegistrationService registrationService;
  private static final Log log = LogFactory.getLog(RegisterAction.class);

  /**
   * @deprecated remove when we drop the link from the web page
   */
  private User user;

  public String execute() throws Exception {

    try {
      final User user
              = registrationService.createUser(loginName1, password1);

      //TODO remove when we drop the link from the web page 
      {
        setUser(user);
      }

    } catch (final UserAlreadyExistsException e) {
      log.debug("UserAlreadyExists:"+loginName1, e);
      addFieldError("loginName1", "User already exists for the given email address");
      return ERROR;
    } catch (final ApplicationException e) {
      log.error("Application error", e);
      addFieldError("loginName1", e.getMessage());
      return ERROR;
    }
    return SUCCESS;

  }

  /**
   * @deprecated The web pages should not need this after the email messaging service is integrated.
   * @param user user
   */
  private void setUser(final User user) {
    this.user = user;
  }

  /**
   * @return loginName1.
   */
  @EmailValidator(message="You must enter a valid email")
  @RequiredStringValidator(message="You must enter an email address")
  @FieldExpressionValidator(fieldName="loginName2", expression = "loginName1==loginName2", message="Email addresses must match")
  @StringLengthFieldValidator(maxLength = "256", message="Email must be less than 256")
  public String getLoginName1() {
    return loginName1;
  }

  /**
   * Set loginName1
   * @param loginName1 loginName1
   */
  public void setLoginName1(final String loginName1) {
    this.loginName1 = loginName1;
  }

  /**
   * @return loginName2.
   */
  public String getLoginName2() {
    return loginName2;
  }

  /**
   * Set loginName2
   * @param loginName2 loginName2
   */
  public void setLoginName2(String loginName2) {
    this.loginName2 = loginName2;
  }

  /**
   * Get password1
   * @return password1
   */
  @RequiredStringValidator(message="You must enter a password")
  @FieldExpressionValidator(fieldName="password2", expression = "password1==password2", message="Passwords must match")
  @StringLengthFieldValidator(maxLength = "256", message="Password must be less than 256")
  public String getPassword1() {
    return password1;
  }

  /**
   * Set password1
   * @param password1 password1
   */
  public void setPassword1(String password1) {
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
  public void setPassword2(String password2) {
    this.password2 = password2;
  }

  /**
   * @deprecated The web pages should not need this after the email messaging service is integrated.
   * @return User
   */
  public User getUser() {
    return user;
  }

  /**
   * Set the registrationService.
   * @param registrationService registrationService
   */
  public void setRegistrationService(final RegistrationService registrationService) {
    this.registrationService = registrationService;
  }
}
