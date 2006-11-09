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

import static com.opensymphony.xwork.Action.SUCCESS;
import org.plos.BasePlosoneTestCase;
import static org.plos.Constants.PLOS_ONE_USER_KEY;
import org.plos.user.PlosOneUser;

import java.util.HashMap;
import java.util.Map;

public class UserActionsTest extends BasePlosoneTestCase {
  private static final String TEST_EMAIL = "testcase@topazproject.org";
  private static final String REAL_NAME = "Test User";
  private static final String AUTH_ID = "Test AuthID";
  private static final String USERNAME= "TEST_USERNAME";

  public void testCreateUser() throws Exception {
    final CreateUserAction createUserAction = getCreateUserAction();
    createUserAction.setEmail(TEST_EMAIL);
    createUserAction.setRealName(REAL_NAME);
    createUserAction.setAuthId(AUTH_ID);
    createUserAction.setUsername(USERNAME);
    assertEquals(SUCCESS, createUserAction.execute());
    final String topazId = createUserAction.getInternalId();
    assertNotNull(topazId);
    
    final DisplayUserAction displayUserAction = getDisplayUserAction();
    displayUserAction.setUserId(topazId);
    assertEquals(SUCCESS, displayUserAction.execute());

    final PlosOneUser pou = displayUserAction.getPou();
    assertEquals (TEST_EMAIL, pou.getEmail());
    assertEquals (REAL_NAME, pou.getRealName());
    assertEquals (USERNAME, pou.getDisplayName());
  }

  public void testCreateAdminUser() throws Exception {
    final CreateUserAction createUserAction = getCreateUserAction();
    createUserAction.setEmail(TEST_EMAIL);
    createUserAction.setRealName(REAL_NAME);
    createUserAction.setAuthId(AUTH_ID);
    createUserAction.setUsername(USERNAME);
    assertEquals(SUCCESS, createUserAction.execute());
    final String topazId = createUserAction.getInternalId();
    assertNotNull(topazId);

    final AssignAdminRoleAction assignAdminRoleAction = getAssignAdminRoleAction(AUTH_ID, topazId);
    assertEquals(SUCCESS, assignAdminRoleAction.execute());

    getUserWebService().deleteUser(topazId);
  }

  protected AssignAdminRoleAction getAssignAdminRoleAction(final String authId, final String topazId) {
    final AssignAdminRoleAction adminRoleAction = super.getAssignAdminRoleAction();
    AssignAdminRoleAction newAdminRoleAction = new AssignAdminRoleAction() {
      protected Map<String, Object> getSessionMap() {
        final PlosOneUser plosOneUser = new PlosOneUser(authId);
        plosOneUser.setUserId(topazId);

        final Map<String, Object> sessionMap = new HashMap<String, Object>();
        sessionMap.put(PLOS_ONE_USER_KEY, plosOneUser);

        return sessionMap;
      }
    };

    newAdminRoleAction.setUserService(adminRoleAction.getUserService());

    return newAdminRoleAction;
  }
}
