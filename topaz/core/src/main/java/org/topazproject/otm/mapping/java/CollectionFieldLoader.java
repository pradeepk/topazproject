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
import java.lang.reflect.Modifier;

import java.util.AbstractList;
import java.util.AbstractSequentialList;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.topazproject.otm.OtmException;
import org.topazproject.otm.id.IdentifierGenerator;
import org.topazproject.otm.serializer.Serializer;

/**
 * FieldLoader for {@link java.util.Collection collection} fields.
 *
 * @author Pradeep Krishnan
 */
public class CollectionFieldLoader extends AbstractFieldLoader {
  /**
   * Creates a new CollectionFieldLoader object.
   *
   * @param field the java class field
   * @param getter the field get method or null
   * @param setter the field set method or null
   * @param serializer the serializer or null
   * @param componentType the collection component type
   */
  public CollectionFieldLoader(Field field, Method getter, Method setter,
                          Serializer serializer, Class componentType) {
    super(field, getter, setter, serializer, componentType);
  }

  /**
   * Retrieve elements from a collection field of an object.
   *
   * @param o the object
   *
   * @return the list of array elements (may be serialized)
   *
   * @throws OtmException if a field's value cannot be retrieved and serialized
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
   * Populate a collection field of an object.
   *
   * @param o the object
   * @param vals the values to be set (may be deserialized)
   *
   * @throws OtmException if a field's value cannot be de-serialized and set
   */
  public void set(Object o, List vals) throws OtmException {
    Collection value = null;
    if (getGetter() != null || getField().isAccessible() ||
        Modifier.isPublic(getField().getModifiers()))
      value = (Collection) getRawValue(o, false);

    boolean    create = (value == null);

    if (create)
      value = newInstance();

    value.clear();

    for (Object v : vals)
      value.add(deserialize(v));

    if (create)
      setRawValue(o, value);
  }

  /**
   * Create a new collection instance. The current implementation will instantiate classes
   * for various field types as follows:<dl><dt>Collection, List, AbstractList<dd>ArrayList<dt>Set,
   * AbstractSet<dd>HashSet<dt>SortedSet<dd>TreeSet<dt>AbstractSequentialList<dd>LinkedList<dt>anything
   * else<dd>assumed to be a concrete implementation and instantiated directly</dl><p>Override
   * this method in order to fine tune the instance creation.</p>
   *
   * @return an empty collection instance
   *
   * @throws OtmException DOCUMENT ME!
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