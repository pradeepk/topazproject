package org.plos.web;

import com.opensymphony.xwork.ActionSupport;
import com.opensymphony.xwork.validator.annotations.EmailValidator;
import com.opensymphony.xwork.validator.annotations.RequiredStringValidator;
import com.opensymphony.xwork.validator.annotations.ValidatorType;
import org.plos.ApplicationException;
import org.plos.service.ServiceFactory;

import java.util.ArrayList;
import java.util.Collection;

/**
 * $HeadURL$
 * @version: $Id$
 */
public class ForgotPasswordAction extends ActionSupport {

  private ServiceFactory serviceFactory;
  private ArrayList messages = new ArrayList();
  private String emailAddress;

  public String execute() throws Exception {
    try {
      getServiceFactory()
              .getRegistrationService()
              .sendForgotPasswordMessage(emailAddress);

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

  @EmailValidator(type = ValidatorType.SIMPLE, fieldName = "email", message = "Not a valid email")
  @RequiredStringValidator(type = ValidatorType.FIELD, fieldName = "email1", message = "Email address not specified")
  public void setEmail(final String emailAddress) {
    this.emailAddress = emailAddress;
  }

  public Collection getMessages() {
    return messages;
  }

}
