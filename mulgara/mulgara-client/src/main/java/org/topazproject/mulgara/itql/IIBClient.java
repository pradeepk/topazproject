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

import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.mulgara.itql.ItqlInterpreterBean;
import org.mulgara.itql.ItqlInterpreterException;
import org.mulgara.query.Answer;
import org.mulgara.query.QueryException;

/** 
 * A mulgara client using ItqlInterpreterBean directly. This is a thin wrapper around
 * ItqlInterpreterBean.
 *
 * @author Ronald Tschalär
 * @see org.mulgara.itql.ItqlInterpreterBean
 */
abstract class IIBClient implements ItqlClient {
  private static final Log  log = LogFactory.getLog(IIBClient.class);

  /** the underlying bean instance to use */
  protected final ItqlInterpreterBean iib;

  /**
   * Create a new instance using the given bean.
   */
  protected IIBClient(ItqlInterpreterBean iib) {
    this.iib = iib;
  }

  public List<org.topazproject.mulgara.itql.Answer> doQuery(String itql)
      throws IOException, AnswerException {
    try {
      if (log.isDebugEnabled())
        log.debug("sending query '" + itql + "'");

      List ans = iib.executeQueryToList(itql, true);

      if (log.isDebugEnabled())
        log.debug("got result '" + ans + "'");

      List<org.topazproject.mulgara.itql.Answer> res =
          new ArrayList<org.topazproject.mulgara.itql.Answer>();
      for (Object o : ans) {
        if (o instanceof Answer)
          res.add(new AnswerAnswer((Answer) o));
        else if (o instanceof String)
          res.add(new AnswerAnswer((String) o));
        else if (o instanceof ItqlInterpreterException)
          throw (ItqlInterpreterException) o;
        else
          throw new Error("Unexpected object received in result to query '" + itql + "': class=" +
                          o.getClass().getName());
      }

      return res;
    } catch (ItqlInterpreterException e) {
      throw (IOException) new IOException().initCause(e);
    }
  }

  public void doUpdate(String itql) throws IOException {
    try {
      if (log.isDebugEnabled())
        log.debug("sending update '" + itql + "'");

      iib.executeUpdate(itql);
    } catch (ItqlInterpreterException e) {
      throw (IOException) new IOException().initCause(e);
    }
  }

  public void beginTxn(String txnName) throws IOException {
    if (log.isDebugEnabled())
      log.debug("sending beginTransaction '" + txnName + "'");

    try {
      iib.beginTransaction(txnName);
    } catch (QueryException e) {
      throw (IOException) new IOException().initCause(e);
    }
  }

  public void commitTxn(String txnName) throws IOException {
    if (log.isDebugEnabled())
      log.debug("sending commit '" + txnName + "'");

    try {
      iib.commit(txnName);
    } catch (QueryException e) {
      throw (IOException) new IOException().initCause(e);
    }
  }

  public void rollbackTxn(String txnName) throws IOException {
    if (log.isDebugEnabled())
      log.debug("sending rollback '" + txnName + "'");

    boolean ok = false;
    try {
      iib.rollback(txnName);
      ok = true;
    } catch (QueryException e) {
      throw (IOException) new IOException().initCause(e);
    } finally {
      if (!ok) {
        try {
          iib.commit(txnName);  // this always resets the transaction
        } catch (Exception e) {
          log.error("Error resetting session", e);
        }
      }
    }
  }

  public void setAliases(Map<String, String> aliases) {
    iib.setAliasMap(aliases instanceof HashMap ? (HashMap) aliases : new HashMap(aliases));
  }

  public Map<String, String> getAliases() {
    return (Map<String, String>) iib.getAliasMap();
  }

  public Exception getLastError() {
    return iib.getLastError();
  }

  public void clearLastError() {
    iib.clearLastError();
  }

  public void close() {
    iib.close();
  }
}
