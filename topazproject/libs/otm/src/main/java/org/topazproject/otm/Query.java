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

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.topazproject.otm.query.GenericQueryImpl;
import org.topazproject.otm.query.Results;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/** 
 * This represents an OQL query. Instances are obtained via {@link Session#createQuery
 * Session.createQuery()}.
 * 
 * @author Ronald Tschalär
 */
public class Query implements Parameterizable<Query> {
  private static final Log log = LogFactory.getLog(Query.class);

  private final Session            sess;
  private final GenericQueryImpl   query;
  private final Collection<Filter> filters;
  private final Map                paramValues = new HashMap<String, Object>();

  /** 
   * Create a new query instance. 
   * 
   * @param sess    the session this is attached to
   * @param query   the oql query string
   * @param filters the filters that should be applied to this query
   */
  Query(Session sess, String query, Collection<Filter> filters) throws OtmException {
    this.sess    = sess;
    this.filters = filters;

    this.query = new GenericQueryImpl(query, log);
    this.query.prepareQuery(sess.getSessionFactory());
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

    query.applyParameterValues(paramValues);
    return store.doQuery(query, sess.getTransaction());
  }

  public Set<String> getParameterNames() {
    return Collections.unmodifiableSet(query.getParameterNames());
  }

  public Query setParameter(String name, Object val) {
    paramValues.put(name, val);
    return this;
  }

  public Query setUri(String name, URI val) {
    paramValues.put(name, val);
    return this;
  }

  public Query setPlainLiteral(String name, String val, String lang) {
    paramValues.put(name, new Results.Literal(val, lang, null));
    return this;
  }

  public Query setTypedLiteral(String name, String val, URI dataType) {
    paramValues.put(name, new Results.Literal(val, null, dataType));
    return this;
  }
}
