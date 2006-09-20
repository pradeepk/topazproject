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

import java.rmi.Remote;
import java.rmi.RemoteException;

/** 
 * This defines the alerts service. It provides the ability to get RSS feeds based on
 * categories (and possibly other categories). And it sends out email alerts based on
 * users preferences in kowari.
 * 
 * @author Eric Brown
 */
public interface Alerts extends Remote {
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
  public boolean sendAlerts(String endDate, int count) throws RemoteException;

  /**
   * Send all the alerts as of yesterday.
   *
   * @return true if successful
   * @throws RemoteException if there was a problem talking to the alerts service
   */
  public boolean sendAllAlerts() throws RemoteException;

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
  public void startUser(String userId, String date) throws RemoteException;

  /**
   * Start alerts for a user. Otherwise they will not start until next time alerts are run.
   *
   * @param userId is the user to start alerts on.
   * @throws RemoteException if there was a problem talking to the alerts service
   */
  public void startUser(String userId) throws RemoteException;
}
