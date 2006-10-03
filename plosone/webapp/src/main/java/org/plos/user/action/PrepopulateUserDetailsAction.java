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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import static org.plos.user.Constants.PLOS_ONE_USER_KEY;
import static org.plos.user.Constants.ReturnCode.NEW_PROFILE;
import static org.plos.user.Constants.ReturnCode.UPDATE_PROFILE;
import static org.plos.user.Constants.SINGLE_SIGNON_USER_KEY;
import org.plos.user.PlosOneUser;
import org.plos.util.FileUtils;

import java.util.Map;

/**
 * Prepopulate the user profile data as available
 */
public class PrepopulateUserDetailsAction extends UserActionSupport {

  private String username;
  private String email;
  private String realName;

  private static final Log log = LogFactory.getLog(PrepopulateUserDetailsAction.class);

  /**
   * Prepopulate the user profile data if found
   * @return return code for webwork
   */
  public String execute() throws Exception {
    final Map<String, Object> sessionMap = getUserService().getUserContext().getSessionMap();
    final String userId = (String) sessionMap.get(SINGLE_SIGNON_USER_KEY);

    final PlosOneUser plosOneUser = (PlosOneUser) sessionMap.get(PLOS_ONE_USER_KEY);

    if (null == plosOneUser) {
      final String emailAddressUrl = getEmailAddressUrl();
      email = FileUtils.getTextFromUrl(emailAddressUrl + userId);
      return NEW_PROFILE;
    } else {
      email = plosOneUser.getEmail();
      username = plosOneUser.getDisplayName();
      realName = plosOneUser.getRealName();
      return UPDATE_PROFILE;
    }
  }

  private String getEmailAddressUrl() {
    return getUserService().getEmailAddressUrl();
  }

  /** @return Returns the email.*/
  public String getEmail() {
    return email;
  }

  /** @return Returns the realName.*/
  public String getRealName() {
    return realName;
  }

  /** @return Returns the username.*/
  public String getUsername() {
    return username;
  }
}
