/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Escapes the user input if it has potential security issues like scripting security bugs, SQL injection bugs, etc.
 */
public class UserInputSecurityCheckingService {
  /**
   * Escape or declaw any text which might cause a security issues for the users or the aplication
   * @param content content to check for profanity
   * @return true if content is not profane
   */
  public List<String> escape(final String content) {
    //TODO: look for a stronger regular expression or algorithm to help with this
    final List<String> messages = new ArrayList<String>();
    if (content.contains("cancellunch")) {
      messages.add("Found dangerous method call: cancellunch");
    }
    return messages;
  }
}
