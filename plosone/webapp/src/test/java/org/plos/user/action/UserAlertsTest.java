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
import org.plos.Constants;
import static org.plos.Constants.PLOS_ONE_USER_KEY;
import org.plos.user.PlosOneUser;
import org.apache.commons.lang.ArrayUtils;

import java.util.Map;
import java.util.HashMap;

public class UserAlertsTest extends BasePlosoneTestCase {
  final String AUTH_ID = UserAlertsTest.class.getName();

  public void testCreateAlerts() throws Exception {
    final String topazId = createUser(AUTH_ID);
    final UserAlertsAction alertsAction = getMockUserAlertsAction(AUTH_ID, topazId);
    final String[] weeklyAlertCategories = new String[]{
                "biology",
                "clinical_trials",
                "computational_biology",
                "genetics",
                "pathogens"
          };

    final String[] monthlyAlertCategories = new String[]{
                "plosone",
                "clinical_trials",
                "genetics",
                "pathogens"
          };

    alertsAction.setMonthlyAlerts(monthlyAlertCategories);
    alertsAction.setWeeklyAlerts(weeklyAlertCategories);
    assertEquals(SUCCESS, alertsAction.saveAlerts());

    assertEquals(SUCCESS, alertsAction.retrieveAlerts());

    for (final String monthlyAlert : alertsAction.getMonthlyAlerts()) {
      ArrayUtils.contains(monthlyAlertCategories, monthlyAlert);
    }

    for (final String weeklyAlert : alertsAction.getWeeklyAlerts()) {
      ArrayUtils.contains(monthlyAlertCategories, weeklyAlert);
    }

    getUserWebService().deleteUser(topazId);
  }

  protected UserAlertsAction getMockUserAlertsAction(final String authId, final String topazId) {
    final UserAlertsAction userAlertsAction = super.getUserAlertsAction();
    final UserAlertsAction newUserAlertsAction = new UserAlertsAction() {
      protected Map<String, Object> getSessionMap() {
        return createMockSessionMap(authId, topazId);
      }
    };

    newUserAlertsAction.setUserService(userAlertsAction.getUserService());

    return newUserAlertsAction;
  }

  private String createUser(final String authId) throws Exception {
    final CreateUserAction createUserAction = getMockCreateUserAction(authId);
    createUserAction.setEmail("UserAlertsTest@test.com");
    createUserAction.setRealName("UserAlertsTest test com");
    createUserAction.setAuthId(authId);
    createUserAction.setUsername("UserAlertsTest");
    assertEquals(SUCCESS, createUserAction.execute());
    final String topazId = createUserAction.getInternalId();
    assertNotNull(topazId);

    return topazId;
  }

  protected CreateUserAction getMockCreateUserAction(final String authId) {
    final CreateUserAction createUserAction = super.getCreateUserAction();
    final CreateUserAction newCreateUserAction = new CreateUserAction() {
      protected Map<String, Object> getSessionMap() {
        return createMockSessionMap(authId, null);
      }
    };

    newCreateUserAction.setUserService(createUserAction.getUserService());

    return newCreateUserAction;
  }

  private Map<String, Object> createMockSessionMap(final String authId, final String topazId) {
    final PlosOneUser plosOneUser = new PlosOneUser(authId);
    if (null != topazId) {
      plosOneUser.setUserId(topazId);
    }

    final Map<String, Object> sessionMap = new HashMap<String, Object>();
    sessionMap.put(PLOS_ONE_USER_KEY, plosOneUser);
    sessionMap.put(Constants.SINGLE_SIGNON_USER_KEY, authId);

    return sessionMap;
  }

}
