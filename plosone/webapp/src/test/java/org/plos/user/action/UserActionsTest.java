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
import org.plos.user.action.CreateUserAction;
import org.plos.user.action.DisplayUserAction;


public class UserActionsTest extends BasePlosoneTestCase {
  private static final String TEST_EMAIL = "testcase@topazproject.org";
  private static final String REAL_NAME = "Test User";
  private static final String AUTH_ID = "Test AuthID";
  private static final String USERNAME= "TEST_USERNAME";
 
  public void testSequencedTests() throws Exception {
    createAndRetrieveUser();
  }

  public void createAndRetrieveUser() throws Exception {
    final CreateUserAction createUserAction = getCreateUserAction();
    createUserAction.setEmail(TEST_EMAIL);
    createUserAction.setRealName(REAL_NAME);
    createUserAction.setAuthId(AUTH_ID);
    createUserAction.setUsername(USERNAME);
    assertEquals(Action.SUCCESS, createUserAction.execute());
    assertNotNull(createUserAction.getInternalId());
    final DisplayUserAction displayUserAction = getDisplayUserAction();
    displayUserAction.setUserId(createUserAction.getInternalId());
    assertEquals(Action.SUCCESS, displayUserAction.execute());
    final PlosOneUser pou = displayUserAction.getPou();
    assertEquals (TEST_EMAIL, pou.getEmail());
    assertEquals (REAL_NAME, pou.getRealName());
    assertEquals (USERNAME, pou.getDisplayName());
  }

}
