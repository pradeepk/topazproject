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

public class UserAlertsTest extends BasePlosoneTestCase {
  public void testCreateAlerts() throws Exception {
    final UserAlertsAction alertsAction = getUserAlertsAction();
    final String[] weeklyAlertCategories = alertsAction.getAllWeeklyCategories();
    final String[] monthlyAlertCategories = alertsAction.getAllMonthlyCategories();
    alertsAction.setMonthlyAlerts(monthlyAlertCategories);
    assertEquals(SUCCESS, alertsAction.saveAlerts());
  }
}
