/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.auth.web;

import junit.framework.TestCase;

import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class UserServiceTest extends TestCase {
  private static final Log log = LogFactory.getLog(UserServiceTest.class);
  public void testUserIsFound() throws Exception {
    final Properties dbProperties = new Properties();
    dbProperties.setProperty("url", "jdbc:postgresql://localhost/postgres");
    dbProperties.setProperty("user", "postgres");
    dbProperties.setProperty("password", "postgres");

    final DatabaseContext context = new DatabaseContext("org.postgresql.Driver", dbProperties, 2, 10, "select 1");

    final UserService userService
            = new UserService(context, 
              "select id from plos_user where loginname=?",
              "select loginname from plos_user where id=?");
    final String testUsername = "viru";

    final String guid = userService.getGuid(testUsername);
    assertNotNull(guid);
    final String username = userService.getEmailAddress(guid);
    assertNotNull(username);
    assertEquals(testUsername, username);
    log.debug(context.getStatus());
  }

}
