package org.plos.util;

import junit.framework.TestCase;

/**
 * $HeadURL: $
 * @version: $Id: $
 */
public class TestUniqueTokenGenerator extends TestCase {
  public void testShouldGenerateUniqueTokens() {
    final String uniqueToken1 = TokenGenerator.getUniqueToken();
    final String uniqueToken2 = TokenGenerator.getUniqueToken();
    assertFalse(uniqueToken1.equals(uniqueToken2));
  }
}
