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

import com.opensymphony.xwork.validator.annotations.EmailValidator;
import com.opensymphony.xwork.validator.annotations.RequiredStringValidator;
import com.opensymphony.xwork.validator.annotations.StringLengthFieldValidator;
import com.opensymphony.xwork.validator.annotations.ValidatorType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import static org.plos.user.Constants.Length;
import static org.plos.user.Constants.PLOS_ONE_USER_KEY;
import org.plos.user.PlosOneUser;

import java.util.Map;

/**
 * Creates a new user in Topaz and sets come Profile properties.  User must be logged in via CAS.
 * 
 * @author Stephen Cheng
 * 
 */
public class CreateUserAction extends UserActionSupport {

  private String username, email, realName, topazId;

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
    final Map<String, Object> sessionMap = getSessionMap();
    if (sessionMap != null) {
      authId = getUserId(sessionMap);
    }

    topazId = getUserService().lookUpUserByAuthId(authId);
    if (topazId == null) {
      topazId = getUserService().createUser(authId);
    }
    if (log.isDebugEnabled()) {
      log.debug("Topaz ID: " + topazId + " with authID: " + authId);
    }

    final PlosOneUser newUser = new PlosOneUser(authId);
    newUser.setUserId(topazId);
    newUser.setEmail(this.email);
    newUser.setDisplayName(this.username);
    newUser.setRealName(this.realName);

    getUserService().setProfile(newUser);

    sessionMap.put(PLOS_ONE_USER_KEY, newUser);

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
  @StringLengthFieldValidator(fieldName = "email", maxLength = Length.EMAIL,
                              message = "Email must be less than " + Length.EMAIL)
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
  @StringLengthFieldValidator(fieldName = "username",
                              minLength = Length.DISPLAY_NAME_MIN,
                              maxLength = Length.DISPLAY_NAME_MAX,
                              message = "Username must be between " + Length.DISPLAY_NAME_MIN + " and " + Length.DISPLAY_NAME_MAX + " characters")
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
   * @return Returns the topazId.
   */
  public String getInternalId() {
    return topazId;
  }

  /**
   * @param internalId
   *          The topazId to set.
   */
  public void setInternalId(String internalId) {
    this.topazId = internalId;
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
