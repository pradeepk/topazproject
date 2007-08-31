/* $HeadURL::                                                                                      $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plosone.it.pages;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.plosone.it.jwebunit.PlosOneWebTester;

/**
 * A base class for pages that contain the most common page elements.
 *
 * @author Pradeep Krishnan
 */
public abstract class CommonBasePage extends AbstractPage {
  private static final Log    log         = LogFactory.getLog(CommonBasePage.class);

  public CommonBasePage(PlosOneWebTester tester, String journal, String url) {
    super(tester, journal, url);
  }

  public void verifyPage() {
    if (!tester.isLoggedIn()) {
      tester.assertLinkPresentWithText("Login");
      tester.assertLinkPresentWithText("Create Account");
      tester.assertLinkPresentWithText("Feedback");
    } else {
      tester.assertLinkPresentWithText("Logout");
      tester.assertLinkPresentWithText("Preferences");
    }
    // TODO : Add more verification tests
  }


  public void loginAs(String authId, String email) {
    boolean onLoginPage = isLoginPage();
    if (!onLoginPage) {
      if (tester.isLoggedIn())
        logOut();

      log.debug("Going to login page ...");
      tester.clickLinkWithText("Login");
    }

    log.debug("Logging in as (" + authId + ", " + email + ") ...");
    tester.setFormElement("sso.auth.id", authId);
    tester.setFormElement("sso.email", email);
    tester.submit();
    tester.setLoggedIn(true);
    if (onLoginPage)
      gotoPage();
  }

  public void logOut() {
    if (tester.isLoggedIn()) {
      log.debug("Logging out ...");
      tester.clickLinkWithText("Logout");
      tester.setLoggedIn(false);
      gotoPage();  // logout takes you to home page. so come-back here again
    }
  }
}
