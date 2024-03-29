/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007-2009 by Topaz, Inc.
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

package org.topazproject.otm;

import org.topazproject.mulgara.itql.DefaultItqlClientFactory;
import org.topazproject.otm.metadata.RdfBuilder;
import org.topazproject.otm.stores.ItqlStore;
import org.topazproject.otm.stores.SimpleBlobStore;
import org.topazproject.otm.Session;
import org.topazproject.otm.impl.SessionFactoryImpl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.topazproject.otm.impl.btm.TransactionManagerHelper;

/**
 * Common superclass for integration tests.
 */
public abstract class AbstractTest extends GroovyTestCase {
  private static final Log log = LogFactory.getLog(AbstractTest.class);

  def rdf;
  def store;
  def blobStore;

  protected def graphs = [['ri', 'otmtest1', null]];

  void setUp() {
    store = new ItqlStore("local:///topazproject".toURI(),
        new DefaultItqlClientFactory(dbDir: "target/mulgara-db"))
    blobStore = new SimpleBlobStore("target/blob-store");

    rdf = new RdfBuilder(
        sessFactory: new SessionFactoryImpl(tripleStore: store, blobStore: blobStore),
        defGraph: 'ri',
        defUriPrefix: 'topaz:')

    for (c in graphs) {
      def g = new GraphConfig(c[0], "local:///topazproject#${c[1]}".toURI(), c[2])
      rdf.sessFactory.addGraph(g)
      try { doInTx(true) {s -> s.dropGraph(g.getId()) } } catch (OtmException e) { }
      doInTx {s -> s.createGraph(g.getId()) }
    }
  }

  protected def doInTx(Closure c) {
    return doInTx(false, c)
  }

  protected def doInTx(boolean quiet, Closure c) {
    Session s = rdf.sessFactory.openSession()
    s.beginTransaction()
    try {
      def r = c(s)
      s.transaction.commit()
      return r
    } catch (OtmException e) {
      try {
        s.transaction.rollback()
      } catch (OtmException oe) {
        log.warn("rollback failed", oe);
      }

      if (!quiet)
        log.error("error: ${e}", e)

      throw e
    } finally {
      try {
        s.close();
      } catch (OtmException oe) {
        log.warn("close failed", oe);
      }
    }
  }
}
