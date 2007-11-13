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
import org.topazproject.otm.query.QueryException;
import org.topazproject.otm.query.Results;

/** 
 * This processes the Itql results into an OTM Results object. Indivual result objects in each row
 * are loaded on-demand.
 * 
 * @author Ronald Tschalär
 */
abstract class ItqlResults extends Results {
  protected final AnswerSet.QueryAnswerSet qas;
  protected final Session                  sess;
  protected final Type[]                   origTypes;

  /** 
   * Create a new itql-results instance.
   * 
   * @param vars     the list of variables in the result
   * @param types    the type of each variable
   * @param qas      the query-answer to build the results from
   * @param warnings the list of warnings generated while processing the query; may be null
   * @param sess     the session this is attached to
   */
  protected ItqlResults(String[] vars, Type[] types, AnswerSet.QueryAnswerSet qas,
                        String[] warnings, Session sess)
      throws OtmException {
    super(vars, types, warnings, sess.getSessionFactory());
    this.qas       = qas;
    this.sess      = sess;
    this.origTypes = types.clone();

    assert qas.getVariables().length == vars.length;
  }

  /** 
   * Parse an itql xml answer. 
   * 
   * @param a  the xml answer
   * @return the parsed answer, or null if there was no result
   * @throws OtmException if the answer is not a query-result or an error occured parsing the answer
   */
  protected static AnswerSet.QueryAnswerSet getQAS(String a) throws OtmException {
    try {
      AnswerSet ans = new AnswerSet(a);

      // check if we got something useful
      ans.beforeFirst();
      if (!ans.next())
        return null;
      if (!ans.isQueryResult())
        throw new QueryException("query failed: " + ans.getMessage());

      // looks like we're ok
      return ans.getQueryResults();
    } catch (AnswerException ae) {
      throw new QueryException("Error parsing answer", ae);
    }
  }

  @Override
  public void beforeFirst() throws OtmException {
    super.beforeFirst();
    qas.beforeFirst();
  }

  @Override
  protected void loadRow() throws OtmException {
    curRow = null;

    if (!qas.next())
      eor = true;
    else {
      for (int idx = 0; idx < getVariables().length; idx++) {
        if (origTypes[idx] == Type.UNKNOWN) {
          if (qas.isLiteral(idx))
            types[idx] = Type.LITERAL;
          else if (qas.isURI(idx))
            types[idx] = Type.URI;
          else if (qas.isSubQueryResults(idx))
            types[idx] = Type.SUBQ_RESULTS;
        }
      }
    }
  }

  @Override
  public Object[] getRow() throws OtmException {
    if (eor)
      throw new QueryException("at end of results");

    if (curRow == null) {
      curRow = new Object[qas.getVariables().length];
      for (int idx = 0; idx < curRow.length; idx++) {
        try {
          curRow[idx] = getResult(idx, getType(idx));
        } catch (AnswerException ae) {
          throw new QueryException("Error parsing answer", ae);
        }
      }
    }

    return curRow;
  }

  @Override
  public Object get(int idx) throws OtmException {
    if (eor)
      throw new QueryException("at end of results");

    if (curRow != null)
      return curRow[idx];
    else {
      try {
        return getResult(idx, getType(idx));
      } catch (AnswerException ae) {
        throw new QueryException("Error parsing answer", ae);
      }
    }
  }

  /** 
   * Get a single result object. This handles LITERAL, URI, and UNKNOWN only.
   * 
   * @param idx  which object to get
   * @param type the object's type
   * @return the object
   * @throws OtmException 
   * @throws AnswerException 
   */
  protected Object getResult(int idx, Type type) throws OtmException, AnswerException {
    switch (type) {
      case LITERAL:
        String dt = qas.getLiteralDataType(idx);
        return new Literal(qas.getString(idx), qas.getLiteralLangTag(idx),
                           (dt != null) ? URI.create(dt) : null);

      case URI:
        return qas.getURI(idx);

      case UNKNOWN:
        if (qas.isLiteral(idx))
          types[idx] = Type.LITERAL;
        else if (qas.isURI(idx))
          types[idx] = Type.URI;
        else if (qas.isSubQueryResults(idx))
          types[idx] = Type.SUBQ_RESULTS;
        else
          throw new Error("unknown query-answer type encountered at index " + idx);
        return getResult(idx, types[idx]);

      default:
        throw new Error("unknown type " + type + " encountered");
    }
  }
}
