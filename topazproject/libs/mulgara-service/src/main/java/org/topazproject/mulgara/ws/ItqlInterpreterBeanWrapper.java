/* $HeadURL::                                                                                     $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.topazproject.mulgara.ws;

import javax.xml.rpc.ServiceException;
import javax.xml.rpc.server.ServiceLifecycle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.kowari.itql.ItqlInterpreterBean;
import org.kowari.itql.ItqlInterpreterException;
import org.kowari.query.QueryException;
import org.kowari.server.SessionFactory;

/**
 * A wrapper around ItqlInterpreterBean to properly initialize it and manage the instances.
 *
 * @author Ronald Tschalär
 */
public class ItqlInterpreterBeanWrapper implements ServiceLifecycle {
  private static final Log  log = LogFactory.getLog(ItqlInterpreterBeanWrapper.class);
  private static final long MIN_FREE_MEM = 10 * 1024 * 1024;    // TODO: make this configurable

  private ItqlInterpreterBean interpreter;

  public void init(Object context) throws ServiceException {
    try {
      /* Note: this check won't work properly on JVM's that return Long.MAX_VALUE for maxMemory().
       * But on those it's hard to know ahead of time if you're getting low on memory...
       */
      Runtime rt = Runtime.getRuntime();
      if (rt.maxMemory() - rt.totalMemory() + rt.freeMemory() < MIN_FREE_MEM)
        throw new ServiceException("Memory too low (probably too many sessions)");

      SessionFactory sf = WebAppListenerInitMulgara.getSessionFactory();
      interpreter = new ItqlInterpreterBean(sf.newSession(), sf.getSecurityDomain());

      if (log.isDebugEnabled())
        log.debug("Created ItqlInterpreterBean instance " + interpreter);
    } catch (QueryException qe) {
      log.error("Error initializing ItqlInterpreterBeanWrapper", qe);
      throw new ServiceException(qe);
    }
  }

  public void destroy() {
    close();
  }

  /** 
   * @see ItqlInterpreterBean#close 
   */
  public synchronized void close() {
    if (interpreter == null)
      return;

    if (log.isDebugEnabled())
      log.debug("Closing ItqlInterpreterBean instance " + interpreter);

    interpreter.close();
    interpreter = null;
  }

  /** 
   * @see ItqlInterpreterBean#beginTransaction 
   */
  public void beginTransaction(String name) throws QueryException {
    interpreter.beginTransaction(name);
  }

  /** 
   * @see ItqlInterpreterBean#commit 
   */
  public void commit(String name) throws QueryException {
    interpreter.commit(name);
  }

  /** 
   * @see ItqlInterpreterBean#rollback 
   */
  public void rollback(String name) throws QueryException {
    interpreter.rollback(name);
  }

  /** 
   * @see ItqlInterpreterBean#executeQueryToString 
   */
  public String executeQueryToString(String queryString) throws Exception {
    return interpreter.executeQueryToString(queryString);
  }

  /** 
   * @see ItqlInterpreterBean#executeUpdate 
   */
  public void executeUpdate(String itql) throws ItqlInterpreterException {
    interpreter.executeUpdate(itql);
  }
}
