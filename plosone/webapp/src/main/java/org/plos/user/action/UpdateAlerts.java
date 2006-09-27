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

import org.plos.user.PlosOneUser;

import com.opensymphony.xwork.ActionContext;

import edu.yale.its.tp.cas.client.filter.CASFilter;

/**
 * @author stevec
 *
 */
public class UpdateAlerts extends UserActionSupport {

  public String execute() throws Exception {
    PlosOneUser newUser = null;
    
    getUserService().setPreferences(newUser);
    
    return SUCCESS;
  }

  
  
}
