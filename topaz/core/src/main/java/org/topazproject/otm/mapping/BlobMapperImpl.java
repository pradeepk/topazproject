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

/**
 * An implementation of Mapper for Blob fields.
 *
 * @author Pradeep krishnan
 */
public class BlobMapperImpl extends AbstractMapper implements BlobMapper {

  // XXX: temporary
  public BlobMapperImpl(Binder binder) {
    super(binder);
  }

  /**
   * Creates a new BlobMapperImpl object.
   *
   * @param name name of the blob field
   * @param binders the binders
   */
  public BlobMapperImpl(String name, Map<EntityMode, Binder> binders) {
    super(name, binders);
  }
}
