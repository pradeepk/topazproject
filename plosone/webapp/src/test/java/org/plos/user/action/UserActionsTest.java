/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.user.action;

import com.opensymphony.xwork.Action;
import org.plos.BasePlosoneTestCase;
import org.plos.user.PlosOneUser;


public class UserActionsTest extends BasePlosoneTestCase {
  private static final String TEST_EMAIL = "testcase@topazproject.org";
  private static final String REAL_NAME = "Test User";
  private static final String AUTH_ID = "Test AuthID";
  private static final String USERNAME= "TEST_USERNAME";
 
  public void testSequencedTests() throws Exception {
    final CreateUserAction createUserAction1 = getCreateUserAction();
    createUserAction1.setEmail(TEST_EMAIL);
    createUserAction1.setRealName(REAL_NAME);
    createUserAction1.setAuthId(AUTH_ID);
    createUserAction1.setUsername(USERNAME);
    assertEquals(Action.SUCCESS, createUserAction1.execute());
    assertNotNull(createUserAction1.getInternalId());
    final DisplayUserAction displayUserAction1 = getDisplayUserAction();
    displayUserAction1.setUserId(createUserAction1.getInternalId());
    assertEquals(Action.SUCCESS, displayUserAction1.execute());
    final PlosOneUser pou = displayUserAction1.getPou();
    assertEquals (TEST_EMAIL, pou.getEmail());
    assertEquals (REAL_NAME, pou.getRealName());
    assertEquals (USERNAME, pou.getDisplayName());
  }

}
