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

import java.util.Map;

import org.topazproject.otm.EntityMode;
import org.topazproject.otm.FetchType;

/**
 * An implementation of Mapper for view projection fields.
 *
 * @author Pradeep krishnan
 */
public class VarMapperImpl extends AbstractMapper implements VarMapper {
  private final String    var;
  private final FetchType fetch;
  private final String    assoc;

  // XXX: temporary
  public VarMapperImpl(Binder binder, String var, FetchType fetch, String assoc) {
    super(binder);
    this.var     = var;
    this.fetch   = fetch;
    this.assoc   = assoc;
  }

  /**
   * Creates a new VarMapperImpl object.
   *
   * @param name the property name
   * @param binders the list of binders
   * @param var the projection variable
   * @param fetch the fetch mode for associations
   * @param assoc the associated entity name
   */
  public VarMapperImpl(String name, Map<EntityMode, Binder> binders, String var, FetchType fetch,
                       String assoc) {
    super(name, binders);
    this.var     = var;
    this.fetch   = fetch;
    this.assoc   = assoc;
  }

  /*
   * inherited javadoc
   */
  public String getProjectionVar() {
    return var;
  }

  /*
   * inherited javadoc
   */
  public FetchType getFetchType() {
    return fetch;
  }

  /*
   * inherited javadoc
   */
  public String getAssociatedEntity() {
    return assoc;
  }

  /*
   * inherited javadoc
   */
  public boolean isAssociation() {
    return (assoc != null);
  }
}
