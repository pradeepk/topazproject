/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */


package org.plos.user.action;

import java.util.Map;

import com.opensymphony.xwork.ActionContext;
import com.opensymphony.xwork.validator.annotations.EmailValidator;
import com.opensymphony.xwork.validator.annotations.RequiredStringValidator;
import com.opensymphony.xwork.validator.annotations.StringLengthFieldValidator;
import com.opensymphony.xwork.validator.annotations.ValidatorType;

import edu.yale.its.tp.cas.client.filter.CASFilter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.plos.user.PlosOneUser;
import org.plos.user.service.UserService;

/**
 * Creates a new user in Topaz and sets come Profile properties.  User must be logged in via CAS.
 * 
 * @author Stephen Cheng
 * 
 */
public class CreateUserAction extends UserActionSupport {

  private String username, email, realName, internalId;

  private String authId;

  private static final Log log = LogFactory.getLog(CreateUserAction.class);

  /**
   * Will take the CAS ID and create a user in Topaz associated with that auth ID. If auth ID
   * already exists, it will not create another user. Email and Username are required and the
   * profile will be updated.  
   * 
   * @return status code for webwork
   */
  public String execute() throws Exception {
    Map sessionMap = ActionContext.getContext().getSession();
    if (sessionMap != null) {
      authId = (String) sessionMap.get(CASFilter.CAS_FILTER_USER);
    }

    internalId = getUserService().lookUpUserByAuthId(authId);
    if (internalId == null) {
      internalId = getUserService().createUser(authId);
    }
    if (log.isDebugEnabled()) {
      log.debug("Topaz ID: " + internalId + " with authID: " + authId);
    }

    PlosOneUser newUser = new PlosOneUser(authId);
    newUser.setUserId(internalId);
    newUser.setEmail(this.email);
    newUser.setDisplayName(this.username);
    newUser.setRealName(this.realName);

    getUserService().setProfile(newUser);

    return SUCCESS;
  }

  /**
   * Email is required and length must be less than 256 characters.
   * 
   * @return Returns the email.
   */
  @EmailValidator(type = ValidatorType.SIMPLE, fieldName = "email", 
                  message = "You must enter a valid email")
  @RequiredStringValidator(type = ValidatorType.FIELD, fieldName = "email", 
                           message = "You must enter an email address")
  @StringLengthFieldValidator(fieldName = "email", maxLength = "256", 
                              message = "Email must be less than 256")
  public String getEmail() {
    return email;
  }

  /**
   * @param email
   *          The email to set.
   */
  public void setEmail(String email) {
    this.email = email;
  }

  /**
   * @return Returns the realName.
   */
  public String getRealName() {
    return realName;
  }

  /**
   * @param realName
   *          The firstName to set.
   */
  public void setRealName(String realName) {
    this.realName = realName;
  }

  /**
   * @return Returns the username.
   */
  @RequiredStringValidator(type = ValidatorType.FIELD, fieldName = "username", 
                           message = "You must enter a username")
  @StringLengthFieldValidator(fieldName = "username", minLength = "4", maxLength = "18", 
                              message = "Username must be between 4 andn 18 characters")
  public String getUsername() {
    return username;
  }

  /**
   * @param username
   *          The username to set.
   */
  public void setUsername(String username) {
    this.username = username;
  }

  /**
   * @return Returns the internalId.
   */
  public String getInternalId() {
    return internalId;
  }

  /**
   * @param internalId
   *          The internalId to set.
   */
  public void setInternalId(String internalId) {
    this.internalId = internalId;
  }

  /**
   * Here mainly for unit tests. Should not need to be used otherwise
   * 
   * @return Returns the authId.
   */
  protected String getAuthId() {
    return authId;
  }

  /**
   * Here mainly for unit tests. Should not need to be used otherwise. Action picks it up from
   * session automatically.
   * 
   * @param authId
   *          The authId to set.
   */
  protected void setAuthId(String authId) {
    this.authId = authId;
  }

}
