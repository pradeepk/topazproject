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
 * Checks that content is not profane. It could be used to check that the user's posts don't contain profane words like F***, GEORGE, BUSH, etc.
 */
public class ProfanityCheckingService {
  /** Validate that the content is profane or not
   * @param content content to check for profanity
   * @return true if content is not profane
   */
  public List<String> validate(final String content) {
    //TODO: look for a stronger regular expression or algorithm to help with this
    final List<String> messages = new ArrayList<String>();
    if (content.contains("BUSH")) {
      messages.add("Found obscene word: BUSH");
    }
    return messages;
  }
}
