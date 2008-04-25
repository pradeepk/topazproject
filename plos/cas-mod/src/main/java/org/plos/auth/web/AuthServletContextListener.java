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
package org.plos.auth.web;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.auth.AuthConstants;
import org.plos.auth.db.DatabaseException;
import org.plos.auth.db.DatabaseContext;
import org.plos.auth.service.UserService;

import org.apache.commons.configuration.Configuration;
import org.plos.configuration.ConfigurationStore;

import javax.servlet.ServletContextListener;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContext;
import java.util.Properties;

/**
 * Initialize the DatabaseContext and UserService for cas.<p>
 *
 * Be sure to add to CAS' web.xml as a servlet context listner. Uses commons-config
 * for configuration.
 *
 * @author Viru
 * @author Eric Brown
 */
public class AuthServletContextListener implements ServletContextListener {
  private DatabaseContext dbContext;
  private static final Log log = LogFactory.getLog(AuthServletContextListener.class);

  public void contextInitialized(final ServletContextEvent event) {
    final ServletContext context = event.getServletContext();

    Configuration conf = ConfigurationStore.getInstance().getConfiguration();
    String url = conf.getString("ambra.services.cas.db.url");

    final Properties dbProperties = new Properties();
    dbProperties.setProperty("url", url);
    dbProperties.setProperty("user", conf.getString("ambra.services.cas.db.user"));
    dbProperties.setProperty("password", conf.getString("ambra.services.cas.db.password"));

    try {
      dbContext = DatabaseContext.createDatabaseContext(
              conf.getString("ambra.services.cas.db.driver"),
              dbProperties,
              conf.getInt("ambra.services.cas.db.initialSize"),
              conf.getInt("ambra.services.cas.db.maxActive"),
              conf.getString("ambra.services.cas.db.connectionValidationQuery"));
    } catch (final DatabaseException ex) {
      throw new Error("Failed to initialize the database context to '" + url + "'", ex);
    }

    final UserService userService = new UserService(
                                          dbContext,
                                          conf.getString("ambra.services.cas.db.usernameToGuidSql"),
                                          conf.getString("ambra.services.cas.db.guidToUsernameSql"));

    context.setAttribute(AuthConstants.USER_SERVICE, userService);
  }

  public void contextDestroyed(final ServletContextEvent event) {
    try {
      dbContext.close();
    } catch (final DatabaseException ex) {
      log.error("Failed to shutdown the database context", ex);
    }
  }
}
