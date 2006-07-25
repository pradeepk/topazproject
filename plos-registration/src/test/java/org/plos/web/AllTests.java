/* $HeadURL::                                                                            $
 * $Id$
 *
 */
package org.plos.web;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 *
 */
public class AllTests {
  public static Test suite() {
    final TestSuite suite = new TestSuite();
    suite.addTestSuite(org.plos.web.TestRegistrationAction.class);
    suite.addTestSuite(org.plos.web.TestConfirmationAction.class);
    return suite;
  }
}
