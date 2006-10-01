/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.common.impl;

/**
 * The listener for TopazContext events.
 *
 * @author Pradeep Krishnan
 */
public interface TopazContextListener {
  /**
   * An event indicating a new hanlde (itql, apim etc.) created.
   *
   * @param context the context that is reporting this event
   * @param handle the handle that was created
   */
  public void handleCreated(TopazContext context, Object handle);
}
