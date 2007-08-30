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

import java.net.URLEncoder;

import org.plosone.it.jwebunit.PlosOneWebTester;

/**
 * PlosOne Article Display Page
 *
 * @author Pradeep Krishnan
 */
public class ArticlePage extends CommonBasePage {

  public static final String PAGE_URL = "/article/";

  public ArticlePage(PlosOneWebTester tester, String journal, String doi) {
    super(tester,journal, PAGE_URL + URLEncoder.encode(doi));
  }

  public void verifyPage() {
    //tester.assertTitleEquals("PLoS ONE : Publishing science, accelerating research");
    super.verifyPage();
  }

}
