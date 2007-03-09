package org.topazproject.otm.criterion;

import org.topazproject.otm.Criteria;

/**
 * An interface for all query criterion used as restrictions in a 
 * {@link org.topazproject.otm.Criteria}.
 *
 * @author Pradeep Krishnan
  */
public interface Criterion {
  /**
   * Creates an ITQL query 'where clause' fragment.
   *
   * @param criteria the Criteria
   * @param subjectVar the subject designator variable (eg. $s etc.)
   * @param varPrefix namespace for internal variables (ie. not visible on select list)
   *
   * @return the itql query fragment
   */
  public String toItql(Criteria criteria, String subjectVar, String varPrefix);
}
