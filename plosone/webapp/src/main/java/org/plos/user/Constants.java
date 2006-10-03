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

import edu.yale.its.tp.cas.client.filter.CASFilter;

/**
 * Some of the constants for the Plosone application.
 */
public interface Constants {
  String PLOS_ONE_USER_KEY = "PLOS_ONE_USER";
  String SINGLE_SIGNON_USER_KEY = CASFilter.CAS_FILTER_USER;

  /**
   * Defines the length of various fields used by Webwork Annotations
   */
  interface Length {
    String EMAIL = "256";
    String PASSWORD = "256";
    String DISPLAY_NAME_MIN = "4";
    String DISPLAY_NAME_MAX = "18";
  }

  /**
   * Return Code to be used for WebWork actions
   */
  interface ReturnCode {
    String NEW_PROFILE = "new-profile";
    String UPDATE_PROFILE = "update-profile";
  }
}
