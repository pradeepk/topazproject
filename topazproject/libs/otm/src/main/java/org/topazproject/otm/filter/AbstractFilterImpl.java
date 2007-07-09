/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.topazproject.otm.filter;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.topazproject.otm.Criteria;
import org.topazproject.otm.Filter;
import org.topazproject.otm.Session;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.query.GenericQueryImpl;
import org.topazproject.otm.query.Results;

/**
 * This defines the internal filter-impl interface and implements some common code for all filters.
 *
 * @author Ronald Tschalär
 */
public abstract class AbstractFilterImpl implements Filter {
  protected final FilterDefinition fd;
  protected final Session          sess;
  protected final Map              paramValues = new HashMap<String, Object>();

  protected AbstractFilterImpl(FilterDefinition fd, Session sess) throws OtmException {
    this.fd   = fd;
    this.sess = sess;
  }

  public FilterDefinition getFilterDefinition() {
    return fd;
  }

  public String getName() {
    return fd.getFilterName();
  }

  public Set<String> getParameterNames() {
    return fd.getParameterNames();
  }

  public Filter setParameter(String name, Object val) {
    paramValues.put(name, val);
    return this;
  }

  public Filter setUri(String name, URI val) {
    paramValues.put(name, val);
    return this;
  }

  public Filter setPlainLiteral(String name, String val, String lang) {
    paramValues.put(name, new Results.Literal(val, lang, null));
    return this;
  }

  public Filter setTypedLiteral(String name, String val, URI dataType) {
    paramValues.put(name, new Results.Literal(val, null, dataType));
    return this;
  }

  /** 
   * Get the filter as a Critieria.
   * 
   * @return the criteria representing this filter
   */
  public abstract Criteria getCriteria() throws OtmException;

  /** 
   * Get the filter as an OQL query.
   * 
   * @return the query representing this filter
   */
  public abstract GenericQueryImpl getQuery() throws OtmException;
}
