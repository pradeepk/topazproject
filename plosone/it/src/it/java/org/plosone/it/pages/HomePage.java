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

import java.io.IOException;

import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
