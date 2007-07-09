/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.otm.criterion;

import org.topazproject.mulgara.itql.ItqlHelper;

import org.topazproject.otm.Criteria;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.annotations.Entity;

/**
 * A criterion for a "greater than or equals" operation on a field value.
 *
 * @author Pradeep Krishnan
 */
@Entity(type = Criterion.RDF_TYPE + "/ge")
public class GECriterion extends AbstractComparisonCriterion {
  /**
   * Creates a new EqualsCriterion object.
   */
  public GECriterion() {
  }

  /**
   * Creates a new PredicateCriterion object.
   *
   * @param name field/predicate name
   * @param value field/predicate value
   */
  public GECriterion(String name, Object value) {
    super(name, value);
  }

  /*
   * inherited javadoc
   */
  public String toItql(Criteria criteria, String subjectVar, String varPrefix)
                throws OtmException {
    return toItql(criteria, subjectVar, varPrefix, "ge");
  }
}
