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

import java.sql.SQLException;

public class UserServiceTest extends TestCase {

  public void testUserIsFound() throws SQLException, ClassNotFoundException {
    final UserService userService
            = new UserService(
              "org.postgresql.Driver",
              "jdbc:postgresql://localhost/postgres",
              "select id from plos_user where loginname=?",
              "select loginname from plos_user where id=?",
              "postgres",
              "postgres");
    final String testUsername = "viru";
    final String guid = userService.getGuid(testUsername);
//    guid = userService.getGuid("susie@home.com");
    assertNotNull(guid);
    final String username = userService.getEmailAddress(guid);
    assertNotNull(username);
    assertEquals(testUsername, username);

  }

}
