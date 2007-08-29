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

import java.io.IOException;

import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.plosone.it.pages.HomePage;
import static org.testng.AssertJUnit.*;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.Test;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebWindow;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import net.sourceforge.jwebunit.junit.WebTester;
import net.sourceforge.jwebunit.util.TestingEngineRegistry;

import org.plosone.it.jwebunit.PlosOneWebTester;

/**
 * PlosOne Tests
 *
 * @author Pradeep Krishnan
 */
@Test
public class PlosOneTest {
  private static final Log    log         = LogFactory.getLog(PlosOneTest.class);
  private static final String TEST_ENGINE = PlosOneHtmlUnitDialog.class.getName();
  private Env[]               envs        =
    new Env[] {
    // new Env("install/07", "org.plosone:plosone-it-data:0.7"),
    new Env("install/basic", "org.plosone:plosone-it-data-basic:0.8"), // new Env("install/empty", null)
    };

  private PlosOneWebTester tester;

  /**
   * DOCUMENT ME!
   *
   * @throws Error DOCUMENT ME!
   */
  @BeforeClass
  public void setUp() {
    for (Env env : envs)
      env.install();

    envs[0].start();

    try {
      TestingEngineRegistry.addTestingEngine(TEST_ENGINE, TEST_ENGINE);
    } catch (Throwable t) {
      throw new Error("Registration of test-engine failed", t);
    }

    tester = new PlosOneWebTester();
    tester.setTestingEngineKey(TEST_ENGINE);

    tester.getTestContext().setBaseUrl("http://localhost:8080/plosone-webapp");
  }

  /**
   * DOCUMENT ME!
   */
  @Test
  public void testHome() {
    HomePage hp = new HomePage(tester);
    hp.beginAt();
    hp.verifyPage();
  }

  /**
   * DOCUMENT ME!
   */
  @Test
  public void testLogin() {
    HomePage hp = new HomePage(tester);
    hp.beginAt();
    hp.loginAs("admin", "plosadmin@gmail.com");
    hp.verifyPage();
    hp.logOut();
    hp.verifyPage();
  }
}
