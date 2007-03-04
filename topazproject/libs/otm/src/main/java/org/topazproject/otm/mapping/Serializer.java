package org.topazproject.otm.mapping;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import java.util.List;

/**
 * A type converter to/from triple store value.
 *
 * @author Pradeep Krishnan
  */
public interface Serializer<T> {
  /**
   * Convert to triple store value.
   *
   * @param o the object to serialize
   *
   * @return the triple store value as a String
   *
   * @throws Exception on a conversion error
   */
  public String serialize(T o) throws Exception;

  /**
   * Convert from a triple store value
   *
   * @param o the triple store value as a String
   *
   * @return the java object
   *
   * @throws Exception on a conversion error
   */
  public T deserialize(String o) throws Exception;
}
