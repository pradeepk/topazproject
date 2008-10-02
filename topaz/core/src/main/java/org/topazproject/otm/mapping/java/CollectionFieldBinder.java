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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import java.util.AbstractList;
import java.util.AbstractSequentialList;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.topazproject.otm.FetchType;
import org.topazproject.otm.Filter;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.Session;
import org.topazproject.otm.mapping.RdfMapper;
import org.topazproject.otm.serializer.Serializer;

/**
 * FieldBinder for {@link java.util.Collection collection} fields.
 *
 * @author Pradeep Krishnan
 */
public class CollectionFieldBinder extends AbstractFieldBinder {
  /**
   * Creates a new CollectionFieldBinder object.
   *
   * @param property the bean property 
   * @param serializer the serializer or null
   */
  public CollectionFieldBinder(Property property, Serializer serializer) {
    super(property, serializer);
  }

  /**
   * Retrieve elements from a collection property of an object.
   *
   * @param o the object
   *
   * @return the list of array elements (may be serialized)
   *
   * @throws OtmException if a property's value cannot be retrieved and serialized
   */
  public List get(Object o) throws OtmException {
    Collection value = (Collection) getRawValue(o, false);

    if (value == null)
      return Collections.emptyList();

    ArrayList res = new ArrayList(value.size());

    for (Object v : value)
      if (v != null)
        res.add(serialize(v));

    return res;
  }

  /**
   * Populate a collection property of an object.
   *
   * @param o the object
   * @param vals the values to be set (may be deserialized)
   *
   * @throws OtmException if a property's value cannot be de-serialized and set
   */
  public void set(Object o, List vals) throws OtmException {
    Collection value = (Collection) getRawValue(o, false);
    boolean create = (value == null);

    if (create)
      value = newInstance();
    else
      value.clear();

    for (Object v : vals)
      value.add(deserialize(v));

    if (create)
      setRawValue(o, value);
  }

  /**
   * Create a new collection instance. The current implementation will instantiate classes
   * for various field types as follows:
   * <dl>
   *   <dt>Collection, List, AbstractList<dd>ArrayList
   *   <dt>Set,AbstractSet<dd>HashSet
   *   <dt>SortedSet<dd>TreeSet
   *   <dt>AbstractSequentialList<dd>LinkedList
   *   <dt>anything else<dd>assumed to be a concrete implementation and instantiated directly
   * </dl>
   * 
   * <p>Override this method in order to fine tune the instance creation.</p>
   *
   * @return an empty collection instance
   *
   * @throws OtmException on an error
   */
  protected Collection newInstance() throws OtmException {
    try {
      Class t = getType();

      if (t == Collection.class)
        return new ArrayList();

      if (t == List.class)
        return new ArrayList();

      if (t == Set.class)
        return new HashSet();

      if (t == SortedSet.class)
        return new TreeSet();

      if (t == AbstractList.class)
        return new ArrayList();

      if (t == AbstractSequentialList.class)
        return new LinkedList();

      if (t == AbstractSet.class)
        return new HashSet();

      return (Collection) t.newInstance();
    } catch (Exception e) {
      throw new OtmException("Can't instantiate " + getType(), e);
    }
  }
}
