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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.plosone.it.jwebunit.PlosOneTestContext;
import org.plosone.it.jwebunit.PlosOneWebTester;
import static org.testng.AssertJUnit.*;

import com.gargoylesoftware.htmlunit.BrowserVersion;

import net.sourceforge.jwebunit.util.TestingEngineRegistry;

/**
 * A base class for plosone tests.
 *
 * @author Pradeep Krishnan
 */
public abstract class AbstractPlosOneTest {
  private static final Log log = LogFactory.getLog(AbstractPlosOneTest.class);

  /**
   * DOCUMENT ME!
   */
  public static final String TEST_ENGINE = PlosOneHtmlUnitDialog.class.getName();

  /**
   * DOCUMENT ME!
   */
  public static final String BASE_URL = "http://localhost:8080/plosone-webapp";

  /**
   * DOCUMENT ME!
   */
  public static final Map<String, BrowserVersion> browsers = new HashMap();

  public static final String IE7 = "Internet Explorer 7";
  public static final String IE6 = "Internet Explorer 6";
  public static final String FIREFOX2 = "Firefox 2";

  static {
    browsers.put(IE6, BrowserVersion.INTERNET_EXPLORER_6_0);
    browsers.put(IE7, BrowserVersion.INTERNET_EXPLORER_7_0);
    browsers.put(FIREFOX2, BrowserVersion.FIREFOX_2);

    // Note: Point the browser to emulate at http://htmlunit.sourceforge.net/cgi-bin/browserVersion 
    //       for the code to generate to add to the list of browsers above.
    try {
      TestingEngineRegistry.addTestingEngine(TEST_ENGINE, TEST_ENGINE);
    } catch (Throwable t) {
      throw new Error("Registration of test-engine failed", t);
    }
  }
  ;

  /**
   * DOCUMENT ME!
   */
  protected final Env[] envs =
    new Env[] {
                new Env("install/basic", "org.plosone:plosone-it-data-basic:0.8"),
                new Env("install/empty", null)
    };

  /**
   * DOCUMENT ME!
   */
  protected final Map<String, PlosOneWebTester> testers = new HashMap();

  /**
   * DOCUMENT ME!
   */
  public void installEnvs() {
    for (Env env : envs)
      env.install();
  }

  /**
   * DOCUMENT ME!
   */
  public void initTesters() {
    for (String key : browsers.keySet()) {
      PlosOneWebTester tester  = new PlosOneWebTester();
      tester.setTestContext(new PlosOneTestContext(browsers.get(key)));
      tester.setTestingEngineKey(TEST_ENGINE);
      tester.getTestContext().setBaseUrl("http://localhost:8080/plosone-webapp");
      testers.put(key, tester);
    }
  }
}
