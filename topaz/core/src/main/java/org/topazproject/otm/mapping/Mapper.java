/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.otm.mapping;

import org.topazproject.otm.CascadeType;
import org.topazproject.otm.CollectionType;
import org.topazproject.otm.EntityMode;
import org.topazproject.otm.FetchType;
import org.topazproject.otm.Session;
import org.topazproject.otm.id.IdentifierGenerator;

/**
 * Mapper for a java class field to rdf triples having a specific predicate.
 *
 * @author Pradeep Krishnan
 */
public interface Mapper {
  /**
   * Get the Binder for this field
   *
   * @return the binder for this field
   */
  public Binder getBinder(Session session);

  /**
   * Get the Binder for this field
   *
   * @return the binder for this field
   */
  public Binder getBinder(EntityMode mode);

  /**
   * Gets the name of the field.
   *
   * @return the name
   */
  public String getName();

  /**
   * Checks if the type is an rdf resource and not a literal.
   *
   * @return true if this field is persisted as a uri
   */
  public boolean typeIsUri();

  /**
   * Gets the dataType for a literal field.
   *
   * @return the dataType or null for un-typed literal
   */
  public String getDataType();

  /**
   * Checks if the type is an association and not a serialized literal/URI.
   * When a field is not an association, the node is considered a leaf node
   * in the rdf graph.
   *
   * @return true if this field is 
   */
  public boolean isAssociation();

  // XXX: for now
  public boolean isPredicateMap();

  /**
   * Gets the rdf:type for an association field.
   *
   * @return the rdf:type or null for un-typed
   */
  public String getRdfType();

  /**
   * Gets the rdf predicate uri. All fields other than an 'Id' field must have a uri (for regular
   * classes) or a projection-variable (for views).
   *
   * @return the rdf predicate uri
   */
  public String getUri();

  /**
   * Gets the projection variable. All fields in a view must have a projection-variable which
   * specifies which element in projection list to tie this field to.
   *
   * @return the projection variable
   */
  public String getProjectionVar();

  /**
   * Tests if the predicate uri represents an inverse.
   *
   * @return true if the predicate uri points towards us rather than away
   */
  public boolean hasInverseUri();

  /**
   * Gets the model where this field is persisted.
   *
   * @return the model name or null 
   */
  public String getModel();

  /**
   * Gets the Collection type of this mapper.
   *
   * @return the collection type
   */
  public CollectionType getColType();

  /**
   * Tests if the triples for this field are owned by the containing entity.
   *
   * @return true if owned, 
   */
  public boolean isEntityOwned();

  /**
   * Get the generator for this field
   *
   * @return the generator to use for this field (or null if there isn't one)
   */
  public IdentifierGenerator getGenerator();

  /**
   * Get the cascading options for this field.
   *
   * @return the cascading options.
   */
  public CascadeType[] getCascade();

  /**
   * Tests if an operation is cascaded for this field
   */
  public boolean isCascadable(CascadeType op);

  /**
   * Get the fetch options for this field. Only applicable for associations.
   *
   * @return the FetchType option
   */
  public FetchType getFetchType();

  /**
   * For associations, the name of the associated entity.
   * 
   * @return the name of the associated entity or null if this is not an association mapping
   */
  public String getAssociatedEntity();
}
