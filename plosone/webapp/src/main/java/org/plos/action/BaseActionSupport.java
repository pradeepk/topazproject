/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.action;

import com.opensymphony.xwork.ActionSupport;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Base class for all actions.
 */
public abstract class BaseActionSupport extends ActionSupport {
  /**
   * This overrides the deprecated super inplementation and returns an empty implementation as we
   * want to avoid JSON serializing the deprecated implementation when it tries to serialize
   * an Action when the result type is ajaxJSON.
   *
   * @return a empty list
   */
  public Collection getErrorMessages() {
      return Collections.EMPTY_LIST;
  }

  /**
   * This overrides the deprecated super inplementation and returns an empty implementation as we
   * want to avoid JSON serializing the deprecated implementation when it tries to serialize
   * an Action when the result type is ajaxJSON.
   *
   * @return a empty map
   */
  public Map getErrors() {
      return Collections.EMPTY_MAP;
  }

}
