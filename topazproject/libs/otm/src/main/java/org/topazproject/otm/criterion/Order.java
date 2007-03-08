package org.topazproject.otm.criterion;

/**
 * Specification of an order-by on a Criteria.
 *
 * @author Pradeep Krishnan
 */
public class Order {
  private String  name;
  private boolean ascending;

/**
   * Creates a new Order object.
   *
   * @param name DOCUMENT ME!
   * @param ascending DOCUMENT ME!
   */
  public Order(String name, boolean ascending) {
    this.name        = name;
    this.ascending   = ascending;
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
  public boolean isAscending() {
    return ascending;
  }

  /**
   * DOCUMENT ME!
   *
   * @param name DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public static Order asc(String name) {
    return new Order(name, true);
  }

  /**
   * DOCUMENT ME!
   *
   * @param name DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public static Order desc(String name) {
    return new Order(name, false);
  }
}
