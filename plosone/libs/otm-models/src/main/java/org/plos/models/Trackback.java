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

import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.Predicate;
import org.topazproject.otm.annotations.Rdf;

/**
 * Represents a trackback on a resource.
 *
 * @author Stephen Cheng
 */
@Entity(type = Rdf.topaz + "TrackbackAnnotation")
public class Trackback extends Annotation {
  @Predicate(uri = Annotea.NS + "body")
  private TrackbackContent body;

  public Trackback() {

  }

  /**
   * Creates a new Trackback object.
   *
   * @param id the trackback annotation id
   */
  public Trackback(URI id) {
    super(id);
  }

  /**
   * @return Returns the body.
   */
  public TrackbackContent getBody() {
    return body;
  }

  /**
   * @param body The body to set.
   */
  public void setBody(TrackbackContent body) {
    this.body = body;
  }
}
