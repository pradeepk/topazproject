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

import org.apache.commons.lang.ArrayUtils;
import static org.plos.Constants.PLOS_ONE_USER_KEY;
import org.plos.user.PlosOneUser;
import org.topazproject.ws.pap.UserPreference;

/**
 * Update action for saving or getting alerts that the user subscribes to.
 */
public class UserAlertsAction extends UserActionSupport {
  private String[] monthlyAlerts;
  /**
   * Save the alerts.
   * @return webwork status
   * @throws Exception Exception
   */
  public String saveAlerts() throws Exception {
    final PlosOneUser plosOneUser = (PlosOneUser) getSessionMap().get(PLOS_ONE_USER_KEY);
    plosOneUser.setUserPrefs(getUserPreferences());
    getUserService().setPreferences(plosOneUser);
    return SUCCESS;
  }

  private UserPreference[] getUserPreferences() {
    return new UserPreference[0];
  }

  /**
   * @return categories that have monthly alerts
   */
  public String[] getMonthlyAlerts() {
    return monthlyAlerts;
  }

  /**
   * Set the categories that have monthly alerts
   * @param monthlyAlerts monthlyAlerts
   */
  public void setMonthlyAlerts(final String[] monthlyAlerts) {
    this.monthlyAlerts = monthlyAlerts;
  }

  /**
   * @return all categories
   */
  public String[] getAllCategories() {
    return new String[]{
                "plosone",
                "biology",
                "clinical_trials",
                "computational_biology",
                "genetics",
                "medicine",
                "pathogens"
          };
  }

  /**
   * @return all the weekly categories
   */
  public String[] getAllWeeklyCategories() {
    return getAllCategories();
  }

  public String[] getAllMonthlyCategories() {
    return (String[]) ArrayUtils.removeElement(getAllCategories(), "plosone");
  }
}
