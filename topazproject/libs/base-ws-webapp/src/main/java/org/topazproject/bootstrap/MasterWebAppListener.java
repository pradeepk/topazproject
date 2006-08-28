/* $HeadURL::                                                                                     $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.bootstrap;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.configuration.ConfigurationStore;

/**
 * A listener class for web-apps to initialize things at startup.
 *
 * This class will call other ServletContextListeners configured.
 *
 * @author Eric Brown
 */
public class MasterWebAppListener implements ServletContextListener {
  private static Log log = LogFactory.getLog(MasterWebAppListener.class);

  /**
   * Shutdown things.
   *
   * @param event the destryed event
   */
  public void contextDestroyed(ServletContextEvent event) {
    call("contextDestroyed", event);
  }

  /**
   * Initialize things.
   *
   * @param event destroyed event
   */
  public void contextInitialized(ServletContextEvent event) {
    call("contextInitialized", event);
  }

  private void call(String methodName, ServletContextEvent event) {
    try {
      Configuration conf = ConfigurationStore.getInstance().getConfiguration();
      List listeners = conf.getList("topaz.life-cycle-listeners.listener");
      Iterator it = listeners.iterator();
      while (it.hasNext()) {
        String listenerName = (String) it.next();
        try {
          Class theClass = Class.forName(listenerName);
          ServletContextListener listener = (ServletContextListener) theClass.newInstance();
          Method method = theClass.getMethod(methodName, new Class[] {ServletContextEvent.class});
          method.invoke(listener, new Object[] {event});
          log.info("Called " + methodName + " on " + listenerName);
        } catch (Exception e) {
          log.warn("Error calling " + methodName + " on " + listenerName, e);
        }
      }
      log.info("Called " + methodName + " on topaz life-cycle-listeners");
    } catch (Exception e) {
      log.warn("Error calling " + methodName + " on topaz life-cycle-listeners", e);
    }
  }
}
