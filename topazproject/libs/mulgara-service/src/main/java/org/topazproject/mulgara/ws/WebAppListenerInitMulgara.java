/* $HeadURL::                                                                                     $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.topazproject.mulgara.ws;

import java.io.File;
import java.net.URL;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.MalformedURLException;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.commons.configuration.ConfigurationFactory;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;

import org.kowari.query.QueryException;
import org.kowari.server.NonRemoteSessionException;
import org.kowari.server.SessionFactory;
import org.kowari.server.driver.SessionFactoryFinder;
import org.kowari.server.driver.SessionFactoryFinderException;
import org.kowari.server.local.LocalSessionFactory;

/**
 * A ServletContextListener that we use to initialize kowari/mulgara.
 *
 * @author Eric Brown
 */
public class WebAppListenerInitMulgara implements ServletContextListener {
  private static Log log = LogFactory.getLog(WebAppListenerInitMulgara.class);

  private static SessionFactory sessionFactory;

  /**
   * A property used to define the <code>ConfigurationFactory</code> configuration 
   * <code>URL</code>.
   */
  public static final String CONFIG_FACTORY_PROPERTY_NAME = "org.topazproject.mulgara.configuration";

  /**
   * Initialize Mulgara.
   *
   * @param event context for initialization
   */
  public void contextInitialized(ServletContextEvent event) {
    try {
      ServletContext context = event.getServletContext();
      Configuration config = initWebAppCommonsConfig(context, CONFIG_FACTORY_PROPERTY_NAME);

      // Create directories
      File serverDir = new File(context.getRealPath(config.getString("serverDir")));
      if (serverDir.exists()) {
        if (!serverDir.isDirectory()) {
          log.error("Mulgara directory '" + serverDir + "' is not a directory");
          return;
        }
      } else if (!serverDir.mkdirs()) {
        log.error("Unable to create Mulgara directory '" + serverDir + "'");
        return;
      }
      log.info("Mulgara directory: " + serverDir);

      // Create the database session
      String serverUri = config.getString("serverUri");
      sessionFactory = SessionFactoryFinder.newSessionFactory(new URI(serverUri));
      ((LocalSessionFactory) sessionFactory).setDirectory(serverDir);

      log.info("Mulgara Initialized - Mulgara URI is '" + serverUri + "'");
    } catch (URISyntaxException use) {
      log.error("Unable to initialize Mulgara - Illegal server URI", use);
    } catch (ConfigurationException ce) {
      log.error("Unable to initialize Mulgara - Unable to load configuration", ce);
    } catch (SessionFactoryFinderException sffe) {
      log.error("Error initializing Mulgara", sffe);
    } catch (NonRemoteSessionException nrse) {
      log.error("Unable to initialize Mulgara", nrse);
    }
  }

  /**
   * Shutdown things.
   *
   * @param event the destroyed event
   */
  public void contextDestroyed(ServletContextEvent event) {
    if (sessionFactory != null) {
      log.info("Shutting down mulgara");

      try {
        sessionFactory.close();
      } catch (QueryException qe) {
        log.error("Error closing mulgara session factory", qe);
      }
      sessionFactory = null;
    }
  }

  /** 
   * Get the session factory to use. 
   * 
   * @return the session factory, or null if the server's still being initialized or being shut down
   */
  public static SessionFactory getSessionFactory() {
    return sessionFactory;
  }

  /**
   * Initialize commons-configuration from within a web-application. The commons-configuration
   * itself is configured from an xml configuration file. This method attempts to locate
   * that file by building a <code>URL</code>. It can be defined as a System property (eg.
   * <code>-Dorg.x.configuration=file:///etc/x/config.xml</code>) or as a web application
   * context initialization parameter. If specified in both places, then the System property
   * will take precedence over the context initialization parameter. If a property is not
   * present or propertyName is null, configuration will be read from
   * <code>/WEB-INF/config.xml</code>.
   * <p>
   * If the value of the propertyName starts with a '/', then it is assumed to be relative to
   * the web application context. Otherwise it must be a valid <code>URL</code>.</p>
   *
   * @param context the context of the servlet container.
   * @param propertyName the name of the system or webapp context initialization parameter.
   * @throws ConfigurationException if there is a problem initalizing commons-configuration.
   * @return a valid Configuration object
   * @see org.apache.commons.configuration.ConfigurationFactory for details on how to configure.
   */
  private Configuration initWebAppCommonsConfig(ServletContext context, String propertyName)
      throws ConfigurationException {
    String name = null;

    if (propertyName != null) {
      name = System.getProperty(propertyName); // System property
      if (name == null)
        name = context.getInitParameter(propertyName); // Configured in web.xml
    }
    if (name == null)
      name = "/WEB-INF/config.xml"; // Default

    URL url;
    try {
      if (!name.startsWith("/"))
        url = new URL(name);
      else {
        url = context.getResource(name);
        if (url == null)
          throw new ConfigurationException("'" + name + "' not found in the web-app context");
      }
    } catch (MalformedURLException mue) {
      throw new ConfigurationException("Unable to load configuration from " + name, mue);
    }

    log.info("Configuration-commons configured from " + url);

    ConfigurationFactory factory = new ConfigurationFactory();
    factory.setConfigurationURL(url);
    return factory.getConfiguration();
  }
}
