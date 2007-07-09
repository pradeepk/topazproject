/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.topazproject.otm.stores;

import org.topazproject.otm.Filter;

import antlr.collections.AST;

/** 
 * This holds the information representing a parsed filter for iTQL.
 *
 * @author Ronald Tschalär
 */
public class ItqlFilter {
  private final String filteredClass;
  private final AST    filterDef;
  private final String classVar;

  /** 
   * Create a new filter-info instance. 
   * 
   * @param f           the original filter this is for
   * @param parsedQuery the preparsed filter expression, as returned by ItqlConstraingGenerator
   */
  ItqlFilter(Filter f, AST parsedQuery) {
    this.filteredClass = f.getFilterDefinition().getFilteredClass();

    AST from = parsedQuery.getFirstChild();
    assert from.getText().equals("from");
    assert !from.getFirstChild().getText().equals("comma");

    AST where = from.getNextSibling();
    assert where.getText().equals("where");

    this.filterDef     = where.getFirstChild();
    this.classVar      = from.getFirstChild().getNextSibling().getText();
  }

  /** 
   * @return the entity name or fully-qualified class name of the class being filtered
   */
  public String getFilteredClass() {
    return filteredClass;
  }

  /** 
   * @return the where clause of preparsed filter expression, as returned by ItqlConstraingGenerator
   */
  public AST getDef() {
    return filterDef;
  }

  /** 
   * @return the variable in the filter expression representing the filtered class
   */
  public String getVar() {
    return classVar;
  }
}
