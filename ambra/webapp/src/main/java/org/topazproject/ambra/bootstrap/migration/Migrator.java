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
package org.topazproject.ambra.bootstrap.migration;

import java.net.URI;

import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.ambra.configuration.ConfigurationStore;
import org.topazproject.ambra.configuration.WebappItqlClientFactory;
import org.topazproject.ambra.models.Ambra;

import org.topazproject.otm.OtmException;
import org.topazproject.otm.Session;
import org.topazproject.otm.SessionFactory;
import org.topazproject.otm.Transaction;
import org.topazproject.otm.impl.SessionFactoryImpl;
import org.topazproject.otm.query.Results;
import org.topazproject.otm.stores.ItqlStore;

/**
 * A listener class for doing migrations on startup.
 *
 * @author Pradeep Krishnan
 */
public class Migrator implements ServletContextListener {
  private static Log    log = LogFactory.getLog(Migrator.class);
  private static String RI  = Ambra.GRAPH_PREFIX + "ri";

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
   * @param event init event
   *
   * @throws Error to abort
   */
  public void contextInitialized(ServletContextEvent event) {
    try {
      migrate();
    } catch (Exception e) {
      throw new Error("A data-migration operation failed. Aborting ...", e);
    }
  }

  /**
   * Apply all migrations.
   *
   * @throws Exception on an error
   */
  public void migrate() throws Exception {
    Session sess            = null;
    Transaction tx          = null;

    try {
      Configuration conf    = ConfigurationStore.getInstance().getConfiguration();
      URI           service = new URI(conf.getString("ambra.topaz.tripleStore.mulgara.itql.uri"));

      log.info("Checking and performing data-migrations ...");
      SessionFactory factory = new SessionFactoryImpl();
      factory.setTripleStore(new ItqlStore(service, WebappItqlClientFactory.getInstance()));

      sess = factory.openSession();
      dropObsoleteGraphs(sess);
      tx = sess.beginTransaction(false, 60*60);
      int count = addXsdIntToTopazState(sess);
      tx.commit();
      tx = null;
      if (count == 0)
        log.info("Nothing to do. Everything was already migrated.");
      else
        log.warn("Committed " + count + " data-migrations.");
    } finally {
      try {
        if (tx != null)
          tx.rollback();
      } catch (Throwable t) {
        log.warn("Error in rollback", t);
      }
      try {
        if (sess != null)
          sess.close();
      } catch (Throwable t) {
        log.warn("Error closing session", t);
      }
    }
  }

  /**
   * Drop obsolete graphs. Ignore the exceptions as graphs might not exist.
   *
   * @param session the Topaz session
   */
  public void dropObsoleteGraphs(Session session) {
    Transaction txn = null;
    try {
      txn = session.beginTransaction();
      session.doNativeUpdate("drop <" + Ambra.GRAPH_PREFIX + "str> ;");
      txn.commit();
    } catch (OtmException e) {
      if (txn != null)
        txn.rollback();
      log.warn("Could not drop graph " + Ambra.GRAPH_PREFIX + "str", e);
    }
  }

  /**
   * Add xsd:int to topaz:state.
   *
   * @param sess the otm session to use
   * @return the number of migrations performed
   * @throws OtmException on an error
   */
  public int addXsdIntToTopazState(Session sess) throws OtmException {
    String marker = "<migrator:migrations> <method:addXsdIntToTopazState> ";
    log.info("Adding xsd:int to topaz:state fields ...");

    // FIXME: Remove the marker. Blocked on  http://mulgara.org/trac/ticket/153
    Results r = sess.doNativeQuery("select $o from <" + RI + "> where " + marker + "$o;");

    if (r.next() && "1".equals(r.getString(0))) {
     log.info("Marker statement says this migration is already done.");
     // log.info("Did not find any <topaz:state> statements without an <xsd:int> data-type.");
      return 0;
    }

    r = sess.doNativeQuery("select $s $o from <" + RI + "> where $s <topaz:state> $o;");
    Map<String, String> map = new HashMap<String, String>();  // not that many; so this is fine.
    while(r.next()) {
      Results.Literal v = r.getLiteral(1);
      if (v.getDatatype() == null)
        map.put(r.getString(0), v.getValue());
    }

    StringBuilder b = new StringBuilder(2500);
    b.append("delete ");
    for (String k : map.keySet()) {
      b.append("<" + k + "> <topaz:state> '" + map.get(k) + "' ");
      if (b.length() > 2000) {
        b.append(" from <" + RI + ">;");
        if (log.isDebugEnabled())
          log.debug(b.toString());
        sess.doNativeUpdate(b.toString());
        b.setLength(0);
        b.append("delete ");
      }
    }

    if (b.length() > 7) {
      b.append(" from <" + RI + ">;");
      if (log.isDebugEnabled())
        log.debug(b.toString());
      sess.doNativeUpdate(b.toString());
    }

    b.setLength(0);
    b.append("insert ");
    for (String k : map.keySet()) {
      b.append("<" + k + "> <topaz:state> '" + map.get(k) + "'^^<xsd:int> ");
      if (b.length() > 2000) {
        b.append(" into <" + RI + ">;");
        if (log.isDebugEnabled())
          log.debug(b.toString());
        sess.doNativeUpdate(b.toString());
        b.setLength(0);
        b.append("insert ");
      }
    }

    b.append(marker).append("'1' into <" + RI + ">;");
    if (log.isDebugEnabled())
      log.debug(b.toString());
    sess.doNativeUpdate(b.toString());

    log.warn("Added ^^<xsd:int> to " + map.size() + " <topaz:state> literals.");

    return map.size();
  }
}
