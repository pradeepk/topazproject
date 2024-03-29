/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007-2008 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.topazproject.otm.mapping.java;

import java.lang.reflect.Method;

import java.util.List;

import org.topazproject.otm.OtmException;
import org.topazproject.otm.Session;
import org.topazproject.otm.mapping.PropertyBinder;
import org.topazproject.otm.mapping.RdfMapper;

/**
 * Mapper for a java class field to rdf triples having a specific predicate.
 *
 * @author Pradeep Krishnan
 */
public interface FieldBinder extends PropertyBinder {
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
   * Load the values into this field of the given object instance.
   *
   * @param root     the root object instance tracked by session
   * @param instance the current nested embedded instance
   * @param values   the values to set
   * @param mapper   the mapper that this loader is associated to
   * @param session  the session under which the load is performed.
   *                 Used for resolving associations etc.
   *
   * @throws OtmException if a field's value cannot be set
   */
  public void load(Object root, Object instance, List<String> values,
          RdfMapper mapper, Session session) throws OtmException;
}
