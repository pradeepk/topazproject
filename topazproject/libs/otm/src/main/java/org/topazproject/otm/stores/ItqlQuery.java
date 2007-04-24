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
import org.topazproject.otm.query.ItqlConstraintGenerator;
import org.topazproject.otm.query.ItqlRedux;
import org.topazproject.otm.query.ItqlWriter;
import org.topazproject.otm.query.QueryException;
import org.topazproject.otm.query.QueryInfo;

import antlr.ANTLRException;
import antlr.collections.AST;

/** 
 * Common code for query handlers.
 * 
 * @author Ronald Tschalär
 */
class ItqlQuery extends AbstractQuery {
  private static final Log log = LogFactory.getLog(ItqlQuery.class);

  /** 
   * Create a new itql-query instance. 
   */
  public ItqlQuery() {
    super(log);
  }

  public QueryInfo parseItqlQuery(Session sess, String query) throws OtmException {
    if (log.isDebugEnabled())
      log.debug("parsing query '" + query + "'");

    try {
      AST a1 = parseQuery(sess, query);

      ItqlConstraintGenerator cg = new ItqlConstraintGenerator();
      cg.query(a1);
      checkMessages(cg.getErrors(), cg.getWarnings());

      ItqlRedux ir = new ItqlRedux();
      ir.query(cg.getAST());
      checkMessages(ir.getErrors(), ir.getWarnings());

      ItqlWriter wr = new ItqlWriter();
      QueryInfo qi = wr.query(ir.getAST());
      checkMessages(wr.getErrors(), wr.getWarnings());

      if (log.isDebugEnabled())
        log.debug("parsed query '" + query + "': itql='" + qi.getQuery() + "', vars='" +
                  qi.getVars() + "', types='" + qi.getTypes() + "'");

      return qi;
    } catch (ANTLRException ae) {
      throw new QueryException("error parsing query '" + query + "'", ae);
    }
  }
}
