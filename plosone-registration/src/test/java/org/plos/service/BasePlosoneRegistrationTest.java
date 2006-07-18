package org.plos.service;

import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

/**
 * $HeadURL: $
 *
 * @version: $Id: $
 */
public class BasePlosoneRegistrationTest extends AbstractDependencyInjectionSpringContextTests {
  protected String[] getConfigLocations() {
    return new String[] {"applicationContext.xml"};
  }
}
