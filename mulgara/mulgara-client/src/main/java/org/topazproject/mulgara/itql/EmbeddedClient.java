/* $HeadURL::                                                                             $
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

package org.topazproject.mulgara.itql;

import java.io.File;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.apache.commons.io.FileDeleteStrategy;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FileCleaningTracker;

import org.mulgara.config.MulgaraConfig;
import org.mulgara.connection.Connection;
import org.mulgara.connection.SessionConnection;
import org.mulgara.query.QueryException;
import org.mulgara.resolver.Database;
import org.mulgara.server.SessionFactory;

/**
 * A mulgara client to an embedded mulgara instance. A separate embedded mulgara instance is
 * created for each unique server-URI; mulgara instances are shared across ItqlClientFactory
 * instances, though.
 *
 * @author Ronald Tschalär
 */
public class EmbeddedClient extends TIClient {
  private static final Map<ItqlClientFactory, Map<URI, SessionFactory>> allFactories =
                                    new WeakHashMap<ItqlClientFactory, Map<URI, SessionFactory>>();
  private static final Set<File> tempFiles = new HashSet<File>();
  private static final Thread    shutdownHook;
  private static final FileCleaningTracker fileCleaningTracker = new FileCleaningTracker();

  static {
    shutdownHook = new Thread() {
      public void run() {
        // close all session factories
        synchronized (EmbeddedClient.class) {
          /*
            Note: need to make a copy of the values here so we don't get values removed
            from out under us; we also need to make sure the copy is "atomic"
          */
          for (Map<URI, SessionFactory> fl : allFactories.values().toArray(new Map[0])) {
            for (SessionFactory sf : fl.values()) {
              try {
                sf.close();
              } catch (Exception e) {
                System.err.println("Error closing session-factory '" + sf + "': " + e);
              }
            }
          }
          allFactories.clear();

          // delete all temporary db's
          for (File f : tempFiles) {
            try {
              if (f.exists())
                FileUtils.forceDelete(f);
            } catch (Exception e) {
              System.err.println("Error deleting '" + f + "': " + e);
            }
          }
          tempFiles.clear();
        }
      }
    };

    Runtime.getRuntime().addShutdownHook(shutdownHook);
  }

  /**
   * Release all static resources associated with this class. This is meant to be used in an
   * environment where classes are reloaded in order to make sure all references to this class
   * or to related classes are removed. Note that this closes all session-factories, so the
   * caller must ensure they will not be needed anymore.
   */
  public static void releaseResources() {
    Runtime.getRuntime().removeShutdownHook(shutdownHook);
    shutdownHook.run();
    fileCleaningTracker.exitWhenFinished();
  }

  /**
   * Create a new instance pointed at the given database. If no database instance exists for the
   * given URI then a new instance is created; otherwise the existing one is used.
   *
   * @param uri   the database uri
   * @param dbDir the directory for the database; if null, a temporary directory is used. May not
   *              be used if the database is in-memory only
   * @param conf  the mulgara configuration to use (must point to a MulgaraConfig xml doc)
   * @param icf   the client-factory instance creating this
   * @throws QueryException if an error occurred setting up the connector
   */
  public EmbeddedClient(URI uri, String dbDir, URL conf, ItqlClientFactory icf)
      throws QueryException {
    super(getCon(uri, dbDir, conf, icf));
  }

  private static synchronized Connection getCon(URI uri, String dbDir, URL conf,
                                                ItqlClientFactory icf) throws QueryException {
    SessionFactory sf = findSessionFactory(uri, icf);
    if (sf == null) {
      sf = newSessionFactory(uri, dbDir, conf);
      getFactories(icf).put(uri, sf);
    }

    return new SessionConnection(sf.newSession(), sf.getSecurityDomain());
  }

  private static SessionFactory findSessionFactory(URI uri, ItqlClientFactory icf) {
    // see if we have it in our factories
    Map<URI, SessionFactory> f = allFactories.get(icf);
    if (f != null && f.containsKey(uri))
      return f.get(uri);

    // nope, so check the others (this makes instances shareable throughout the JVM
    for (Map<URI, SessionFactory> fl : allFactories.values()) {
      SessionFactory sf = fl.get(uri);
      if (sf != null) {
        getFactories(icf).put(uri, sf); // make sure we have a ref to it too
        return sf;
      }
    }

    // never seen this uri
    return null;
  }

  private static Map<URI, SessionFactory> getFactories(ItqlClientFactory icf) {
    Map<URI, SessionFactory> f = allFactories.get(icf);
    if (f == null)
      allFactories.put(icf, f = new HashMap<URI, SessionFactory>());
    return f;
  }

  private static SessionFactory newSessionFactory(URI uri, String dbDir, URL conf)
      throws QueryException {
    File dir = createDir(dbDir);
    SessionFactory sf = newSessionFactory(uri, dir, conf);

    if (dbDir == null) {
      fileCleaningTracker.track(dir, sf, FileDeleteStrategy.FORCE);
      tempFiles.add(dir);
    }

    return sf;
  }

  private static SessionFactory newSessionFactory(URI uri, File dir, URL conf)
      throws QueryException {
    /* We don't use SessionFactoryFinder and LocalSessionFactory here for a couple reasons.
     * First, SessionFactoryFinder doesn't give us a way to properly specify our own config.
     * Second, LocalSessionFactory uses a static variable to hold the underlying session factory,
     * which means we can't create multiple instances.
     */
    try {
      MulgaraConfig mc = MulgaraConfig.unmarshal(new InputStreamReader(conf.openStream()));
      return new Database(uri, dir, mc);
    } catch (QueryException qe) {
      throw qe;
    } catch (Exception e) {
      throw new QueryException("Error creating Database instance", e);
    }
  }

  private static File createDir(String dir) {
    File d;
    if (dir != null) {
      d = new File(dir);
      if (d.exists()) {
        if (!d.isDirectory())
          throw new RuntimeException("Database directory exists but is not a directory: '" +
                                     d + "'");
      } else {
        if (!d.mkdirs())
          throw new RuntimeException("Failed to create database directory '" + d + "'");
      }
    } else {
      try {
        d = File.createTempFile("mulgara_", "");
      } catch (IOException ioe) {
        throw new RuntimeException("Error creating temporary directory", ioe);
      }
      if (!d.delete())
        throw new RuntimeException("Error creating temporary directory: could not delete '" +
                                   d + "'");
      if (!d.mkdirs())
        throw new RuntimeException("Failed to create temporary directory '" + d + "'");
    }

    return d;
  }
}
