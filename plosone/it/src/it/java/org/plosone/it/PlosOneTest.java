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

import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.*;

import junit.framework.TestCase;

/**
 * PlosOne Tests
 *
 * @author Pradeep Krishnan
 */
public class PlosOneTest extends TestCase {
  private static final Log log     = LogFactory.getLog(PlosOneTest.class);
  private static Env[] envs = new Env[] {new Env("install/07", "org.plosone:plosone-it-data:0.7"),
                             //  new Env("install/08", "org.plosone:plosone-it-data:0.7"),
                             //  new Env("install/empty", null)
  };

  static {
    for (Env env : envs)
      env.install();
  }


  public void start07() {
    envs[0].start(); // note: stops other envs
  }
/*
  public void start08() {
    envs[1].start(); // note: stops other envs
  }

  public void startEmpty() {
    envs[2].start(); // note: stops other envs
  }
*/
  /**
   * A simple test
   *
   * @throws Exception on an error
   */
  public void testHomePage() throws Exception {
    start07();
    final WebClient webClient = new WebClient(BrowserVersion.MOZILLA_1_0);
    final URL url = new URL("http://localhost:8080/plosone-webapp/home.action");
    final HtmlPage page = (HtmlPage)webClient.getPage(url);
    assertTrue( "PLoS ONE : Publishing science, accelerating research".equals(page.getTitleText()) );
  }

}
