/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.topazproject.ambra.user.action;

import static com.opensymphony.xwork2.Action.SUCCESS;
import org.apache.commons.lang.ArrayUtils;

import static org.topazproject.ambra.Constants.PLOS_ONE_USER_KEY;

import org.topazproject.ambra.BaseAmbraTestCase;
import org.topazproject.ambra.Constants;
import org.topazproject.ambra.user.PlosOneUser;
import org.topazproject.ambra.user.action.MemberUserAlertsAction;
import org.topazproject.ambra.user.action.MemberUserProfileAction;
import org.topazproject.ambra.user.action.UserAlertsAction;
import org.topazproject.ambra.user.action.UserProfileAction;

import java.util.HashMap;
import java.util.Map;

public class UserAlertsTest extends BaseAmbraTestCase {
  final String AUTH_ID = UserAlertsTest.class.getName();

  public void testCreateAlerts() throws Exception {
//    getUserService().deleteUser("info:doi/10.1371/account/141");
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
    final String ALERT_EMAIL = "alert@emailaddress.com";

    alertsAction.setMonthlyAlerts(monthlyAlertCategories);
    alertsAction.setWeeklyAlerts(weeklyAlertCategories);

    assertEquals(SUCCESS, alertsAction.saveAlerts());
    assertEquals(SUCCESS, alertsAction.retrieveAlerts());

    for (final String monthlyAlert : alertsAction.getMonthlyAlerts()) {
      assertTrue(ArrayUtils.contains(monthlyAlertCategories, monthlyAlert));
    }

    for (final String weeklyAlert : alertsAction.getWeeklyAlerts()) {
      assertTrue(ArrayUtils.contains(weeklyAlertCategories, weeklyAlert));
    }

    getUserService().deleteUser(topazId);
  }

  protected UserAlertsAction getMockUserAlertsAction(final String authId, final String topazId) {
    final UserAlertsAction userAlertsAction = super.getMemberUserAlertsAction();
    final UserAlertsAction newUserAlertsAction = new MemberUserAlertsAction() {
      private final Map<String, Object> mockSessionMap = createMockSessionMap(authId, topazId);
      protected Map<String, Object> getSessionMap() {
        return mockSessionMap;
      }
    };

    newUserAlertsAction.setUserService(userAlertsAction.getUserService());

    return newUserAlertsAction;
  }

  private String createUser(final String authId) throws Exception {
    final UserProfileAction createUserAction = getMockCreateUserAction(authId);
    createUserAction.setEmail("UserAlertsTest@test.com");
    createUserAction.setRealName("UserAlertsTest test com");
    createUserAction.setAuthId(authId);
    createUserAction.setDisplayName("UserAlertsTest");
    assertEquals(SUCCESS, createUserAction.executeSaveUser());
    final String topazId = createUserAction.getInternalId();
    assertNotNull(topazId);

    return topazId;
  }

  protected UserProfileAction getMockCreateUserAction(final String authId) {
    final UserProfileAction createUserAction = super.getMemberUserProfileAction();
    final UserProfileAction newCreateUserAction = new MemberUserProfileAction() {
      private Map<String,Object> mockSessionMap = createMockSessionMap(authId, null);
      protected Map<String, Object> getSessionMap() {
        return mockSessionMap;
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
