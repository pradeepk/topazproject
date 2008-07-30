/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2008 by Topaz, Inc.
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.annotation.Transactional;
import org.topazproject.ambra.ApplicationException;
import org.topazproject.ambra.user.AmbraUser;
import org.topazproject.ambra.user.service.CategoryBean;


import java.util.ArrayList;
import java.util.Collection;

/**
 * Update action for saving or getting alerts that the user subscribes to.
 */
public abstract class UserAlertsAction extends UserActionSupport {
  private String displayName;
  private String[] monthlyAlerts = new String[]{};
  private String[] weeklyAlerts = new String[]{};
  private final String MONTHLY_ALERT_SUFFIX = "_monthly";
  private final String WEEKLY_ALERT_SUFFIX = "_weekly";
  private static final Log log = LogFactory.getLog(UserAlertsAction.class);

  /**
   * Save the alerts.
   * @return webwork status
   * @throws Exception Exception
   */
  @Transactional(rollbackFor = { Throwable.class })
  public String saveAlerts() throws Exception {
    final AmbraUser ambraUser = getAmbraUserToUse();
    if (log.isDebugEnabled()) {
      if (ambraUser != null) {
        log.debug("ambrauser authID = " + ambraUser.getAuthId());
        log.debug("ambrauser email = " + ambraUser.getEmail());
        log.debug("ambrauser userID = " + ambraUser.getUserId());
      }
    }

    final Collection<String> alertsList = new ArrayList<String>();
    for (final String alert : monthlyAlerts) {
      if (log.isDebugEnabled()) {
        log.debug("found monthly alert: " + alert);
      }
      alertsList.add(alert + MONTHLY_ALERT_SUFFIX);
    }
    for (final String alert : weeklyAlerts) {
      if (log.isDebugEnabled()) {
        log.debug("found weekly alert: " + alert);
      }
      alertsList.add(alert + WEEKLY_ALERT_SUFFIX);
    }

    final String[] alerts = alertsList.toArray(new String[alertsList.size()]);
    ambraUser.setAlerts(alerts);

    getUserService().setPreferences(ambraUser);

    return SUCCESS;
  }

  /**
   * Retrieve the alerts for the logged in user
   * @return webwork status
   * @throws Exception Exception
   */
  @Transactional(readOnly = true)
  public String retrieveAlerts() throws Exception {
    final AmbraUser ambraUser = getAmbraUserToUse();
    final Collection<String> monthlyAlertsList = new ArrayList<String>();
    final Collection<String> weeklyAlertsList = new ArrayList<String>();

    final String[] alerts = ambraUser.getAlerts();

    if (log.isDebugEnabled()) {
      log.debug("ambrauser authID = " + ambraUser.getAuthId());
      log.debug("ambrauser email = " + ambraUser.getEmail());
      log.debug("ambrauser userID = " + ambraUser.getUserId());
    }

    if (null != alerts) {
      for (final String alert : alerts) {
        if (log.isDebugEnabled()) {
          log.debug("Alert: " + alert);
        }
        if (alert.endsWith(MONTHLY_ALERT_SUFFIX)) {
          monthlyAlertsList.add(alert.substring(0, alert.indexOf(MONTHLY_ALERT_SUFFIX)));
        } else if (alert.endsWith(WEEKLY_ALERT_SUFFIX)) {
          weeklyAlertsList.add(alert.substring(0, alert.indexOf(WEEKLY_ALERT_SUFFIX)));
        }
      }
    }

    monthlyAlerts = monthlyAlertsList.toArray(new String[monthlyAlertsList.size()]);
    weeklyAlerts = weeklyAlertsList.toArray(new String[weeklyAlertsList.size()]);

    setDisplayName(ambraUser.getDisplayName());
    return SUCCESS;
  }

  /**
   * Provides a way to get the AmbraUser to edit
   * @return the AmbraUser to edit
   * @throws org.topazproject.ambra.ApplicationException ApplicationException
   */
  protected abstract AmbraUser getAmbraUserToUse() throws ApplicationException;

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

  /**
   * @return all the category beans
   */
  public Collection<CategoryBean> getCategoryBeans() {
    return getUserService().getCategoryBeans();
  }

  /**
   * @return Returns the displayName.
   */
  public String getDisplayName() {
    return displayName;
  }

  /**
   * @param displayName The displayName to set.
   */
  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

}
