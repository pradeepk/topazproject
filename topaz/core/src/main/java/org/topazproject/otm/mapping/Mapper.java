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
import org.topazproject.otm.Session;

/**
 * Defines a Mapper for a property .
 *
 * @author Pradeep Krishnan
 */
public interface Mapper {
  /**
   * Get the Binder for this field
   *
   * @return the binder for this field
   */
  public Binder getBinder(Session session);

  /**
   * Get the Binder for this field
   *
   * @return the binder for this field
   */
  public Binder getBinder(EntityMode mode);

  /**
   * Gets the name of the field.
   *
   * @return the name
   */
  public String getName();

  /**
   * Gets all defined binders.
   *
   * @return the name
   */
  public Map<EntityMode, Binder> getBinders();
}
