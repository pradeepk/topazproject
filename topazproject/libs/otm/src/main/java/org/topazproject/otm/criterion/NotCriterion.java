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
 * A criterion for doing a 'not' operation.
 *
 * @author Pradeep Krishnan
 */
@Entity(type = Criterion.NS + "/not")
public class NotCriterion extends Criterion {
  private Criterion criterion;

  /**
   * Creates a new NotCriterion object.
   */
  public NotCriterion() {
  }

  /**
   * Creates a new NotCriterion object.
   *
   * @param criterion the criterion to NOT
   */
  public NotCriterion(Criterion criterion) {
    setCriterion(criterion);
  }

  /*
   * inherited javadoc
   */
  public String toItql(Criteria criteria, String subjectVar, String varPrefix)
                throws OtmException {
    Criterion impl = new MinusCriterion(new PredicateCriterion(), criterion);

    return impl.toItql(criteria, subjectVar, varPrefix);
  }

  /**
   * Get criterion.
   *
   * @return criterion as Criterion.
   */
  public Criterion getCriterion() {
    return criterion;
  }

  /**
   * Set criterion.
   *
   * @param criterion the value to set.
   */
  public void setCriterion(Criterion criterion) {
    this.criterion = criterion;
  }
}
