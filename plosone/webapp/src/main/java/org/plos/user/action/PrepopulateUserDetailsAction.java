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
import static org.plos.Constants.PLOS_ONE_USER_KEY;
import static org.plos.Constants.ReturnCode.NEW_PROFILE;
import static org.plos.Constants.ReturnCode.UPDATE_PROFILE;
import org.plos.user.PlosOneUser;

import java.util.Map;

/**
 * Prepopulate the user profile data as available
 */
public class PrepopulateUserDetailsAction extends UserActionSupport {
  private String username;
  private String email;
  private String realName;
  private String[] privateFields = new String[]{""};
  private static final String PUBLIC_VISIBLE = "public";
   
  
  private static final Log log = LogFactory.getLog(PrepopulateUserDetailsAction.class);

  /**
   * Prepopulate the user profile data if found
   * @return return code for webwork
   */
  public String execute() throws Exception {
    final Map<String, Object> sessionMap = getSessionMap();
    final PlosOneUser plosOneUser = (PlosOneUser) sessionMap.get(PLOS_ONE_USER_KEY);

    if (null == plosOneUser) {
      email = fetchUserEmailAddress();
      log.debug("new profile with email: " + email);
      return NEW_PROFILE;
    } else {
      log.debug("this is an existing user with email: " + plosOneUser.getEmail());
      email = plosOneUser.getEmail();
      username = plosOneUser.getDisplayName();
      realName = plosOneUser.getRealName();
      return UPDATE_PROFILE;
    }
  }

  /** @return Returns the email.*/
  public String getEmail() {
    log.debug ("calling getEmail() for user "); new Exception().printStackTrace() ;
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

  /**
   * Getter for property 'privateFields'.
   * @return Value for property 'privateFields'.
   */
  public String[] getPrivateFields() {
    return privateFields;
  }
  
  /**
   * Getter for extendedVisibility.
   * @return Value of extendedVisibility.
   */
  public String getExtendedVisibility() {
    return PUBLIC_VISIBLE;
  }

  /**
   * Getter for nameVisibility.
   * @return Value of nameVisibility.
   */
  public String getNameVisibility() {
    return PUBLIC_VISIBLE;
  }

  /**
   * Getter for orgVisibility.
   * @return Value of orgVisibility.
   */
  public String getOrgVisibility() {
    return PUBLIC_VISIBLE;
  }
  
  
}
