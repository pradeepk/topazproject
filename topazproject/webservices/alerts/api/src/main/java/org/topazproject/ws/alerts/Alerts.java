/* $HeadURL::                                                                             $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.topazproject.ws.alerts;

import java.rmi.RemoteException;
import java.util.Calendar;

/** 
 * This defines the alerts service. It provides the ability to get RSS feeds based on
 * categories (and possibly other categories). And it sends out email alerts based on
 * users preferences in kowari.
 * 
 * @author Eric Brown
 */
public interface Alerts {
  /**
   * Get list of articles for a given set of categories or authors bracked by specified
   * times. List is returned as an XML string of the following format:
   * <pre>
   *   <articles>
   *     <article>
   *       <doi>...</doi>
   *       <title>...</title>
   *       <description>...</description>
   *       <date>YYY-MM-DD</date>
   *       <authors>
   *         <author>...</author>
   *         ...
   *       </authors>
   *       <categories>
   *         <category>...</category>
   *         ...
   * </pre>
   *
   * @param startDate is the date to start searching from. If empty, start from begining of time
   * @param endDate is the date to search until. If empty, search until prsent date
   * @param categories is list of categories to search for articles within (all categories if empty)
   * @param authors is list of authors to search for articles within (all authors if empty)
   * @return the xml for the specified feed
   * @throws RemoteException if there was a problem talking to the alerts service
   */
  public String getFeed(String startDate, String endDate, String[] categories, String[] authors)
      throws RemoteException;

  /**
   * Send up to count alerts up to the date specified.
   *
   * This is primarily for testing.
   *
   * @param endDate is the date to send the last alert for
   * @param count is the maximum number of alerts to send. 0 to send all
   * @return true if successful
   * @throws RemoteException if there was a problem talking to the alerts service
   */
  public boolean sendAlerts(Calendar endDate, int count);

  /**
   * Send all the alerts as of yesterday.
   *
   * @return true if successful
   * @throws RemoteException if there was a problem talking to the alerts service
   */
  public boolean sendAllAlerts();

  /**
   * Clean up if a user is removed.
   *
   * @param userId is the user to clean up after.
   * @throws RemoteException if there was a problem talking to the alerts service
   */
  public void clearUser(String userId) throws RemoteException;

  /**
   * Start alerts for a user. Otherwise it will not start until next time alerts are run.
   *
   * @param userId is the user to start alerts on.
   * @param date is the date to start alerts as of.
   * @throws RemoteException if there was a problem talking to the alerts service
   */
  public void startUser(String userId, Calendar date) throws RemoteException;

  /**
   * Start alerts for a user. Otherwise they will not start until next time alerts are run.
   *
   * @param userId is the user to start alerts on.
   * @throws RemoteException if there was a problem talking to the alerts service
   */
  public void startUser(String userId) throws RemoteException;
}
