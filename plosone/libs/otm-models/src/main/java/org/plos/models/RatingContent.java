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

import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.GeneratedValue;
import org.topazproject.otm.annotations.Id;
import org.topazproject.otm.annotations.Predicate;
import org.topazproject.otm.annotations.Rdf;

/**
 * AnnotationBody implementation to store a value for the rating
 *
 * @author stevec
 */
@Entity(model = "ri", type = Rdf.topaz + "RatingContent")
public class RatingContent {
  @Id
  @GeneratedValue(uriPrefix = "info:doi/10.1371/ratingContent/")
  private String                                                                  id;
  @Predicate(uri = Rdf.topaz + "RatingValue")
  private int                                                                     value;

  /**
   * Creates a new RatingContent object.
   */
  public RatingContent() {
  }

  /**
   * Creates a new RatingContent object.
   *
   * @param inValue the rating value to set
   */
  public RatingContent(int inValue) {
    this.value = inValue;
  }

  /**
   * @return Returns the value.
   */
  public int getValue() {
    return value;
  }

  /**
   * @param value The value to set.
   */
  public void setValue(int value) {
    this.value = value;
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
}
