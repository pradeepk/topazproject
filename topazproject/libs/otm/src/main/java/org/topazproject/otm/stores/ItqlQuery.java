/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.topazproject.otm.stores;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.otm.OtmException;
import org.topazproject.otm.Session;
import org.topazproject.otm.query.ErrorCollector;
import org.topazproject.otm.query.GenericQueryImpl;
import org.topazproject.otm.query.ItqlConstraintGenerator;
import org.topazproject.otm.query.ItqlRedux;
import org.topazproject.otm.query.ItqlWriter;
import org.topazproject.otm.query.QueryException;
import org.topazproject.otm.query.QueryImplBase;
import org.topazproject.otm.query.QueryInfo;

/** 
 * Common code for query handlers.
 * 
 * @author Ronald Tschalär
 */
class ItqlQuery extends QueryImplBase {
  private static final Log log = LogFactory.getLog(ItqlQuery.class);
  private final GenericQueryImpl   query;
  private final Session            sess;

  /** 
   * Create a new itql-query instance. 
   */
  public ItqlQuery(GenericQueryImpl query, Session sess) {
    this.query   = query;
    this.sess    = sess;

    warnings.addAll(query.getWarnings());
  }

  public QueryInfo parseItqlQuery() throws OtmException {
    if (log.isDebugEnabled())
      log.debug("parsing query '" + query + "'");

    ErrorCollector curParser = null;

    try {
      ItqlConstraintGenerator cg = new ItqlConstraintGenerator(sess);
      curParser = cg;
      cg.query(query.getResolvedQuery());
      checkMessages(cg.getErrors(), cg.getWarnings(), query);

      ItqlRedux ir = new ItqlRedux();
      curParser = ir;
      ir.query(cg.getAST());
      checkMessages(ir.getErrors(), ir.getWarnings(), query);

      ItqlWriter wr = new ItqlWriter();
      curParser = wr;
      QueryInfo qi = wr.query(ir.getAST());
      checkMessages(wr.getErrors(), wr.getWarnings(), query);

      if (log.isDebugEnabled())
        log.debug("parsed query '" + query + "': itql='" + qi.getQuery() + "', vars='" +
                  qi.getVars() + "', types='" + qi.getTypes() + "'");

      return qi;
    } catch (OtmException oe) {
      throw oe;
    } catch (Exception e) {
      if (curParser != null && curParser.getErrors().size() > 0) {
        // exceptions are usually the result of aborted parsing due to errors
        log.debug("error parsing query: " + curParser.getErrors(null), e);
        throw new QueryException("error parsing query '" + query + "'", curParser.getErrors());
      } else
        throw new QueryException("error parsing query '" + query + "'", e);
    }
  }
}
