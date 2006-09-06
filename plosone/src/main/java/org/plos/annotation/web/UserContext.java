/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.annotation.web;

import com.opensymphony.xwork.ActionContext;

import java.util.Map;

/**
 * Provides a way to get a handle on the session objects when running in a web application
 */
public class UserContext {

  /**
   * @return the session variables for the user session in a map
   */
  public Map getSessionMap() {
    return ActionContext.getContext().getSession();
  }
}
