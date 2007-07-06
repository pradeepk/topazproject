/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.otm.criterion;

import java.net.URI;

import org.topazproject.otm.Criteria;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.GeneratedValue;
import org.topazproject.otm.annotations.Id;
import org.topazproject.otm.annotations.Rdf;
import org.topazproject.otm.annotations.UriPrefix;

/**
 * An abstract base for all query criterion used as restrictions in a  {@link
 * org.topazproject.otm.Criteria}. 
 *
 * @author Pradeep Krishnan
 */
@Entity(type = Criterion.RDF_TYPE, model = "criterion")
@UriPrefix(Criterion.NS)
public abstract class Criterion {
  /**
   * Namespace for all URIs.
   */
  public static final String NS = Rdf.topaz + "otm/";

  /**
   * The base rdf:type and also the namespace for sub-class types.
   */
  public static final String RDF_TYPE = NS + "Criterion";

  /**
   * The id field used for persistence. Ignored otherwise.
   */
  @Id
  @GeneratedValue(uriPrefix = NS + "Criterion/Id/")
  public URI criterionId;

  /**
   * Creates an ITQL query 'where clause' fragment.
   *
   * @param criteria the Criteria
   * @param subjectVar the subject designator variable (eg. $s etc.)
   * @param varPrefix namespace for internal variables (ie. not visible on select list)
   *
   * @return the itql query fragment
   *
   * @throws OtmException if an error occurred
   */
  public abstract String toItql(Criteria criteria, String subjectVar, String varPrefix)
                         throws OtmException;
}
