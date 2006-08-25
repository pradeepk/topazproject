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
import java.util.Collection;

/**
 * Checks that content is not profane. It could be used to check that the user's posts don't contain profane words like F***, GEORGE, BUSH, etc.
 */
public class ProfanityCheckingService {
  private Collection<String> profaneWords;

  /**
   * Validate that the content is profane or not
   * @param content content to check for profanity
   * @return true if content is not profane
   */
  public List<String> validate(final String content) {
    final List<String> messages = new ArrayList<String>();

    final String contentLowerCase = content.toLowerCase();
    for (final String word : profaneWords) {
      //TODO:  More work needed with the regular expression as it catches even when it should not.
      final String wordLowerCase = word.toLowerCase();
      if (contentLowerCase.matches(".*\\s*\\p{Punct}*" + wordLowerCase + ".*")) {
        if (contentLowerCase.matches(".*[a-zA-Z]+" + wordLowerCase)) {
          continue;
        }
        if (contentLowerCase.matches("^[[a-zA-Z]*\\s+\\p{Punct}*]*" + wordLowerCase + ".*")) {
          messages.add("Found obscene word:" + word);
          break;
        }
      }
    }
    

    return messages;
  }

  public void setWords(final Collection<String> profaneWords) {
    this.profaneWords = profaneWords;
  }
}
