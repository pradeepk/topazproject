/* $HeadURL:: http://gandalf.topazproject.org/svn/head/plosone/libs/article-util/src/main/groovy/#$
 * $Id: Delete.groovy 2686 2007-05-15 08:22:38Z ebrown $
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.plosone.it

/**
 * Test /home.action.
 */
public class HomeActionIT {

  def ant
  def props
  def config

  public void testSuite() throws Exception {

    ant.webtest(name:'/home.action') {
      config(config)
      steps() {
        invoke(description:'home page', url:'/home.action')
        verifyTitle(description:'verify home page title',
          text:'PLoS ONE : Publishing science, accelerating research')
      }
    }
  }
}
