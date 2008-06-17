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
package org.plos.bootstrap.migration;

import java.net.URI;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.plos.configuration.ConfigurationStore;
import org.plos.configuration.WebappItqlClientFactory;

import org.topazproject.otm.OtmException;
import org.topazproject.otm.Rdf;
import org.topazproject.otm.RdfUtil;
import org.topazproject.otm.Session;
import org.topazproject.otm.SessionFactory;
import org.topazproject.otm.Transaction;
import org.topazproject.otm.impl.SessionFactoryImpl;
import org.topazproject.otm.query.Results;
import org.topazproject.otm.stores.ItqlStore;
import org.topazproject.otm.util.TransactionHelper;

/**
 * A listener class for doing migrations on startup.
 *
 * @author Pradeep Krishnan
 */
public class Migrator implements ServletContextListener {
  private static Log    log = LogFactory.getLog(Migrator.class);
  private static String RI;

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
    Session sess            = null;

    try {
      Configuration conf    = ConfigurationStore.getInstance().getConfiguration();
      URI           service = new URI(conf.getString("ambra.topaz.tripleStore.mulgara.itql.uri"));
      RI                    = conf.getString("ambra.models.ri");

      SessionFactory factory = new SessionFactoryImpl();
      factory.setTripleStore(new ItqlStore(service, new WebappItqlClientFactory()));

      sess = factory.openSession();

      Integer count =
        TransactionHelper.doInTxE(sess,
                                  new TransactionHelper.ActionE<Integer, Exception>() {
            public Integer run(Transaction tx) throws Exception {
              return migrate(tx.getSession());
            }
          });

      if (count == 0)
        log.info("Nothing to do. Everything was already migrated.");
      else
        log.warn("Committed " + count + " data-migrations.");
    } catch (Exception e) {
      throw new Error("A data-migration operation failed. Aborting ...", e);
    } finally {
      try {
        if (sess != null)
          sess.close();
      } catch (Throwable t) {
        log.warn("Error closing session", t);
      }
    }
  }

  /**
   * Apply all migrations.
   *
   * @param sess the otm session to use
   *
   * @return the number of migrations performed
   *
   * @throws OtmException on an error
   */
  public int migrate(Session sess) throws OtmException {
    log.info("Checking and performing data-migrations ...");

    return migrateReps(sess) +
           addObjInfoType(sess) +
           removeObsoleteStates(sess);
  }

  /**
   * Migrate the Article Representations.
   *
   * @param sess the otm session to use
   *
   * @return the number of migrations performed
   *
   * @throws OtmException on an error
   */
  public int migrateReps(Session sess) throws OtmException {
    final String C_T = "-contentType";
    final String O_S = "-objectSize";
    log.info("Checking if Representations need data-migration ...");

    Results                                 r       =
      sess.doNativeQuery("select $s subquery (select $p $o from <" + RI
                         + "> where $s $p $o) from <" + RI + "> where ($s <rdf:type> <" + Rdf.topaz
                         + "Article> or " + "($a <rdf:type> <" + Rdf.topaz + "Article> and $a <"
                         + Rdf.dc_terms + "hasPart> $x and $x <rdf:type> <rdf:Seq> and $x $li $s))"
                         + " minus ($s <" + Rdf.topaz
                         + "hasRepresentation> $rep and $rep <rdf:type> <" + Rdf.topaz
                         + "Representation>);");

    Map<String, Collection<Representation>> objs    =
      new HashMap<String, Collection<Representation>>();
    Set<String>                             allReps = new HashSet<String>();

    while (r.next()) {
      String                      id   = r.getString(0);
      Results                     sub  = r.getSubQueryResults(1);
      Map<String, Representation> reps = new HashMap<String, Representation>();

      while (sub.next()) {
        String p = sub.getString(0);

        if (!p.startsWith(Rdf.topaz))
          continue;

        if (p.endsWith(C_T)) {
          String rep = p.substring(Rdf.topaz.length(), p.length() - C_T.length());
          getRep(reps, rep).contentType = sub.getLiteral(1);
        } else if (p.endsWith(O_S)) {
          String rep = p.substring(Rdf.topaz.length(), p.length() - O_S.length());
          getRep(reps, rep).objectSize = sub.getLiteral(1);
        } else if (p.equals(Rdf.topaz + "hasRepresentation")) {
          getRep(reps, sub.getString(1));
        }
      }

      if (reps.size() > 0)      // happens for $li=<rdf:type> $s=<rdf:Seq>
        objs.put(id, reps.values());
      allReps.addAll(reps.keySet());
    }

    if (objs.isEmpty()) {
      log.info("Did not find any object that required data-migration for its Representations.");

      return 0;
    }

    log.info("" + objs.size() + " objects have representations that require data-migration ...");

    StringBuilder b = new StringBuilder();
    b.append("delete select $s $p $o from <" + RI + "> where $s $p $o and (");

    for (String id : objs.keySet())
      b.append("$s <mulgara:is> <" + id + "> or ");

    b.setLength(b.length() - 3);
    b.append(") and (");

    for (String rep : allReps) {
      b.append("$p <mulgara:is> <" + Rdf.topaz + rep + C_T + "> or ");
      b.append("$p <mulgara:is> <" + Rdf.topaz + rep + O_S + "> or ");
    }

    b.append("$p <mulgara:is> <" + Rdf.topaz + "hasRepresentation>)");
    b.append(" from <" + RI + ">;");

    log.warn("Deleting old representations: " + b.toString());

    sess.doNativeUpdate(b.toString());

    for (String id : objs.keySet()) {
      b = new StringBuilder();
      b.append("insert ");

      for (Representation rep : objs.get(id)) {
        String rid = id + "/" + rep.name;
        b.append("<" + id + "> <" + Rdf.topaz + "hasRepresentation> <" + rid + "> ");
        b.append("<" + rid + "> <rdf:type> <" + Rdf.topaz + "Representation> ");
        b.append("<" + rid + "> <" + Rdf.dc_terms + "identifier> '" + rep.name + "' ");

        if (rep.contentType != null) {
          b.append("<" + rid + "> <" + Rdf.topaz + "contentType> '"
                   + RdfUtil.escapeLiteral(rep.contentType.getValue()) + "'");

          if (rep.contentType.getDatatype() != null)
            b.append("^^<" + rep.contentType.getDatatype() + ">");

          b.append(" ");
        }

        if (rep.objectSize != null) {
          b.append("<" + rid + "> <" + Rdf.topaz + "objectSize> '"
                   + RdfUtil.escapeLiteral(rep.objectSize.getValue()) + "'");

          if (rep.objectSize.getDatatype() != null)
            b.append("^^<" + rep.objectSize.getDatatype() + ">");

          b.append(" ");
        }
      }

      b.append(" into <" + RI + ">;");
      log.warn("Inserting new representations for '" + id + "' : " + b.toString());
      sess.doNativeUpdate(b.toString());
    }

    log.warn("Finished data-migration of Representations. " + objs.size()
             + " ObjectInfo objects migrated.");

    return objs.size();
  }

  private Representation getRep(Map<String, Representation> reps, String rep) {
    Representation o = reps.get(rep);

    if (o == null) {
      o        = new Representation();
      o.name   = rep;
      reps.put(rep, o);
    }

    return o;
  }

  private static class Representation {
    public String          name;
    public Results.Literal contentType;
    public Results.Literal objectSize;
  }

  /**
   * Add the rdf:type for ObjectInfo's.
   *
   * @param sess the otm session to use
   * @return the number of migrations performed
   * @throws OtmException on an error
   */
  public int addObjInfoType(Session sess) throws OtmException {
    log.info("Adding rdf:type to ObjectInfo's ...");

    Results r = sess.doNativeQuery(
          "select count(select $s from <" + RI + "> where " +
          "             $s <dcterms:isPartOf> $o minus $s <rdf:type> <topaz:ObjectInfo>) " +
          "from <" + RI + "> where $dummy <mulgara:is> 'ignored';");
    r.next();
    int cnt = (int) Double.parseDouble(r.getString(0));

    if (cnt == 0) {
      log.info("Did not find any ObjectInfo that required an rdf:type to be added.");
    } else {
      sess.doNativeUpdate("insert select $s <rdf:type> <topaz:ObjectInfo> from <" + RI +
                          "> where $s <dcterms:isPartOf> $o into <" + RI + ">;");
      log.warn("Added rdf:type to " + cnt + " ObjectInfo's.");
    }

    return cnt;
  }

  /**
   * Removes articleState's on ObjectInfo's and Category's.
   *
   * @param sess the otm session to use
   * @return the number of migrations performed
   * @throws OtmException on an error
   */
  public int removeObsoleteStates(Session sess) throws OtmException {
    log.info("Removing obsolete state fields ...");

    Results r = sess.doNativeQuery(
          "select count(select $s from <" + RI + "> where " +
          "             $s <topaz:articleState> $o minus $s <rdf:type> <topaz:Article>) " +
          "from <" + RI + "> where $dummy <mulgara:is> 'ignored';");
    r.next();
    int cnt = (int) Double.parseDouble(r.getString(0));

    if (cnt == 0) {
      log.info("Did not find any objects with leftover state fields.");
    } else {
      sess.doNativeUpdate("delete select $s <topaz:articleState> $o from <" + RI +
                          "> where $s <topaz:articleState> $o minus $s <rdf:type> <topaz:Article>" +
                          " from <" + RI + ">;");
      log.warn("Removed state fields from " + cnt + " objects.");
    }

    return cnt;
  }
}
