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

import com.opensymphony.xwork.ActionSupport;

import org.plos.user.service.UserService;
import org.plos.user.Constants;

import java.util.Map;

/**
 * Base class for user actions in order to have a userService object accessible
 * 
 * @author Stephen Cheng
 * 
 */
public class UserActionSupport extends ActionSupport {
  private UserService userService;

  /**
   * Note: The visibility of this method is default so that the JSON serializer does not get into
   * an infinite circular loop when trying to serialize the action.
   * @return Returns the userService.
   */
  UserService getUserService() {
    return userService;
  }

  /**
   * @param userService
   *          The userService to set.
   */
  public void setUserService(UserService userService) {
    this.userService = userService;
  }

  protected String getUserId(final Map<String, Object> sessionMap) {
    return (String) sessionMap.get(Constants.SINGLE_SIGNON_USER_KEY);
  }

  protected Map<String, Object> getSessionMap() {
    return userService.getUserContext().getSessionMap();
  }
}
