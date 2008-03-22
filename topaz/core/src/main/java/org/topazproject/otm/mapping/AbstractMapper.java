/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.otm.mapping;

import java.util.HashMap;
import java.util.Map;

import org.topazproject.otm.EntityMode;
import org.topazproject.otm.Session;

/**
 * A convenient base class for all mappers.
 *
 * @author Pradeep krishnan
 */
public abstract class AbstractMapper implements Mapper {
  private final Map<EntityMode, Binder> binders;
  private final String                  name;

  // XXX : temporary
  public AbstractMapper(Binder binder) {
    name      = binder.getName();
    binders   = new HashMap<EntityMode, Binder>();
    binders.put(EntityMode.POJO, binder);
  }

  /**
   * Creates a new AbstractMapper object.
   *
   * @param name    name of this property
   * @param binders the binders for this property
   */
  public AbstractMapper(String name, Map<EntityMode, Binder> binders) {
    this.name      = name;
    this.binders   = binders;
  }

  /*
   * inherited javadoc
   */
  public Binder getBinder(Session session) {
    return getBinder(session.getEntityMode());
  }

  /*
   * inherited javadoc
   */
  public Binder getBinder(EntityMode mode) {
    return binders.get(mode);
  }

  /*
   * inherited javadoc
   */
  public String getName() {
    return name;
  }

  /*
   * inherited javadoc
   */
  public Map<EntityMode, Binder> getBinders() {
    return binders;
  }
}
