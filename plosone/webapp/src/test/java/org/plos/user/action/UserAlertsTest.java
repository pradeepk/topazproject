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
import org.apache.commons.lang.ArrayUtils;

public class UserAlertsTest extends BasePlosoneTestCase {
  public void testCreateAlerts() throws Exception {
    final UserAlertsAction alertsAction = getUserAlertsAction();
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
  }
}
