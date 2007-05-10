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

import java.net.URI;

import org.plos.annotation.otm.Annotation;
import org.plos.annotation.otm.Annotea;
import org.plos.annotation.otm.AnnotationBody;

import org.topazproject.otm.annotations.Embedded;
import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.Predicate;
import org.topazproject.otm.annotations.Rdf;

/**
 * General base rating class to store a RatingContent body
 * 
 * @author Stephen Cheng
 *
 */
@Entity(type = Rdf.topaz + "RatingSummaryAnnotation")
public class RatingSummary extends Annotation {

  @Predicate(uri=Annotea.NS + "body")
  private RatingSummaryContent                              body;
  
  @Predicate(uri = Rdf.rdf + "type")
  private String                                            type;
  
  public RatingSummary() {
  }

  public RatingSummary (URI id) {
    super(id);
  }
  /**
   * @return Returns the rating.
   */
  public RatingSummaryContent getBody() {
    return this.body;
  }
  
  /**
   * @param rating The rating to set.
   */
  public void setBody(AnnotationBody rating) {
    this.body = (RatingSummaryContent)rating;
  }

  
  /**
   * Get the value of the rating
   * 
   * @return value
   */
  public double retrieveAverage() {
    if (this.body == null) {
      return 0;
    }
    return this.body.retrieveAverage();
  }
  
  
  /**
   * Set the value of the rating
   * 
   * @param numRatings
   */
  public void assignNumRatings (int numRatings) {
    if (this.body == null) {
      this.body = new RatingSummaryContent();
    }
    this.body.setNumRatings(numRatings);
  }
  
  
  /**
   * Get the value of the rating
   * 
   * @return value
   */
  public int retrieveNumRatings() {
    if (this.body == null) {
      return 0;
    }
    return this.body.getNumRatings();
  }
  
  
  /**
   * Set the value of the rating
   * 
   * @param total
   */
  public void assignTotal(double total) {
    if (this.body == null) {
      this.body = new RatingSummaryContent();
    }
    this.body.setTotal(total);
  }

  /**
   * Set the value of the rating
   * 
   */
  public double retrieveTotal() {
    if (this.body == null){
      return 0;
    }
    return this.body.getTotal();
  }
  
  

  /**
   * @return Returns the type.
   */
  public String getType() {
    return type;
  }

  /**
   * @param type The type to set.
   */
  public void setType(String type) {
    this.type = type;
  }
  
  public void removeRating (int value) {
    assignNumRatings(retrieveNumRatings() - 1);
    assignTotal(retrieveTotal() - value);
  }
  
  public void addRating (int value) {
    assignNumRatings(retrieveNumRatings() + 1);
    assignTotal(retrieveTotal() + value);
  }
  
}
  

