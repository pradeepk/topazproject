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



import static org.testng.AssertJUnit.*;




import org.plosone.it.jwebunit.PlosOneWebTester;

/**
 * A base class for pages that contain the most common page elements.
 *
 * @author Pradeep Krishnan
 */
public abstract class CommonBasePage extends AbstractPage {


  public CommonBasePage(PlosOneWebTester tester, String url) {
    super(tester,url);
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
    if (tester.isLoggedIn())
      logOut();

    beginAt();
    tester.clickLinkWithText("Login");
    tester.setFormElement("sso.auth.id", authId);
    tester.setFormElement("sso.email", email);
    tester.submit();
    tester.setLoggedIn(true);
    verifyPage();
  }

  public void logOut() {
    if (tester.isLoggedIn()) {
      tester.clickLinkWithText("Logout");
      tester.setLoggedIn(false);
      verifyPage();
    }
  }
}
