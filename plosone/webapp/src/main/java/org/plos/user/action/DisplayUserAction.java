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

import org.plos.user.PlosOneUser;

/**
 * Simple class to display a user based on a TopazId
 * 
 * @author Stephen Cheng
 * 
 */
public class DisplayUserAction extends UserActionSupport {

  private static final Log log = LogFactory.getLog(DisplayUserAction.class);

  private PlosOneUser pou;
  private String userId;

  /**
   * Returns the user based on the userId passed in.
   * 
   * @return webwork status string
   */
  public String execute() throws Exception {

    pou = getUserService().getUser(userId);
    return SUCCESS;
  }

  /**
   * @return Returns the pou.
   */
  public PlosOneUser getPou() {
    return pou;
  }

  /**
   * @param pou
   *          The pou to set.
   */
  public void setPou(PlosOneUser pou) {
    this.pou = pou;
  }

  /**
   * @return Returns the userId.
   */
  public String getUserId() {
    return userId;
  }

  /**
   * @param userId
   *          The userId to set.
   */
  public void setUserId(String userId) {
    this.userId = userId;
  }
}
