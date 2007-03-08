package org.topazproject.otm.criterion;

/**
 * A criterion for a triple pattern where the predicate and value are known.
 *
 * @author Pradeep Krishnan
 */
public class PredicateCriterion implements Criterion {
  private String name;
  private String value;

/**
   * Creates a new PredicateCriterion object.
   *
   * @param name DOCUMENT ME!
   * @param value DOCUMENT ME!
   */
  public PredicateCriterion(String name, String value) {
    this.name    = name;
    this.value   = value;
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public String getName() {
    return name;
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public String getValue() {
    return value;
  }
}
