package org.plos.web;

import com.opensymphony.xwork.ActionSupport;
import com.opensymphony.xwork.validator.annotations.EmailValidator;
import com.opensymphony.xwork.validator.annotations.ValidatorType;
import org.plos.ApplicationException;
import org.plos.service.ServiceFactory;

import java.util.ArrayList;
import java.util.Collection;

/**
 * $HeadURL$
 * @version: $Id$
 */
public class ConfirmationAction extends ActionSupport {

  private ServiceFactory serviceFactory;
  private String emailVerificationToken;
  private ArrayList<String> messages = new ArrayList<String>();
  private String loginName;

  public String execute() throws Exception {

    try {
      getServiceFactory()
              .getRegistrationService()
              .verifyUser(loginName, emailVerificationToken);

    } catch (ApplicationException e) {
      messages.add(e.getMessage());
      addFieldError("loginName", e.getMessage());
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

//  @RequiredStringValidator(type=ValidatorType.FIELD, fieldName="emailVerificationToken", message="Verification token missing")
  public void setEmailVerificationToken(final String emailVerificationToken) {
    this.emailVerificationToken = emailVerificationToken;
  }

  @EmailValidator(type= ValidatorType.SIMPLE, fieldName="loginName", message="Not a valid email address")
//  @RequiredStringValidator(type=ValidatorType.FIELD, fieldName="loginName", message="Email address not specified")
  public void setLoginName(final String loginName) {
    this.loginName = loginName;
  }

  public Collection<String> getMessages() {
    return messages;
  }
}
