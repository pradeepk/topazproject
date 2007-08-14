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

import org.testng.annotations.Test

/**
 * Test /home.action.
 */
public class HomeActionWebTest extends PlosWebTest {

  @Test
  public void testSuite() throws Exception {

    try {
      ant.webtest(name:'/home.action') {
        config(config)
        steps() {
          invoke(description:'home page', url:'/home.action')
          verifyTitle(description:'verify home page title',
            text:'PLoS ONE : Publishing science, accelerating research')
        }
      }
    } catch (Exception e) {
      e.printStackTrace()
      throw(e)
    }
  }
}
