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

import org.topazproject.otm.filter.FilterDefinition;

/**
 * An OTM filter.
 *
 * @author Ronald Tschalär
 */
public interface Filter {
  /**
   * Get the underlying filter definition.
   *
   * @return the filter definition
   */
  FilterDefinition getFilterDefinition();

  /** 
   * Get the name of this filter.
   * 
   * @return the name of with this filter
   */
  String getName();

  /** 
   * Get the class this filter is applied to. This may be either the entity name associated with
   * the class or the fully qualified class name - see also {@link
   * org.topazproject.otm.SessionFactory#getClassMetadata(java.lang.String)
   * SessionFactory.getClassMetadata()}.
   * 
   * @return this
   */
  Filter setParameter(String name, Object value);
}
