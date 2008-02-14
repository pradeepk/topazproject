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

import java.util.EnumSet;
import java.util.List;

import org.topazproject.otm.CascadeType;
import org.topazproject.otm.CollectionType;
import org.topazproject.otm.FetchType;
import org.topazproject.otm.OtmException;
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
   * @return the loader for this field
   */
  public Binder getBinder();

  /**
   * Gets the name of the field.
   *
   * @return the name
   */
  public String getName();

  /**
   * Get a value from a field of an object.
   *
   * @param o the object
   *
   * @return the list containing the field's values (may be serialized)
   *
   * @throws OtmException if a field's value cannot be retrieved and serialized
   */
  public List get(Object o) throws OtmException;

  /**
   * Set a value for an object field.
   *
   * @param o the object
   * @param vals the list of values to set (may be deserialized)
   *
   * @throws OtmException if a field's value cannot be de-serialized and set
   */
  public void set(Object o, List vals) throws OtmException;

  /**
   * Get the raw object field value.
   *
   * @param o the object
   * @param create whether to create an instance
   *
   * @return the raw field value
   *
   * @throws OtmException if a field's value cannot be retrieved
   */
  public Object getRawValue(Object o, boolean create) throws OtmException;

  /**
   * Set the raw object field value
   *
   * @param o the object
   * @param value the value to set
   *
   * @throws OtmException if a field's value cannot be set
   */
  public void setRawValue(Object o, Object value) throws OtmException;

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

  // XXX: temporary hack
  public Class getComponentType();

  /**
   * For associations, the name of the associated entity.
   * 
   * @return the name of the associated entity or null if this is not an association mapping
   */
  public String getAssociatedEntity();
}
