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
   * @throws RemoteException if there was a problem talking to the alerts service
   */
  public String getFeed(String startDate, String endDate, String[] categories, String[] authors)
      throws RemoteException;
}
