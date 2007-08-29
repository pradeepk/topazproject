/* $HeadURL::                                                                                      $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plosone.it;



import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.plosone.it.pages.HomePage;
import static org.testng.AssertJUnit.*;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;




/**
 * PlosOne Tests
 *
 * @author Pradeep Krishnan
 */
public class HomePageTest extends AbstractPlosOneTest {
  private static final Log    log         = LogFactory.getLog(HomePageTest.class);

  /**
   * DOCUMENT ME!
   *
   * @throws Error DOCUMENT ME!
   */
  @BeforeClass
  public void setUp() {
    installEnvs();
    initTesters();
    env[0].start();
  }

  /**
   * DOCUMENT ME!
   */
  @Test
  public void testHome() {
    for (String key: testers.keySet()) {
      log.info("Testing home-page with " + key + " ... ");
      HomePage hp = new HomePage(testers.get(key));
      hp.beginAt();
      hp.verifyPage();
    }
  }

  /**
   * DOCUMENT ME!
   */
  @Test
  public void testLogin() {
    for (String key: testers.keySet()) {
      log.info("Testing login with " + key + " ... ");
      HomePage hp = new HomePage(testers.get(key));
      hp.beginAt();
      hp.loginAs("admin", "plosadmin@gmail.com");
      hp.verifyPage();
      hp.logOut();
      hp.verifyPage();
    }
  }
}
