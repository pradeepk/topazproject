/* $HeadURL::                                                                                      $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plosone.it.jwebunit;

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

/**
 * An extension for WebTester for keeping some plosone specific states.
 *
 * @author Pradeep Krishnan
 */
public class PlosOneWebTester extends WebTester {
  private boolean loggedIn = false;
  private boolean admin    = false;

  /**
   * Gets the login state.
   *
   * @return the login state of this tester
   */
  public boolean isLoggedIn() {
    return loggedIn;
  }

  /**
   * Set the login state of this tester
   *
   * @param loggedIn the state to set
   */
  public void setLoggedIn(boolean loggedIn) {
    this.loggedIn = loggedIn;
  }

  /**
   * Gets the admin state of this tester.
   *
   * @return admin if the logged in user role is set to admin
   */
  public boolean isAdmin() {
    return admin;
  }

  /**
   * Set admin.
   *
   * @param admin the value to set.
   */
  public void setAdmin(boolean admin) {
    this.admin = admin;
  }
}
