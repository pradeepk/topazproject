package org.topazproject.otm.mapping;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Mapper for a functional property field.
 *
 * @author Pradeep Krishnan
 */
public class FunctionalMapper extends AbstractMapper {
/**
   * Creates a new FunctionalMapper object.
   *
   * @param uri the rdf predicate
   * @param field the java class field
   * @param getter the field get method or null
   * @param setter the field set method or null
   * @param serializer the serializer or null
   */
  public FunctionalMapper(String uri, Field field, Method getter, Method setter,
                          Serializer serializer) {
    super(uri, field, getter, setter, serializer, field.getType());
  }

  /**
   * Get the value of a field of an object.
   *
   * @param o the object
   *
   * @return a singelton or empty list (may be serialized)
   */
  public List get(Object o) {
    Object value = getRawValue(o, false);

    return (value == null) ? Collections.emptyList() : Collections.singletonList(serialize(value));
  }

  /**
   * Set the value of a field of an object.
   *
   * @param o the object
   * @param vals a singelton or empty list (may be deserialized)
   *
   * @throws RuntimeException if too many values to set
   */
  public void set(Object o, List vals) {
    int size = vals.size();

    if (size > 1) // xxx: should be optional
      throw new RuntimeException("Too many values for '" + getField().toGenericString() + "'");

    Object value = (size == 0) ? null : deserialize(vals.get(0));
    setRawValue(o, value);
  }
}
