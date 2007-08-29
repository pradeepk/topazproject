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
 * An abstract base class for PlosOne pages.
 *
 * @author Pradeep Krishnan
 */
public abstract class AbstractPage {

  protected final PlosOneWebTester tester;
  protected final String url;

  public AbstractPage(PlosOneWebTester tester, String url) {
    this.tester = tester;
    this.url = url;
  }

  public String getUrl() {
    return url;
  }

  public PlosOneWebTester getTester() {
    return tester;
  }


  public void beginAt() {
    tester.beginAt(url);
  }

  public abstract void verifyPage();
}
