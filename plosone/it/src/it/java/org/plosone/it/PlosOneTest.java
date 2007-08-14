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

import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.testng.annotations.Test;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.BeforeSuite;

import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.*;

/**
 * PlosOne Tests
 *
 * @author Pradeep Krishnan
 */
public class PlosOneTest {
  private static final Log log     = LogFactory.getLog(PlosOneTest.class);
  private Env[] envs = new Env[] {
             // new Env("install/07", "org.plosone:plosone-it-data:0.7"),
              new Env("install/basic", "org.plosone:plosone-it-data-basic:0.8"),
             // new Env("install/empty", null)
  };

  @BeforeSuite
  public void install() {
    for (Env env : envs)
      env.install();
  }



  @BeforeGroups({"basic"})
  public void startBasic() {
    envs[0].start(); // note: stops other envs
  }

  @Test(groups={"basic"})
  public void testPlosOne() throws Exception {
    startBasic();
    HomeActionWebTest test = new HomeActionWebTest();
    test.testSuite();
  }

}
