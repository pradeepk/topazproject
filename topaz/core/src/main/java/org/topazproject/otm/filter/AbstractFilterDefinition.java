/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007-2008 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

  public int hashCode() {
    return filterName.hashCode();
  }

  public boolean equals(Object other) {
    if (!(other instanceof FilterDefinition))
      return false;
    return filterName.equals(((FilterDefinition) other).getFilterName());
  }
}
