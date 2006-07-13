package org.plos.web;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * $HeadURL: $
 * @version: $Id: $
 */
public class AllTests {
  public static Test suite() {
    final TestSuite suite = new TestSuite();
    suite.addTestSuite(org.plos.web.TestRegistrationAction.class);
    return suite;
  }
}
