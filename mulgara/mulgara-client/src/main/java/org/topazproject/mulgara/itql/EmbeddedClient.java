/* $HeadURL::                                                                             $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.topazproject.mulgara.itql;

import java.io.File;
import java.net.URI;

import org.mulgara.itql.ItqlInterpreterBean;
import org.mulgara.query.QueryException;
import org.mulgara.server.NonRemoteSessionException;
import org.mulgara.server.SessionFactory;
import org.mulgara.server.driver.SessionFactoryFinder;
import org.mulgara.server.driver.SessionFactoryFinderException;
import org.mulgara.server.local.LocalSessionFactory;

/** 
 * A mulgara client to an embedded mulgara instance.
 *
 * @author Ronald Tschalär
 */
class EmbeddedClient extends IIBClient {
  /** 
   * Create a new instance pointed at the given database.
   * 
   * @param dbDir   the directory for the database
   * @param uriPath the database uri (for models etc); must be local:...
   */
  public EmbeddedClient(String dbDir, URI uri) {
    super(getIIB(dbDir, uri));
  }

  private static ItqlInterpreterBean getIIB(String dbDir, URI uri) {
    try {
      SessionFactory sf = SessionFactoryFinder.newSessionFactory(uri);
      if (dbDir != null)
        ((LocalSessionFactory) sf).setDirectory(new File(dbDir));

      return new ItqlInterpreterBean(sf.newSession(), sf.getSecurityDomain());
    } catch (QueryException qe) {
      throw new RuntimeException(qe);
    } catch (SessionFactoryFinderException sffe) {
      throw new RuntimeException(sffe);
    } catch (NonRemoteSessionException nrse) {
      throw new RuntimeException(nrse);
    }
  }
}
