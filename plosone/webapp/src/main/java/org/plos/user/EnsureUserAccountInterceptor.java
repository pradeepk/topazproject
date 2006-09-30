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

import com.opensymphony.xwork.ActionContext;
import com.opensymphony.xwork.ActionInvocation;
import com.opensymphony.xwork.interceptor.Interceptor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.ApplicationException;
import org.plos.user.service.UserService;
import org.springframework.util.StringUtils;

import java.util.Map;

import edu.yale.its.tp.cas.client.filter.CASFilter;

/**
 * Ensures that the user has a profile if the user does something which requires membership
 * Get the user object for the logged in user or redirect the user to set up his profile.
 */
public class EnsureUserAccountInterceptor implements Interceptor {
  //TODO: move this to spring context or xwork static-params example  file:///D:/java/webwork-2.2.4/docs/wikidocs/Static%20Parameters%20Interceptor.html
  //TODO: Look at page 232 of the webwork pdf for more info
  //ComponentManager cm = (ComponentManager) ServletActionContext.getRequest().getAttribute("DefaultComponentManager");
  //cm.initializeObject(interceptor);
  //Object interceptor;
  private static final String USER_ID = CASFilter.CAS_FILTER_USER;

  //TODO: move this to spring context or xwork static-params
  private static final String PLOS_USER = "plosUser";
  private static final String NEW_PROFILE = "new-profile";
  private static final String UPDATE_PROFILE = "update-profile";

  private UserService userService;
  private static final Log log = LogFactory.getLog(EnsureUserAccountInterceptor.class);

  public String intercept(final ActionInvocation actionInvocation) throws Exception {
    final Map<String, Object> sessionMap = getUserSessionMap();
    final String userId = (String) sessionMap.get(USER_ID);

    if (null == userId) {
      final String errorMessage = "User not logged in yet. How is the user getting here??";
      log.warn(errorMessage);
      throw new ApplicationException(errorMessage);
    }

    log.debug("UserId(guid)=" + userId);
    final PlosOneUser plosUser = (PlosOneUser) sessionMap.get(PLOS_USER);
    if (null != plosUser) {
      log.warn("A valid PlosOneUser exists, can a call to this interceptor be avoided in this case");
      return actionInvocation.invoke();
    } else {
      final UserService userService = getUserService();
      log.debug("userService.getApplicationId()=" + userService.getApplicationId());
      final PlosOneUser plosOneUser = userService.getUserByAuthId(userId);

      if (null == plosOneUser) {
        //forward to new profile creation page
        return NEW_PROFILE;
      } else {
        sessionMap.put(PLOS_USER, plosOneUser);
        if (StringUtils.hasText(plosOneUser.getDisplayName())) {
          //forward the user to the page he was going to
          return actionInvocation.invoke();
        } else {// profile has partial details as the user might have been ported from old application
          //forward to update profile page with a new display name
          return UPDATE_PROFILE;
        }
      }
    }
  }

  private Map getUserSessionMap() {
    return ActionContext.getContext().getSession();
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
