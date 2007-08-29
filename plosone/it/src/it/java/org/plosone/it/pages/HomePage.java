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
 * PlosOne Home page
 *
 * @author Pradeep Krishnan
 */
public class HomePage extends CommonBasePage {

  public static final String PAGE_URL = "/home.action";

  public HomePage(PlosOneWebTester tester) {
    super(tester,PAGE_URL);
  }

  public void verifyPage() {
    tester.assertTitleEquals("PLoS ONE : Publishing science, accelerating research");
    super.verifyPage();
  }

}
