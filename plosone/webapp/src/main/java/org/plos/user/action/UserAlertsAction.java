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

import static org.plos.Constants.PLOS_ONE_USER_KEY;
import org.plos.user.PlosOneUser;
import org.topazproject.ws.pap.UserPreference;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Update action for saving or getting alerts that the user subscribes to.
 */
public class UserAlertsAction extends UserActionSupport {
  private String[] monthlyAlerts;
  private String[] weeklyAlerts;
  private final String MONTHLY_ALERT_SUFFIX = "_monthly";
  private final String WEEKLY_ALERT_SUFFIX = "_weekly";

  /**
   * Save the alerts.
   * @return webwork status
   * @throws Exception Exception
   */
  public String saveAlerts() throws Exception {
    final PlosOneUser plosOneUser = (PlosOneUser) getSessionMap().get(PLOS_ONE_USER_KEY);
    final Collection<String> alertsList = new ArrayList<String>();
    for (final String alert : monthlyAlerts) {
      alertsList.add(alert + MONTHLY_ALERT_SUFFIX);
    }
    for (final String alert : weeklyAlerts) {
      alertsList.add(alert + WEEKLY_ALERT_SUFFIX);
    }

    final String[] alerts = alertsList.toArray(new String[alertsList.size()]);
    plosOneUser.setAlerts(alerts);
    //TODO: What does the following line do?
    plosOneUser.setUserPrefs(getUserPreferences());
    getUserService().setPreferences(plosOneUser);
    return SUCCESS;
  }

  /**
   * Retrieve the alerts for the logged in user
   * @return webwork status
   * @throws Exception Exception
   */
  public String retrieveAlerts() throws Exception {
    final PlosOneUser plosOneUser = (PlosOneUser) getSessionMap().get(PLOS_ONE_USER_KEY);
    final String[] alerts = plosOneUser.getAlerts();

    final Collection<String> monthlyAlertsList = new ArrayList<String>();
    final Collection<String> weeklyAlertsList = new ArrayList<String>();
    for (final String alert : alerts) {
      if (alert.endsWith(MONTHLY_ALERT_SUFFIX)) {
        monthlyAlertsList.add(alert);
      }
      if (alert.endsWith(WEEKLY_ALERT_SUFFIX)) {
        weeklyAlertsList.add(alert);
      }
    }

    monthlyAlerts = (String[]) monthlyAlertsList.toArray();
    weeklyAlerts = (String[]) weeklyAlertsList.toArray();
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
   * @return weekly alert categories
   */
  public String[] getWeeklyAlerts() {
    return weeklyAlerts;
  }

  /**
   * Set weekly alert categories
   * @param weeklyAlerts weeklyAlerts
   */
  public void setWeeklyAlerts(String[] weeklyAlerts) {
    this.weeklyAlerts = weeklyAlerts;
  }
}
