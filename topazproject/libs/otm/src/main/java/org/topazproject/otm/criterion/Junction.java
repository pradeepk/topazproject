package org.topazproject.otm.criterion;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for junctions on Criterions.
 *
 * @author Pradeep Krishnan
 */
public class Junction implements Criterion {
  private List<Criterion> criterions = new ArrayList<Criterion>();
  private String          op;

/**
   * Creates a new Junction object.
   *
   * @param op the operation
   */
  protected Junction(String op) {
    this.op                          = op;
  }

  /**
   * Adds a Criterion.
   *
   * @param c the criterion
   *
   * @return this for expression chaining
   */
  public Junction add(Criterion c) {
    criterions.add(c);

    return this;
  }

  /**
   * Gets the list of criterions.
   *
   * @return list or criterions
   */
  public List<Criterion> getCriterions() {
    return criterions;
  }

  /**
   * Gets the operation.
   *
   * @return the operation
   */
  public String getOp() {
    return op;
  }
}
