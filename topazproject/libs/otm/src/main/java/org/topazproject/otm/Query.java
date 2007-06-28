/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.topazproject.otm;

import java.util.Collection;

import org.topazproject.otm.query.Results;

/** 
 * This represents an OQL query. Instances are obtained via {@link Session#createQuery
 * Session.createQuery()}.
 * 
 * @author Ronald Tschalär
 */
public class Query {
  private final Session            sess;
  private final String             query;
  private final Collection<Filter> filters;

  /** 
   * Create a new query instance. 
   * 
   * @param sess    the session this is attached to
   * @param query   the oql query string
   * @param filters the filters that should be applied to this query
   */
  Query(Session sess, String query, Collection<Filter> filters) {
    this.sess    = sess;
    this.query   = query;
    this.filters = filters;
  }

  /** 
   * Execute this query. 
   * 
   * @return the query results
   * @throws OtmException
   */
  public Results execute() throws OtmException {
    if (sess.getTransaction() == null)
      throw new OtmException("No transaction active");

    sess.flush(); // so that mods are visible to queries

    TripleStore store = sess.getSessionFactory().getTripleStore();

    return store.doQuery(query, sess.getTransaction());
  }
}
