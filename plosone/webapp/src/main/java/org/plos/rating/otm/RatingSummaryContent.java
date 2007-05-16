/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.plos.rating.otm;

import org.topazproject.otm.annotations.GeneratedValue;
import org.topazproject.otm.annotations.Id;
import org.topazproject.otm.annotations.Predicate;
import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.Rdf;

/**
 * AnnotationBody implementation to store a value for the rating
 * 
 * @author stevec
 *
 */

@Entity (model="ri", type = Rdf.topaz + "RatingSummaryContent")
public class RatingSummaryContent {
  
  @Id @GeneratedValue(uriPrefix = "info:doi/10.1371/ratingSummaryContent/")
  private String id;
  
  @Predicate (uri = Rdf.topaz + "NumRatings")
  private int numRatings;
  
  @Predicate (uri = Rdf.topaz + "TotalValue")
  private double total;
  
  public RatingSummaryContent () {

  }
  
  public RatingSummaryContent (double totalValue, int numRatings) {
    this.total = totalValue;
    this.numRatings = numRatings;
  }
  
  /**
   * @return Returns the value.
   */
  public double retrieveAverage() {
    return total/numRatings;
  }
  
  /**
   * @return Returns the id.
   */
  public String getId() {
    return id;
  }

  /**
   * @param id The id to set.
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * @return Returns the numRatings.
   */
  public int getNumRatings() {
    return numRatings;
  }

  /**
   * @param numRatings The numRatings to set.
   */
  public void setNumRatings(int numRatings) {
    this.numRatings = numRatings;
  }

  /**
   * @return Returns the total.
   */
  public double getTotal() {
    return total;
  }

  /**
   * @param total The total to set.
   */
  public void setTotal(double total) {
    this.total = total;
  }
}
