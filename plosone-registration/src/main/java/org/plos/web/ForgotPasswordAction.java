package org.plos.web;

import com.opensymphony.xwork.ActionSupport;
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
  private ArrayList<String> messages = new ArrayList<String>();
  private String loginName;

  public String execute() throws Exception {
    try {
      getServiceFactory()
              .getRegistrationService()
              .sendForgotPasswordMessage(loginName);

    } catch (final ApplicationException e) {
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

//  @EmailValidator(type = ValidatorType.SIMPLE, fieldName = "loginName", message = "Not a valid loginName")
//  @RequiredStringValidator(type = ValidatorType.FIELD, fieldName = "loginName", message = "Email address is required")
  public void setLoginName(final String loginName) {
    this.loginName = loginName;
  }

  public Collection<String> getMessages() {
    return messages;
  }

}
