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

import java.net.URI;

/**
 * A simple factory for {@link ItqlClient ItqlClient} instances.
 *
 * @author Ronald Tschalär
 */
public class ItqlClientFactory {
  /** 
   * Create a new itql-client instance. Uri schemes are currently mapped as follows:
   * <dl>
   *   <dt>rmi</dt>
   *   <dd>Use RMI</dd>
   *   <dt>http</dt>
   *   <dd>Use SOAP over http (deprecated - may be changed to use a REST API in the future)</dd>
   *   <dt>soap</dt>
   *   <dd>Use SOAP over http</dd>
   *   <dt>local</dt>
   *   <dd>Create embedded mulgara instance</dd>
   * </dl>
   * 
   * @param uri  the server's URI
   * @return the new client
   * @throws Exception 
   */
  public ItqlClient createClient(URI uri) throws Exception {
    String scheme = uri.getScheme();
    if (scheme.equals("rmi"))
      return new RmiClient(uri.toString());
    if (scheme.equals("http"))
      return new SoapClient(uri);
    if (scheme.equals("soap"))
      return new SoapClient(new URI("http", uri.getSchemeSpecificPart(), uri.getFragment()));
    if (scheme.equals("local"))
      return new EmbeddedClient(null, uri);

    throw new IllegalArgumentException("Unsupported scheme '" + scheme + "' from '" + uri + "'");
  }
}
