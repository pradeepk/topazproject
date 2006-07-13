package org.plos.web;

import com.opensymphony.xwork.ActionSupport;
import com.opensymphony.xwork.validator.annotations.EmailValidator;
import com.opensymphony.xwork.validator.annotations.FieldExpressionValidator;
import com.opensymphony.xwork.validator.annotations.RequiredStringValidator;
import com.opensymphony.xwork.validator.annotations.ValidatorType;
import org.plos.registration.User;
import org.plos.service.ServiceFactory;

import java.util.ArrayList;
import java.util.Collection;

/**
 * $HeadURL$
 * @version: $Id$
 */
public class RegisterAction extends ActionSupport {

  private String email1;
  private String email2;
  private String password1;
  private String password2;
  private User user;

  private ServiceFactory  serviceFactory;

  public String execute() throws Exception {
    final Collection<String> errors = new ArrayList<String>(2);

    if ((!email1.equals(email2))) {
      errors.add("Email addresses don't match");
    }

    if (!password1.equals(password2)) {
      errors.add("Passwords don't match");
    }

    if (errors.isEmpty()) {
      createUser(email1, password1);
      return SUCCESS;
    } else {
      setActionErrors(errors);
      return ERROR;
    }
  }


  private void createUser(final String userId, final String password) {
    user = getServiceFactory()
            .getRegistrationService()
            .createUser(userId, password);
  }

  @EmailValidator(type=ValidatorType.SIMPLE, fieldName="email1", message="You must enter a valid email")
  @RequiredStringValidator(type=ValidatorType.FIELD, fieldName="email1", message="You must enter an email address")
  @FieldExpressionValidator(fieldName="email2", expression = "email1==email2", message="Email addresses must match")
  public String getEmail1() {
    return email1;
  }

  public void setEmail1(final String email1) {
    this.email1 = email1;
  }

  public String getEmail2() {
    return email2;
  }

  public void setEmail2(String email2) {
    this.email2 = email2;
  }

  @RequiredStringValidator(type=ValidatorType.FIELD, fieldName="password1", message="You must enter a password")
  @FieldExpressionValidator(fieldName="password2", expression = "password1==password2", message="Passwords must match")
  public String getPassword1() {
    return password1;
  }

  public void setPassword1(String password1) {
    this.password1 = password1;
  }

  public String getPassword2() {
    return password2;
  }

  public void setPassword2(String password2) {
    this.password2 = password2;
  }

  private ServiceFactory getServiceFactory() {
    return serviceFactory;
  }

  public void setServiceFactory(final ServiceFactory serviceFactory) {
    this.serviceFactory = serviceFactory;
  }

  public User getUser() {
    return user;
  }
}
