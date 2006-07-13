package org.plos.service;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * $HeadURL: $
 * @version: $Id: $
 */
public class AllTests {
  public static Test suite() {
    final TestSuite suite = new TestSuite();
    suite.addTestSuite(org.plos.service.TestRegistrationService.class);
    suite.addTestSuite(org.plos.service.TestHibernate.class);
    return suite;
  }
}
