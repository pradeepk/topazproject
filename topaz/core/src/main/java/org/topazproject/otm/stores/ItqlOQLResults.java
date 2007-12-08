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

import java.net.URI;
import java.util.List;

import org.topazproject.mulgara.itql.AnswerException;
import org.topazproject.mulgara.itql.AnswerSet.QueryAnswerSet;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.Session;
import org.topazproject.otm.query.ProjectionFunction;
import org.topazproject.otm.query.QueryInfo;

/** 
 * This processes the Itql results from an OQL query into an OTM Results object.
 * 
 * @author Ronald Tschalär
 */
class ItqlOQLResults extends ItqlResults {
  private final QueryInfo qi;

  private ItqlOQLResults(QueryAnswerSet qas, QueryInfo qi, String[] warnings, Session sess)
      throws OtmException {
    super(getVariables(qi), getTypes(qi), getFuncResult(qas, qi), warnings, sess);
    this.qi = qi;
  }

  /** 
   * Create a new oql-itql-query results object. 
   * 
   * @param a        the xml answer
   * @param qi       the query-info
   * @param warnings the list of warnings generated during the query processing, or null
   * @param sess     the session this is attached to
   * @throws OtmException 
   */
  public ItqlOQLResults(String a, QueryInfo qi, String[] warnings, Session sess)
      throws OtmException {
    this(getQAS(a), qi, warnings, sess);
  }

  private static String[] getVariables(QueryInfo qi) {
    List<String> vars = qi.getVars();

    int idx = 0;
    for (ProjectionFunction pf : qi.getFuncs()) {
      if (pf != null) {
        int old_size = vars.size();
        vars = pf.initVars(vars, idx);
        idx -= old_size - vars.size();
      }
      idx++;
    }

    return vars.toArray(new String[vars.size()]);
  }

  private static Type[] getTypes(QueryInfo qi) {
    List<Object> types = qi.getTypes();

    int idx = 0;
    for (ProjectionFunction pf : qi.getFuncs()) {
      if (pf != null) {
        int old_size = types.size();
        types = pf.initTypes(types, idx);
        idx -= old_size - types.size();
      }
      idx++;
    }

    Type[] res = new Type[types.size()];

    idx = 0;
    for (Object t : qi.getTypes()) {
      if (t == null)
        res[idx] = Type.UNKNOWN;
      else if (t instanceof QueryInfo)
        res[idx] = Type.SUBQ_RESULTS;
      else if (t.equals(URI.class))
        res[idx] = Type.URI;
      else if (t.equals(String.class))
        res[idx] = Type.LITERAL;
      else
        res[idx] = Type.CLASS;
      idx++;
    }

    return res;
  }

  private static QueryAnswerSet getFuncResult(QueryAnswerSet qas, QueryInfo qi) {
    int idx = 0;
    for (ProjectionFunction pf : qi.getFuncs()) {
      if (pf != null) {
        int old_size = qas.getVariables().length;
        qas = pf.initItqlResult(qas, idx);
        idx -= old_size - qas.getVariables().length;
      }
      idx++;
    }

    return qas;
  }

  @Override
  protected Object getResult(int idx, Type type, boolean eager)
      throws OtmException, AnswerException {
    ProjectionFunction pf = qi.getFuncs().get(idx);
    if (pf != null) {
      Object res = pf.getItqlResult(qas, pos, idx, type, eager);
      if (res != null)
        return res;
    }

    switch (type) {
      case SUBQ_RESULTS:
        return new ItqlOQLResults(qas.getSubQueryResults(idx), (QueryInfo) qi.getTypes().get(idx),
                                  null, sess);

      case CLASS:
        return eager ? sess.get((Class<?>) qi.getTypes().get(idx), qas.getString(idx), false) :
                       sess.load((Class<?>) qi.getTypes().get(idx), qas.getString(idx));

      default:
        return super.getResult(idx, type, eager);
    }
  }
}
