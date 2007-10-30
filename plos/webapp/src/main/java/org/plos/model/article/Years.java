package org.plos.model.article;

import java.util.TreeMap;



/**
 * An ordered list of years (as year numbers). Each year has a list of months.
 */
public class Years extends TreeMap<Integer, Months> {
  /**
   * @return the list of months (possibly emtpy, but always non-null)
   */
  public Months getMonths(Integer year) {
    Months months = get(year);
    if (months == null)
      put(year, months = new Months());
    return months;
  }
}