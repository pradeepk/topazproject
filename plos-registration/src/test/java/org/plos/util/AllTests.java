package org.plos.util;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * $HeadURL: http://gandalf.topazproject.org/svn/head/plosone-registration/src/test/java/org/plos/service/AllTests.java $
 * @version: $Id: AllTests.java 164 2006-07-13 02:56:53Z ebrown $
 */
public class AllTests {
  public static Test suite() {
    final TestSuite suite = new TestSuite();
    suite.addTestSuite(TestUniqueTokenGenerator.class);
    suite.addTestSuite(TestPasswordEncryptionService.class);
    return suite;
  }
}
