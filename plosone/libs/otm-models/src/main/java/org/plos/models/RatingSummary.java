/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.models;

import java.net.URI;

import org.topazproject.otm.annotations.Embedded;
import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.Predicate;
import org.topazproject.otm.annotations.Rdf;

/**
 * General base rating class to store a RatingContent body
 *
 * @author Stephen Cheng
 */
@Entity(type = Rdf.topaz + "RatingSummaryAnnotation")
public class RatingSummary extends AbstractAnnotation {
  @Predicate(uri = Annotea.NS + "body")
  private RatingSummaryContent body;

  /**
   * Creates a new RatingSummary object.
   */
  public RatingSummary() {
  }

  /**
   * Creates a new RatingSummary object.
   *
   * @param id id for the rating summary annotation
   */
  public RatingSummary(URI id) {
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
  public void setBody(RatingSummaryContent rating) {
    this.body = rating;
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
  public void assignNumRatings(int numRatings) {
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
   * Get the value of the rating
   *
   * @return total rating
   */
  public double retrieveTotal() {
    if (this.body == null) {
      return 0;
    }

    return this.body.getTotal();
  }

  /**
   * @param value remove a rating
   */
  public void removeRating(int value) {
    assignNumRatings(retrieveNumRatings() - 1);
    assignTotal(retrieveTotal() - value);
  }

  /**
   * @param value add a rating
   */
  public void addRating(int value) {
    assignNumRatings(retrieveNumRatings() + 1);
    assignTotal(retrieveTotal() + value);
  }
}
