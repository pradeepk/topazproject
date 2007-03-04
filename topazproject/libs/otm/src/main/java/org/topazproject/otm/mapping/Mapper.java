package org.topazproject.otm.mapping;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import java.util.List;

/**
 * Mapper for a java class field to rdf triples having a specific predicate.
 *
 * @author Pradeep Krishnan
 */
public interface Mapper {
  /**
   * Get a value from a field of an object.
   *
   * @param o the object
   *
   * @return the list containing the field's values (may be serialized)
   */
  public List get(Object o);

  /**
   * Set a value for an object field.
   *
   * @param o the object
   * @param vals the list of values to set (may be deserialized)
   */
  public void set(Object o, List vals);

  /**
   * Get the raw object field value.
   *
   * @param o the object
   * @param create whether to create an instance
   *
   * @return the raw field value
   */
  public Object getRawValue(Object o, boolean create);

  /**
   * Set the raw object field value
   *
   * @param o the object
   * @param value the value to set
   */
  public void setRawValue(Object o, Object value);

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
   * Gets the underlying object field.
   *
   * @return the filed
   */
  public Field getField();

  /**
   * Gets the type of the object field.
   *
   * @return the field type
   */
  public Class getType();

  /**
   * Gets the component type of this object field.
   *
   * @return component type for arrays; member type for collections; or same as type for simple
   *         fields
   */
  public Class getComponentType();

  /**
   * Gets the rdf predicate uri. All fields other than an 'Id' field must have a uri.
   *
   * @return the rdf predicate uri
   */
  public String getUri();

  /**
   * Gets the serializer used. Note that there won't be any serializer set up for
   * associations.
   *
   * @return the serializer or null
   */
  public Serializer getSerializer();

  /**
   * Tests if the predicate uri represents an inverse.
   *
   * @return
   */
  public boolean hasInverseUri();

  /**
   * Gets the model where the other end of the 'inverse' related object is persisted.
   *
   * @return the inverse association's model
   */
  public String getInverseModel();
}
