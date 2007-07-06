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

import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.Criteria;
import org.topazproject.otm.ModelConfig;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.mapping.Mapper;

/**
 * A criterion for an "exists" operation on a field.
 *
 * @author Pradeep Krishnan
 */
@Entity(type = Criterion.RDF_TYPE + "/exists")
public class ExistsCriterion extends Criterion {
  private String fieldName;

  /**
   * Creates a new ExistsCriterion object.
   */
  public ExistsCriterion() {
  }

  /**
   * Creates a new ExistsCriterion object.
   *
   * @param name field/predicate name
   */
  public ExistsCriterion(String name) {
    this.fieldName = name;
  }

  /*
   * inherited javadoc
   */
  public String toItql(Criteria criteria, String subjectVar, String varPrefix)
                throws OtmException {
    Criterion impl = new PredicateCriterion(fieldName);

    return impl.toItql(criteria, subjectVar, varPrefix);
  }

  /**
   * Get fieldName.
   *
   * @return fieldName as String.
   */
  public String getFieldName() {
    return fieldName;
  }

  /**
   * Set fieldName.
   *
   * @param fieldName the value to set.
   */
  public void setFieldName(String fieldName) {
    this.fieldName = fieldName;
  }
}
