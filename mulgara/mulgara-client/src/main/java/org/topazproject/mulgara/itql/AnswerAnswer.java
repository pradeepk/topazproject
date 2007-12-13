/* $HeadURL::                                                                            $
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

import org.jrdf.graph.Literal;
import org.jrdf.graph.BlankNode;
import org.jrdf.graph.URIReference;

import org.mulgara.query.Answer;
import org.mulgara.query.TuplesException;
import org.mulgara.query.Variable;

/** 
 * This wraps a mulgara Answer object.
 * 
 * @author Ronald Tschalär
 */
class AnswerAnswer extends AbstractAnswer {
  private final Answer ans;

  /** 
   * Create a query answer.
   * 
   * @param ans the mulgara answer
   */
  public AnswerAnswer(Answer ans) {
    this.ans = ans;

    Variable[] vars = ans.getVariables();
    variables = new String[vars.length];
    for (int idx = 0; idx < vars.length; idx++)
      variables[idx] = vars[idx].getName();
  }

  /** 
   * Create a non-query answer.
   * 
   * @param msg the message
   */
  public AnswerAnswer(String msg) {
    this.ans = null;
    message = msg;
  }

  /**********************************************************************************/

  public void beforeFirst() throws AnswerException {
    if (ans == null)
      return;

    try {
      ans.beforeFirst();
    } catch (TuplesException te) {
      throw new AnswerException(te);
    }
  }

  public boolean next() throws AnswerException {
    if (ans == null)
      return false;

    try {
      return ans.next();
    } catch (TuplesException te) {
      throw new AnswerException(te);
    }
  }

  public boolean isLiteral(int idx) throws AnswerException {
    try {
      return ans.getObject(idx) instanceof Literal;
    } catch (TuplesException te) {
      throw new AnswerException(te);
    }
  }

  public String getLiteralDataType(int idx) throws AnswerException {
    Object o;
    try {
      o = ans.getObject(idx);
    } catch (TuplesException te) {
      throw new AnswerException(te);
    }

    if (o instanceof Literal) {
      URI dt = ((Literal) o).getDatatypeURI();
      return (dt != null) ? dt.toString() : null;
    }

    throw new AnswerException("is not a Literal");
  }

  public String getLiteralLangTag(int idx) throws AnswerException {
    Object o;
    try {
      o = ans.getObject(idx);
    } catch (TuplesException te) {
      throw new AnswerException(te);
    }

    if (o instanceof Literal) {
      String lang = ((Literal) o).getLanguage();
      return (lang != null && lang.length() > 0) ? lang : null;
    }

    throw new AnswerException("is not a Literal");
  }

  public String getString(int idx) throws AnswerException {
    Object o;
    try {
      o = ans.getObject(idx);
    } catch (TuplesException te) {
      throw new AnswerException(te);
    }

    if (o instanceof Literal)
      return ((Literal) o).getLexicalForm();

    if (o instanceof URIReference)
      return ((URIReference) o).getURI().toString();

    if (o instanceof BlankNode)
      return o.toString();

    if (o instanceof Answer)
      throw new AnswerException("is a subquery result");

    throw new AnswerException("unknown object: '" + o + "'");
  }

  public boolean isURI(int idx) throws AnswerException {
    try {
      return ans.getObject(idx) instanceof URIReference;
    } catch (TuplesException te) {
      throw new AnswerException(te);
    }
  }

  public URI getURI(int idx) throws AnswerException {
    Object o;
    try {
      o = ans.getObject(idx);
    } catch (TuplesException te) {
      throw new AnswerException(te);
    }

    if (o instanceof URIReference)
      return ((URIReference) o).getURI();

    throw new AnswerException("is not a URI");
  }

  public boolean isBlankNode(int idx) throws AnswerException {
    try {
      return ans.getObject(idx) instanceof BlankNode;
    } catch (TuplesException te) {
      throw new AnswerException(te);
    }
  }

  public String getBlankNode(int idx) throws AnswerException {
    Object o;
    try {
      o = ans.getObject(idx);
    } catch (TuplesException te) {
      throw new AnswerException(te);
    }

    if (o instanceof BlankNode)
      return o.toString();

    throw new AnswerException("is not a blank node");
  }

  public boolean isSubQueryResults(int idx) throws AnswerException {
    try {
      return ans.getObject(idx) instanceof Answer;
    } catch (TuplesException te) {
      throw new AnswerException(te);
    }
  }

  public AnswerAnswer getSubQueryResults(int idx) throws AnswerException {
    Object o;
    try {
      o = ans.getObject(idx);
    } catch (TuplesException te) {
      throw new AnswerException(te);
    }

    if (o instanceof Answer)
      return new AnswerAnswer((Answer) o);

    throw new AnswerException("is not a sub-query answer");
  }
}
