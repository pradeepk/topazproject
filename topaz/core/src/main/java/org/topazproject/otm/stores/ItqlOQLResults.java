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

import org.topazproject.mulgara.itql.AnswerException;
import org.topazproject.mulgara.itql.AnswerSet;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.Session;
import org.topazproject.otm.query.QueryInfo;

/** 
 * This processes the Itql results from an OQL query into an OTM Results object.
 * 
 * @author Ronald Tschalär
 */
class ItqlOQLResults extends ItqlResults {
  private final QueryInfo qi;

  private ItqlOQLResults(AnswerSet.QueryAnswerSet qas, QueryInfo qi, String[] warnings,
                         Session sess)
      throws OtmException {
    super(getVariables(qi), getTypes(qi), qas, warnings, sess);
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
    return qi.getVars().toArray(new String[0]);
  }

  private static Type[] getTypes(QueryInfo qi) {
    Type[] res = new Type[qi.getTypes().size()];

    int idx = 0;
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

  @Override
  protected Object getResult(int idx, Type type) throws OtmException, AnswerException {
    switch (type) {
      case SUBQ_RESULTS:
        return new ItqlOQLResults(qas.getSubQueryResults(idx), (QueryInfo) qi.getTypes().get(idx),
                                  null, sess);

      case CLASS:
        return sess.get((Class<?>) qi.getTypes().get(idx), qas.getString(idx), false);

      default:
        return super.getResult(idx, type);
    }
  }
}
