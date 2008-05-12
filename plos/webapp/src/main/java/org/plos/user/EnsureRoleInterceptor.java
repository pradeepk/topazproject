/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.plos.user;

import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;

import org.plos.Constants;
import org.plos.user.service.UserService;

/**
 * Ensures that the user has the required role.
 *
 * FIXME: this class should not exist. Instead, normal access-controls be used.
 */
public class EnsureRoleInterceptor extends AbstractInterceptor {
  private static final Log log = LogFactory.getLog(EnsureRoleInterceptor.class);

  private UserService userService;
  private String roleToCheck;

  public String intercept(final ActionInvocation actionInvocation) throws Exception {
    log.debug("EnsureRoleInterceptor called for role:" + roleToCheck);
    final Map<String, Object> sessionMap = getUserSessionMap();
    final String userId = (String) sessionMap.get(Constants.SINGLE_SIGNON_USER_KEY);

    if (null == userId) {
      log.debug("No user account found. Make sure the user gets logged in before calling this.");
      return Action.ERROR;
    }

    String[] userRoles = (String[]) sessionMap.get(Constants.USER_ROLES_KEY);
    if (userRoles == null) {
      PlosOneUser plosUser = (PlosOneUser) sessionMap.get(Constants.PLOS_ONE_USER_KEY);
      if (plosUser != null) {
        userRoles = userService.getRole(plosUser.getUserId());
        sessionMap.put(Constants.USER_ROLES_KEY, userRoles);
      }
    }

    if (userRoles != null) {
      if (ArrayUtils.contains(userRoles, roleToCheck)) {
        log.debug("User found with admin role:" + userId);
        return actionInvocation.invoke();
      } else {
        return Constants.ReturnCode.NOT_SUFFICIENT_ROLE;
      }
    }
    return Action.ERROR;
  }

  /**
   * Get the user session map.
   * @return session map
   */
  protected Map<String, Object> getUserSessionMap() {
    return userService.getUserContext().getSessionMap();
  }

  /**
   * Set the userService
   * @param userService userService
   */
  public void setUserService(final UserService userService) {
    this.userService = userService;
  }

  /**
   * Set the role to check for
   * @param roleToCheck roleToCheck
   */
  public void setRoleToCheck(final String roleToCheck) {
    this.roleToCheck = roleToCheck;
  }
}
