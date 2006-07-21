/* $HeadURL::                                                                            $
 * $Id$
 *
 */
package org.plos;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 *
 */
public class AllTests {
  public static Test suite() {
    final TestSuite suite = new TestSuite();
    suite.addTest(org.plos.service.AllTests.suite());
    suite.addTest(org.plos.web.AllTests.suite());
    suite.addTest(org.plos.util.AllTests.suite());
    return suite;
  }
}
