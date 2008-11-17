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
package org.topazproject.ambra.bootstrap;

import java.net.URI;
import java.util.Iterator;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


import org.topazproject.ambra.configuration.ConfigurationStore;
import org.topazproject.ambra.configuration.WebappItqlClientFactory;
import org.topazproject.otm.GraphConfig;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.Session;
import org.topazproject.otm.SessionFactory;
import org.topazproject.otm.Transaction;
import org.topazproject.otm.impl.SessionFactoryImpl;
import org.topazproject.otm.stores.ItqlStore;

/**
 * A listener class for web-apps to initialize things at startup.
 *
 * @author Pradeep Krishnan
 */
public class WebAppListenerInitModels implements ServletContextListener {
  private static Log log = LogFactory.getLog(WebAppListenerInitModels.class);

  /**
   * Shutdown things.
   *
   * @param event the destryed event
   */
  public void contextDestroyed(ServletContextEvent event) {
  }

  /**
   * Initialize things.
   *
   * @param event destroyed event
   */
  public void contextInitialized(ServletContextEvent event) {
    // xxx" may be this can itself be driven by config
    initGraphs();
  }

  private void initGraphs() {
    Session session = null;
    Transaction txn = null;
    try {
      Configuration conf    = ConfigurationStore.getInstance().getConfiguration();
      URI           service = new URI(conf.getString("ambra.topaz.tripleStore.mulgara.itql.uri"));

      ItqlStore     store   = new ItqlStore(service, WebappItqlClientFactory.getInstance());

      SessionFactory sessionFactory = new SessionFactoryImpl();
      sessionFactory.setTripleStore(store);
      conf                  = conf.subset("ambra.graphs");

      Iterator it           = conf.getKeys();

      while (it.hasNext()) {
        String key = (String) it.next();

        if ((key.indexOf("[") >= 0) || (key.indexOf(".") >= 0))
          continue;

        String graph  = conf.getString(key);
        String type   = conf.getString(key + "[@type]", "mulgara:Model");

        sessionFactory.addGraph(new GraphConfig(key, new URI(graph), new URI(type)));
      }


      session = sessionFactory.openSession();
      dropObsoleteGraphs(conf, session);
      txn = session.beginTransaction();
      for (GraphConfig graph: sessionFactory.listGraphs())
        session.createGraph(graph.getId());
      txn.commit();
      log.info("Successfully created all configured ITQL Graphs.");
    } catch (Exception e) {
      log.warn("bootstrap of graphs failed", e);
      if (txn != null)
        txn.rollback();
      log.error("Error creating all configured ITQL Graphs.", e);
    } finally {
      if (session != null)
        session.close();
    }

  }

  /**
   * Have to do this to deal with change from "models" to "graphs"
   * @param conf Configuration
   * @param session A writable Session to access the store
   *
   * TODO: Remove this after 0.9.2
   */
  private void dropObsoleteGraphs(Configuration conf, Session session) {
    String graphPrefix = conf.getString("ambra.topaz.tripleStore.mulgara.graphPrefix");
    Transaction txn = null;
    try {
      txn = session.beginTransaction();
      session.doNativeUpdate("drop <" + graphPrefix + "str> ;");
      txn.commit();
    } catch (OtmException e) {
      if (txn != null)
        txn.rollback();
      log.warn("Could not drop graph " + graphPrefix + "str", e);
    }
  }
}
