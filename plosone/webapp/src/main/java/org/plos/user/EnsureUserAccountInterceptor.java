/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.user;

import com.opensymphony.xwork.ActionInvocation;
import com.opensymphony.xwork.interceptor.Interceptor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import static org.plos.Constants.PLOS_ONE_USER_KEY;
import static org.plos.Constants.ReturnCode;
import static org.plos.Constants.SINGLE_SIGNON_USER_KEY;
import org.plos.user.service.UserService;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * Ensures that the user has a profile if the user does something which requires membership
 * Get the user object for the logged in user or redirect the user to set up his profile.
 */
public class EnsureUserAccountInterceptor implements Interceptor {
  private UserService userService;
  private static final Log log = LogFactory.getLog(EnsureUserAccountInterceptor.class);

  public String intercept(final ActionInvocation actionInvocation) throws Exception {
    final Map<String, Object> sessionMap = getUserSessionMap();
    final String userId = (String) sessionMap.get(SINGLE_SIGNON_USER_KEY);

    if (null == userId) {
      return actionInvocation.invoke();
    }

    PlosOneUser plosUser = (PlosOneUser) sessionMap.get(PLOS_ONE_USER_KEY);
    if (null != plosUser) {
      log.warn("A valid PlosOneUser exists, can a call to this interceptor be avoided in this case");
      return getReturnCodeDependingOnDisplayName(plosUser, actionInvocation);
    } else {
      final UserService userService = getUserService();
      plosUser = userService.getUserByAuthId(userId);

      if (null == plosUser) {
        //forward to new profile creation page
        return ReturnCode.NEW_PROFILE;
      } else {
        sessionMap.put(PLOS_ONE_USER_KEY, plosUser);

        return getReturnCodeDependingOnDisplayName(plosUser, actionInvocation);
      }
    }
  }

  private String getReturnCodeDependingOnDisplayName(final PlosOneUser plosOneUser, final ActionInvocation actionInvocation) throws Exception {
    if (StringUtils.hasText(plosOneUser.getDisplayName())) {
      //forward the user to the page he was initially going to
      return actionInvocation.invoke();
    } else {
      // profile has partial details as the user might have been ported from old application
      return ReturnCode.UPDATE_PROFILE; //forward to update profile page
    }
  }

  protected Map<String, Object> getUserSessionMap() {
    return userService.getUserContext().getSessionMap();
  }

  private UserService getUserService() {
    return userService;
  }

  public void setUserService(final UserService userService) {
    this.userService = userService;
  }

  public void destroy() {}
  public void init() {}
}
