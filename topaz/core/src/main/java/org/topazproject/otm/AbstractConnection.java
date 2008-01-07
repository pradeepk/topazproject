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

import java.util.ArrayList;
import java.util.List;

/**
 * A connection impl that coordinates the operations on its child
 * connections. The paradigm that is followed here is the Tree 2-Phase commit.
 *
 * @author Pradeep Krishnan
  */
public abstract class AbstractConnection implements Connection {
  private final List<Connection> childConnections = new ArrayList<Connection>();

  /*
   * inherited javadoc
   */
  public void prepare() throws OtmException {
    for (Connection con : getChildConnections())
      con.prepare();
    doPrepare();
  }

  /*
   * inherited javadoc
   */
  public void commit() throws OtmException {
    for (Connection con : getChildConnections())
      con.commit();
    doCommit();
  }

  /*
   * inherited javadoc
   */
  public void rollback() throws OtmException {
    int idx = 0;
    List<Connection> cons = getChildConnections();
    try {
      doRollback();
      while (idx < cons.size())
       cons.get(idx++).rollback();
    } finally {
      while (idx < cons.size())
       try { cons.get(idx++).rollback(); } catch (Throwable t) {}
    }
  }

  /*
   * inherited javadoc
   */
  public List<Connection> getChildConnections() {
    return childConnections;
  }

  /**
   * Override in sub-classes to do the actual prepare on this Connection.
   */
  protected void doPrepare() throws OtmException {
  }

  /**
   * Override in sub-classes to do the actual commit on this Connection.
   */
  protected void doCommit() throws OtmException {
  }

  /**
   * Override in sub-classes to do the actual rollback on this Connection.
   */
  protected void doRollback() throws OtmException {
  }
}
