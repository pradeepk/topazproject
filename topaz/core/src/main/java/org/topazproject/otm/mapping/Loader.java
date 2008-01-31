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

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.topazproject.otm.OtmException;
import org.topazproject.otm.Session;

/**
 * Mapper for a java class field to rdf triples having a specific predicate.
 *
 * @author Pradeep Krishnan
 */
public interface Loader {
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
  public boolean typeIsUri(Mapper mapper);

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
   * Load the values into this field of the given object instance.
   *
   * @param instance the object
   * @param values   the values to set
   * @param types    the type look ahead for associations
   * @param mapper   the mapper that this loader is associated to (XXX:revisit) 
   * @param session  the session under which the load is performed.
   *                 Used for resolving associations etc.
   *
   * @throws OtmException if a field's value cannot be set
   */
  public void load(Object instance, List<String> values, 
          Map<String, Set<String>> types, Mapper mapper, 
          Session session) throws OtmException;

  /**
   * Tests wether this field of the given object instance is loaded completely.
   * Used mainly for testing if a lazy-loaded field is loaded or is still a proxy.
   *
   * @param instance the object
   *
   * @return true for all eager-loaded fields and lazy-loaded fields that are loaded completely
   */
  public boolean isLoaded(Object instance);

}
