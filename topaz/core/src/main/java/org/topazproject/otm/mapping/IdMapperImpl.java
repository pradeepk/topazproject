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
import org.topazproject.otm.id.IdentifierGenerator;

/**
 * An implementation of Mapper for Id fields.
 *
 * @author Pradeep krishnan
 */
public class IdMapperImpl extends AbstractMapper implements IdMapper {
  private IdentifierGenerator gen;

  // XXX: temporary
  public IdMapperImpl(Binder binder, IdentifierGenerator gen) {
    super(binder);
    this.gen = gen;
  }

  /**
   * Creates a new IdMapperImpl object.
   *
   * @param name the name of this mapper
   * @param binders the binders
   * @param gen id geberator or null
   */
  public IdMapperImpl(String name, Map<EntityMode, Binder> binders, IdentifierGenerator gen) {
    super(name, binders);
    this.gen = gen;
  }

  /*
   * inherited javadoc
   */
  public IdentifierGenerator getGenerator() {
    return gen;
  }
}
