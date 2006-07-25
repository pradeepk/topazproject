/* $HeadURL::                                                                            $
 * $Id$
 */
package org.plos.web;

import com.opensymphony.xwork.ActionSupport;
import com.opensymphony.xwork.validator.annotations.EmailValidator;
import com.opensymphony.xwork.validator.annotations.FieldExpressionValidator;
import com.opensymphony.xwork.validator.annotations.RequiredStringValidator;
import com.opensymphony.xwork.validator.annotations.StringLengthFieldValidator;
import com.opensymphony.xwork.validator.annotations.ValidatorType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.ApplicationException;
import org.plos.service.NoUserFoundWithGivenLoginNameException;
import org.plos.service.RegistrationService;
import org.plos.service.PasswordInvalidException;
import org.plos.service.UserNotVerifiedException;

import java.util.ArrayList;

/**
 * Change the password action.
 */
public class ChangePasswordAction extends ActionSupport {
  private String loginName;
  private String oldPassword;
  private String newPassword1;
  private String newPassword2;

  private ArrayList<String> messages = new ArrayList<String>();
  private RegistrationService registrationService;
  private static final Log log = LogFactory.getLog(ChangePasswordAction.class);

  public String execute() throws Exception {
    try {
      registrationService.changePassword(loginName, oldPassword, newPassword1);

    } catch (final NoUserFoundWithGivenLoginNameException e) {
      final String message = "No user found with given email:"+ loginName;
      addFieldError("loginName", message);
      log.trace(message, e);
      return ERROR;
    } catch (final PasswordInvalidException e) {
      final String message = "Invalid password entered";
      addFieldError("oldPassword", message);
      log.trace(message + "for loginName" + loginName, e);
      return ERROR;
    } catch (final UserNotVerifiedException e) {
      final String message = "User not verified:" + loginName;
      addFieldError("loginName", message);
      log.trace(message, e);
      return ERROR;
    } catch (final ApplicationException e) {
      addFieldError("loginName", e.getMessage());
      messages.add(e.getMessage());
      log.error("Application error", e);
      return ERROR;
    }
    return SUCCESS;
  }

  /**
   * @return loginName
   */
  @EmailValidator(type= ValidatorType.SIMPLE, fieldName= "loginName", message="You must enter a valid email")
  @RequiredStringValidator(type=ValidatorType.FIELD, fieldName= "loginName", message="You must enter an email address")
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
   * @return oldPassword
   */
  @RequiredStringValidator(type=ValidatorType.FIELD, fieldName= "oldPassword", message="You must enter your earlier password")
  public String getOldPassword() {
    return oldPassword;
  }

  /**
   * Set oldPassword
   * @param oldPassword oldPassword
   */
  public void setOldPassword(final String oldPassword) {
    this.oldPassword = oldPassword;
  }

  /**
   * @return newPassword1 newPassword1
   */
  @RequiredStringValidator(type=ValidatorType.FIELD, fieldName= "newPassword1", message="You must enter a new password")
  @FieldExpressionValidator(fieldName= "newPassword1", expression= "newPassword1" +"=="+ "newPassword2", message="New passwords must match")
  @StringLengthFieldValidator(fieldName= "newPassword1", maxLength="256", message="Password must be less than 256")
  public String getNewPassword1() {
    return newPassword1;
  }

  /**
   * Set newPassword1
   * @param newPassword1 newPassword1
   */
  public void setNewPassword1(final String newPassword1) {
    this.newPassword1 = newPassword1;
  }

  /**
   * @return newPassword2
   */
  public String getNewPassword2() {
    return newPassword2;
  }

  /**
   * Set newPassword2
   * @param newPassword2 newPassword2
   */
  public void setNewPassword2(final String newPassword2) {
    this.newPassword2 = newPassword2;
  }

  public void setRegistrationService(final RegistrationService registrationService) {
    this.registrationService = registrationService;
  }

  public ArrayList<String> getMessages() {
    return messages;
  }
}
