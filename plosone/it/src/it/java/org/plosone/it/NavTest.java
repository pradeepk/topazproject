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

import org.plosone.it.pages.HomePage;
import org.plosone.it.pages.ArticlePage;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * PlosOne Tests
 *
 * @author Pradeep Krishnan
 */
public class NavTest extends AbstractPlosOneTest {
  private static final Log    log         = LogFactory.getLog(NavTest.class);

  private Map<String, String[]> articles = new HashMap();

  /**
   * DOCUMENT ME!
   *
   * @throws Error DOCUMENT ME!
   */
  @BeforeClass
  public void setUp() {
    installEnvs();
    initTesters();
    getBasicEnv().start();
    setUpArticles();
  }

  private void setUpArticles() {
    String both[] = new String[] {HomePage.J_PONE, HomePage.J_CT};
    String pone[] = new String[] {HomePage.J_PONE};
    String ct[] = new String[] {HomePage.J_CT};
    articles.put("info:doi/10.1371/journal.pone.0000021", both);
    articles.put("info:doi/10.1371/journal.pone.0000155", pone);
    articles.put("info:doi/10.1371/journal.pctr.0020028", ct);
  }

  /**
   * DOCUMENT ME!
   */
  @Test
  public void testHome() {
    boolean browserStarted = false;
    for (String journal : new String[] {HomePage.J_PONE, HomePage.J_CT}) {
      for (String key: testers.keySet()) {
        log.info("Testing " + journal + " home-page with " + key + " ... ");
        HomePage hp = new HomePage(testers.get(key), journal);
        if (browserStarted)
          hp.gotoPage();
        else
          hp.beginAt();
        hp.verifyPage();
      }
      browserStarted = true;
    }
  }

  /**
   * DOCUMENT ME!
   */
  @Test(dependsOnMethods={"testHome"})
  public void testLogin() {
    for (String journal : new String[] {HomePage.J_PONE, HomePage.J_CT}) {
      for (String key: testers.keySet()) {
        log.info("Testing " + journal + " login with " + key + " ... ");
        HomePage hp = new HomePage(testers.get(key), journal);
        hp.gotoPage();
        hp.loginAs("admin", "plosadmin@gmail.com");
        hp.verifyPage();
        hp.logOut();
        hp.verifyPage();
      }
    }
  }

  @Test(dependsOnMethods={"testHome"})
  public void testArticle() {
    for (String article : articles.keySet()) {
      for (String journal : articles.get(article)) {
        for (String key: testers.keySet()) {
          // TODO: fix java script for firefox
          if (FIREFOX2.equals(key))
            continue;

          log.info("Testing " + journal + " article view with " + key + " ... ");
          ArticlePage ap = new ArticlePage(testers.get(key), journal, article);
          ap.gotoPage();
          ap.verifyPage();
        }
      }
    }
  }

}
