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
 * BootStrap tests for an empty environment
 *
 * @author Pradeep Krishnan
 */
public class BootStrapTest extends AbstractPlosOneTest {
  private static final Log    log         = LogFactory.getLog(BootStrapTest.class);

  /**
   * DOCUMENT ME!
   *
   * @throws Error DOCUMENT ME!
   */
  @BeforeClass
  public void setUp() {
    installEnvs();
    initTesters();
    envs[1].restore();
    envs[1].start();
  }

  /**
   * DOCUMENT ME!
   */
  @Test
  public void testJournalInstall() {
    String script = envs[1].resource("/createjournal.groovy");
    String plosone = envs[1].resource("/journal-plosone.xml");
    String ct = envs[1].resource("/journal-clinicaltrials.xml");
    envs[1].script(new String[]{script, "-j", plosone});
    envs[1].script(new String[]{script, "-j", ct});
    envs[1].stop();
    envs[1].start();
  }

  @Test(dependsOnMethods={"testJournalInstall"})
  public void testPlosOneHomePage() {
    log.info("Testing home-page after journal install  ... ");
    HomePage hp = new HomePage(testers.get(IE7));
    hp.beginAt();
    hp.verifyPage();
  }

}
