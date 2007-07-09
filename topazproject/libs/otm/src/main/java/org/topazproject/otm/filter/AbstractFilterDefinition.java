/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.topazproject.otm.filter;

/**
 * This implements some minimal common functionality of filter-definitions.
 *
 * @author Ronald Tschalär
 */
public abstract class AbstractFilterDefinition implements FilterDefinition {
  private final String           filterName;
  private final String           filteredClass;

  protected AbstractFilterDefinition(String filterName, String filteredClass) {
    this.filterName    = filterName;
    this.filteredClass = filteredClass;
  }

  public String getFilterName() {
    return filterName;
  }

  public String getFilteredClass() {
    return filteredClass;
  }
}
