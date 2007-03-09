package org.topazproject.otm.criterion;

import org.topazproject.otm.Criteria;

/**
 * A criterion for a triple pattern where the subject value is known.
 *
 * @author Pradeep Krishnan
 */
public class SubjectCriterion implements Criterion {
  private String id;

/**
   * Creates a new SubjectCriterion object.
   *
   * @param id DOCUMENT ME!
   */
  public SubjectCriterion(String id) {
    this.id = id;
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public String getId() {
    return id;
  }

  /*
   * inherited javadoc
   */
  public String toItql(Criteria criteria, String subjectVar, String varPrefix) {
    return subjectVar + " <mulgara:is> <" + id + "> ";
  }
}
