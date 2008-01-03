/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.otm.mapping.java;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.topazproject.otm.mapping.Loader;
import org.topazproject.otm.serializer.Serializer;

/**
 * Mapper for a java class field to rdf triples having a specific predicate.
 *
 * @author Pradeep Krishnan
 */
public interface FieldLoader extends Loader {
  /**
   * Gets the get method used.
   *
   * @return the get method or null
   */
  public Method getGetter();

  /**
   * Gets the set method used.
   *
   * @return the set method or null
   */
  public Method getSetter();

  /**
   * Gets the underlying field.
   *
   * @return the filed
   */
  public Field getField();

  /**
   * Gets the type of the field.
   *
   * @return the field type
   */
  public Class getType();

  /**
   * Gets the component type of this field.
   *
   * @return component type for arrays; member type for collections; or same as type for simple
   *         fields
   */
  public Class getComponentType();

  /**
   * Gets the serializer used. Note that there won't be any serializer set up for
   * associations.
   *
   * @return the serializer or null
   */
  public Serializer getSerializer();
}
