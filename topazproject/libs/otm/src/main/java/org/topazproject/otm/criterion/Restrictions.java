package org.topazproject.otm.criterion;

/**
 * A factory class for creating Criterion objects for adding to a Criteria.
 *
 * @author Pradeep Krishnan
 */
public class Restrictions {
  /**
   * Creates a criterion where the id of the retrieved object is known.
   *
   * @param value the subject-uri/id of the object
   *
   * @return a newly created Criterion object
   */
  public static Criterion id(String value) {
    return new SubjectCriterion(value);
  }

  /**
   * Creates a criterion where an object property has a known value.
   *
   * @param name the property name
   * @param value its value
   *
   * @return a newly created Criterion object
   */
  public static Criterion eq(String name, String value) {
    return new PredicateCriterion(name, value);
  }
}
