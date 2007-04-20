/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.otm.owl;

import java.net.URI;
import java.util.List;
import java.util.ArrayList;

import org.topazproject.otm.annotations.Model;
import org.topazproject.otm.annotations.Rdf;
import org.topazproject.otm.annotations.RdfList;
import org.topazproject.otm.annotations.Id;

/**
 * Represents an ObjectProperty in owl.
 *
 * @author Eric Brown
 */
@Model("metadata")
@Rdf(Rdf.owl + "ObjectProperty")
public class ObjectProperty {
  @Id
  private URI property;
  @Rdf(Rdf.rdfs + "range")
  private URI[] ranges;
  @Rdf(Rdf.rdfs + "domain")
  private DomainUnion domains;

  /**
   * Returns the URI of this property.
   *
   * @return the URI of this property
   */
  public URI getProperty() {
    return property;
  }

  /**
   * Set the URI of this property.
   *
   * @param property the URI representing this owl property
   */
  public void setProperty(URI property) {
    this.property = property;
  }

  /**
   * Return the set of ranges possible for this property. If there are any ranges
   * defined, these represent data-types or possible values.
   *
   * @return list of ranges for this property
   */
  public URI[] getRanges() {
    return ranges;
  }

  /**
   * Set ranges for this property.
   *
   * @param ranges possible qualifiers for this property.
   */
  public void setRanges(URI[] ranges) {
    this.ranges = ranges;
  }

  /**
   * Get possible domains for this property. Domains are the owl classes that this
   * property appears in.
   *
   * @returns a union of possible owl classes.
   */
  public DomainUnion getDomains() {
    return domains;
  }

  /**
   * Set the domains.
   *
   * @param domains a union of domains
   */
  public void setDomains(DomainUnion domains) {
    this.domains = domains;
  }
}
