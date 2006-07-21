/* $HeadURL::                                                                            $
 * $Id$
 *
 */
package org.plos.util;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 *
 */
public class AllTests {
  public static Test suite() {
    final TestSuite suite = new TestSuite();
    suite.addTestSuite(TestUniqueTokenGenerator.class);
    suite.addTestSuite(TestPasswordEncryptionService.class);
    return suite;
  }
}
