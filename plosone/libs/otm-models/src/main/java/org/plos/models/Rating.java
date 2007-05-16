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
@Entity(type = Rdf.topaz + "RatingsAnnotation")
public class Rating extends AbstractAnnotation {
  public static final String STYLE_TYPE = Rdf.topaz + "StyleRating";
  public static final String INSIGHT_TYPE = Rdf.topaz + "InsightRating";
  public static final String RELIABILITY_TYPE = Rdf.topaz + "ReliabilityRating";
  public static final String OVERALL_TYPE = Rdf.topaz + "OverallRating";
  @Predicate(uri = Annotea.NS + "body")
  private RatingContent body;

  /**
   * Creates a new Rating object.
   */
  public Rating() {
  }

  /**
   * Creates a new Rating object.
   *
   * @param id the rating annotation id
   */
  public Rating(URI id) {
    super(id);
  }

  /**
   * @return Returns the rating.
   */
  public RatingContent getBody() {
    return this.body;
  }

  /**
   * @param rating The rating to set.
   */
  public void setBody(RatingContent rating) {
    this.body = rating;
  }

  /**
   * Get the value of the rating
   *
   * @return value
   */
  public int retrieveValue() {
    if (this.body == null) {
      return -1;
    }

    return this.body.getValue();
  }

  /**
   * Set the value of the rating
   *
   * @param value
   */
  public void assignValue(int value) {
    if (this.body == null) {
      this.body = new RatingContent();
    }

    this.body.setValue(value);
  }
}
