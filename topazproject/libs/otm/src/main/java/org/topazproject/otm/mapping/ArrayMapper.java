package org.topazproject.otm.mapping;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.topazproject.otm.OtmException;

/**
 * Mapper for array type fileds.
 *
 * @author Pradeep Krishnan
 */
public class ArrayMapper extends AbstractMapper {
/**
   * Creates a new ArrayMapper object.
   *
   * @param uri the rdf predicate
   * @param field the java class field
   * @param getter the field get method or null
   * @param setter the field set method or null
   * @param serializer the serializer or null
   * @param componentType the array component type
   */
  public ArrayMapper(String uri, Field field, Method getter, Method setter, Serializer serializer,
                     Class componentType) {
    super(uri, field, getter, setter, serializer, componentType);
  }

  /**
   * Retrieve elements from an array field of an object.
   *
   * @param o the object
   *
   * @return the list of array elements (may be serialized)
   */
  public List get(Object o) throws OtmException {
    Object value = getRawValue(o, false);

    if (value == null)
      return Collections.emptyList();

    int  len = Array.getLength(value);
    List res = new ArrayList(len);

    for (int i = 0; i < len; i++) {
      Object v = Array.get(o, i);

      if (v != null)
        res.add(serialize(v));
    }

    return res;
  }

  /**
   * Populate an array field of an object.
   *
   * @param o the object
   * @param vals the values to be set (may be deserialized)
   */
  public void set(Object o, List vals) throws OtmException {
    Object value;

    if (vals.size() == 0)
      value = null; // xxx: this should be an option
    else {
      value = Array.newInstance(getComponentType(), vals.size());

      int i = 0;

      for (Object val : vals)
        Array.set(value, i++, deserialize(val));
    }

    setRawValue(o, value);
  }
}
