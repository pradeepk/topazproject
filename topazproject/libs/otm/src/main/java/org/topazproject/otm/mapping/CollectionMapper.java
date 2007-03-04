package org.topazproject.otm.mapping;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Mapper for {@link java.util.Collection collection} fields.
 *
 * @author Pradeep Krishnan
 */
public class CollectionMapper extends AbstractMapper {
/**
   * Creates a new CollectionMapper object.
   *
   * @param uri the rdf predicate
   * @param field the java class field
   * @param getter the field get method or null
   * @param setter the field set method or null
   * @param serializer the serializer or null
   * @param componentType the collection component type
   */
  public CollectionMapper(String uri, Field field, Method getter, Method setter,
                          Serializer serializer, Class componentType) {
    super(uri, field, getter, setter, serializer, componentType);
  }

  /**
   * Retrieve elements from a collection field of an object.
   *
   * @param o the object
   *
   * @return the list of array elements (may be serialized)
   */
  public List get(Object o) {
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
   */
  public void set(Object o, List vals) {
    Collection value  = (Collection) getRawValue(o, false);

    boolean    create = (value == null);

    if (create)
      value = newInstance();

    value.clear();

    for (Object v : vals)
      value.add(deserialize(v));

    if (create)
      setRawValue(o, value);
  }

  private Collection newInstance() {
    try {
      // xxx: handle interfaces and abstract collections
      return (Collection) getType().newInstance();
    } catch (Exception e) {
      throw new RuntimeException("Can't instantiate " + getType(), e);
    }
  }
}
