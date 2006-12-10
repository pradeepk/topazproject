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

public class TextUtilsTest extends TestCase {
  public void testValidatesUrl() {
    assertFalse(TextUtils.verifyUrl("http://"));
    assertFalse(TextUtils.verifyUrl("..."));
    assertFalse(TextUtils.verifyUrl("--"));
    assertFalse(TextUtils.verifyUrl("htt://..."));
    assertFalse(TextUtils.verifyUrl("ftps://..."));
    assertFalse(TextUtils.verifyUrl("asdasdasd"));
    assertFalse(TextUtils.verifyUrl("123123"));
    assertFalse(TextUtils.verifyUrl("http://www.yahoo.com:asas"));
    assertFalse(TextUtils.verifyUrl("http://www.   yahoo.com:asas"));

    assertTrue(TextUtils.verifyUrl("http://www.yahoo.com"));
    assertTrue(TextUtils.verifyUrl("http://www.yahoo.com:9090"));
    assertTrue(TextUtils.verifyUrl("http://www.yahoo.com/"));
    assertTrue(TextUtils.verifyUrl("https://www.yahoo.com/"));
    assertTrue(TextUtils.verifyUrl("ftp://www.yahoo.com/"));
  }

  public void testMakeUrl() throws Exception {
    assertEquals("http://www.google.com", TextUtils.makeValidUrl("www.google.com"));
    assertEquals("http://www.google.com", TextUtils.makeValidUrl("http://www.google.com"));
    assertEquals("ftp://www.google.com", TextUtils.makeValidUrl("ftp://www.google.com"));
    assertEquals("https://www.google.com", TextUtils.makeValidUrl("https://www.google.com"));
  }
}
