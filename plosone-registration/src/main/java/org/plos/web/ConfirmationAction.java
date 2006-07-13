package org.plos.web;

import com.opensymphony.xwork.ActionSupport;
import com.opensymphony.xwork.validator.annotations.EmailValidator;
import com.opensymphony.xwork.validator.annotations.ValidatorType;
import com.opensymphony.xwork.validator.annotations.RequiredStringValidator;
import org.plos.service.ServiceFactory;
import org.plos.ApplicationException;

import java.util.Collection;
import java.util.ArrayList;

/**
 * $HeadURL: $
 * @version: $Id: $
 */
public class ConfirmationAction extends ActionSupport {

  private ServiceFactory serviceFactory;
  private String emailVerificationToken;
  private ArrayList messages = new ArrayList();
  private String emailAddress;

  public String execute() throws Exception {

    try {
      getServiceFactory()
              .getRegistrationService()
              .verifyUser(emailAddress, emailVerificationToken);

    } catch (ApplicationException e) {
      messages.add(e.getMessage());
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

  @RequiredStringValidator(type=ValidatorType.FIELD, fieldName="emailVerificationToken", message="Verification token missing")
  public void setEmailVerificationToken(final String emailVerificationToken) {
    this.emailVerificationToken = emailVerificationToken;
  }

  @EmailValidator(type= ValidatorType.SIMPLE, fieldName="email", message="Not a valid email")
  @RequiredStringValidator(type=ValidatorType.FIELD, fieldName="email1", message="Email address not specified")
  public void setEmail(final String emailAddress) {
    this.emailAddress = emailAddress;
  }

  public Collection getMessages() {
    return messages;
  }
}
