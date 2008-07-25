/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.topazproject.ambra.web;

import com.opensymphony.xwork2.validator.annotations.RegexFieldValidator;
import com.opensymphony.xwork2.validator.annotations.FieldExpressionValidator;
import com.opensymphony.xwork2.validator.annotations.RequiredStringValidator;
import com.opensymphony.xwork2.validator.annotations.StringLengthFieldValidator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.topazproject.ambra.ApplicationException;
import org.topazproject.ambra.registration.User;
import org.topazproject.ambra.service.RegistrationMailer;
import org.topazproject.ambra.service.RegistrationService;
import org.topazproject.ambra.service.UserAlreadyExistsException;

import static org.topazproject.ambra.Constants.Length.PASSWORD_MAX;
import static org.topazproject.ambra.Constants.Length.PASSWORD_MIN;

/**
 * Uses use this to register as a new user. Verification stage is separate from this.
 */
public class RegisterAction extends BaseAction {
  private String loginName1;
  private String loginName2;
  private String password1;
  private String password2;

  private RegistrationMailer registrationVerificationMailer;
  private RegistrationService registrationService;
  private static final Log log = LogFactory.getLog(RegisterAction.class);

  public String execute() throws Exception {
    try {
      final User user = registrationService.createUser(loginName1, password1);
    } catch (final UserAlreadyExistsException e) {
      log.debug("UserAlreadyExists:"+loginName1, e);
      addFieldError("loginName1", "User already exists for the given e-mail address");
      return INPUT;
    } catch (final ApplicationException e) {
      log.error("Application error", e);
      addFieldError("loginName1", e.getMessage());
      return ERROR;
    }
    return SUCCESS;

  }

  /**
   * @return loginName1.
   */
  @RegexFieldValidator(message = "You must enter a valid e-mail", expression = EMAIL_REGEX)
  @RequiredStringValidator(message="You must enter an e-mail address")
  @FieldExpressionValidator(fieldName="loginName2", expression = "loginName1==loginName2",
      message="Email addresses must match")
  @StringLengthFieldValidator(maxLength = "256", message="E-mail must be less than 256")
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
  @RequiredStringValidator(message="You must enter a password", shortCircuit=true)
  @FieldExpressionValidator(fieldName="password2", expression = "password1==password2",
      message="Passwords must match", shortCircuit=true)
  @StringLengthFieldValidator(minLength= PASSWORD_MIN, maxLength = PASSWORD_MAX,
      message="Password length must be between " + PASSWORD_MIN + " and " + PASSWORD_MAX, shortCircuit=true)
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
   * Set the registrationService.
   * @param registrationService registrationService
   */
  public void setRegistrationService(final RegistrationService registrationService) {
    this.registrationService = registrationService;
  }

  /**
   * @return Returns the registrationMailer.
   */
  public RegistrationMailer getRegistrationVerificationMailer() {
    return registrationVerificationMailer;
  }

  /**
   * @param registrationMailer The registrationMailer to set.
   */
  public void setRegistrationVerificationMailer(RegistrationMailer registrationMailer) {
    this.registrationVerificationMailer = registrationMailer;
  }
}
