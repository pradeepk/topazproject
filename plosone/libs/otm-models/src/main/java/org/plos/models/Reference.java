/* $HeadURL::                                                                                     $
 * $Id: $
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.models;

import java.net.URI;
import org.topazproject.otm.annotations.Id;
import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.Predicate;
import org.topazproject.otm.annotations.Rdf;
import org.topazproject.otm.annotations.UriPrefix;

/**
 * Articles contain references.
 *
 * @author Eric Brown
 */
@Entity(type = Rdf.topaz + "Reference", model = "ri")
@UriPrefix(Rdf.topaz)
public class Reference {
  @Id
  private URI id;
  @Predicate(uri = Rdf.topaz + "label")
  private String label;

  public Citation citation = new Citation();

  /**
   * @return the id of the reference
   */
  public URI getId() {
    return id;
  }

  /**
   * @param id the reference id
   */
  public void setId(URI id) {
    this.id = id;
  }

  /**
   * @return the reference's label
   */
  public String getLabel() {
    return label;
  }

  /**
   * @param label the label for this reference
   */
  public void setLabel(String label) {
    this.label = label;
  }
}
