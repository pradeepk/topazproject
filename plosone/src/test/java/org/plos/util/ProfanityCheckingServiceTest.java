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

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Collection;

public class ProfanityCheckingServiceTest extends TestCase {
  public void testShouldCatchProfaneText() {
    final ProfanityCheckingService service = new ProfanityCheckingService();
    final Collection<String> profaneWordList = new ArrayList<String>();
    profaneWordList.add("BUSH");
    service.setWords(profaneWordList);
    found("bush", service);
    found(" bush", service);
    found("  bush", service);
    found("  \nbush", service);
    found("bUSH", service);
    found("am bush", service);
    found(".bush", service);
    found(" some bush", service);
    found("+bush", service);
    found("-bush", service);
    found(" bush", service);
    found(" some before Bush and some after", service);
    found(" some /n before and some after/n before Bush and some after", service);
    found("[Bushe ", service);
    found(" (Bush ", service);
    found("[Bush]", service);
  }

  public void testShouldAllowTextWhichIsNotProfane() {
    final ProfanityCheckingService service = new ProfanityCheckingService();
    final Collection<String> profaneWordList = new ArrayList<String>();
    profaneWordList.add("BUSH");
    service.setWords(profaneWordList);
    notFound("ambush", service);
    notFound(" some ambush", service);
    notFound(" amBush ", service);
    notFound(" some before amBush and some after", service);
    notFound(" some /n before some before some /n before amBush and some after /n adter", service);
  }

  private void notFound(final String content, final ProfanityCheckingService service) {
    assertEquals("["+content+"]", 0, service.validate(content).size());
  }

  private void found(final String content, final ProfanityCheckingService service) {
    assertEquals("["+content+"]", 1, service.validate(content).size());
  }
}
