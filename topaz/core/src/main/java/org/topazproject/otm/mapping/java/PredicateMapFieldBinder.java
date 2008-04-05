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

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import java.util.Collections;
import java.util.List;

import org.topazproject.otm.OtmException;

/**
 * FieldBinder for a map that allows a dynamic/run-time map of properties to values.
 *
 * @author Pradeep Krishnan
 */
public class PredicateMapFieldBinder extends AbstractFieldBinder {
  /**
   * Creates a new PredicateMapFieldBinder object.
   *
   * @param field the java class field
   * @param getter the field get method or null
   * @param setter the field set method or null
   */
  public PredicateMapFieldBinder(Field field, Method getter, Method setter) {
    super(field, getter, setter, null, null);
  }

  /**
   * Returns the value as 'raw' map.
   *
   * @param o the object
   *
   * @return the 'raw' value in a list
   *
   * @throws OtmException if a field's value cannot be retrieved
   */
  public List get(Object o) throws OtmException {
    Object value = getRawValue(o, false);

    return (value == null) ? Collections.emptyList() : Collections.singletonList(value);
  }

  /**
   * Sets the value from a 'raw' map.
   *
   * @param o the object
   * @param vals the values to be set 
   *
   * @throws OtmException if a field's value cannot be set
   */
  public void set(Object o, List vals) throws OtmException {
    int size = vals.size();

    if (size > 1)
      throw new OtmException("Too many values for '" + getField().toGenericString() + "'");

    Object value = (size == 0) ? null : vals.get(0);
    setRawValue(o, value);
  }

}
