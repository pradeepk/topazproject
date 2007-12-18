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

import java.net.URI;
import java.net.URL;

import org.topazproject.otm.OtmException;
import org.topazproject.otm.Rdf;
import org.topazproject.otm.id.IdentifierGenerator;
import org.topazproject.otm.mapping.Mapper;
import org.topazproject.otm.serializer.Serializer;

/**
 * A convenient base class for all mappers.
 *
 * @author Pradeep krishnan
 */
public abstract class AbstractFieldLoader implements FieldLoader {
  private final Serializer          serializer;
  private final Method              getter;
  private final Method              setter;
  private final Field               field;
  private final String              name;
  private final Class               type;
  private final Class               componentType;

  /**
   * Creates a new AbstractMapper object for a regular class.
   *
   * @param field the java class field
   * @param getter the field get method or null
   * @param setter the field set method or null
   * @param serializer the serializer or null
   * @param componentType of arrays and collections or type of functional properties
   */
  public AbstractFieldLoader(Field field, Method getter, Method setter,
                        Serializer serializer, Class componentType) {
    this.field           = field;
    this.getter          = getter;
    this.setter          = setter;
    this.serializer      = serializer;
    this.name            = field.getName();
    this.type            = field.getType();
    this.componentType   = componentType;
  }

  /*
   * inherited javadoc
   */
  public Object getRawValue(Object o, boolean create) throws OtmException {
    Object value;

    try {
      if (getter != null)
        value = getter.invoke(o);
      else
        value = field.get(o);

      if ((value == null) && create) {
        value = field.getType().newInstance();
        setRawValue(o, value);
      }
    } catch (Exception e) {
      throw new OtmException("Failed to get a value from '" + field.toGenericString() + "'", e);
    }

    return value;
  }

  /*
   * inherited javadoc
   */
  public void setRawValue(Object o, Object value) throws OtmException {
    try {
      if (setter != null)
        setter.invoke(o, value);
      else
        field.set(o, value);
    } catch (Exception e) {
      if ((value == null) && type.isPrimitive())
        setRawValue(o, 0);
      else
        throw new OtmException("Failed to set a value for '" + field.toGenericString() + "'", e);
    }
  }


  /*
   * inherited javadoc
   */
  public Method getGetter() {
    return getter;
  }

  /*
   * inherited javadoc
   */
  public Method getSetter() {
    return setter;
  }

  /*
   * inherited javadoc
   */
  public Field getField() {
    return field;
  }

  /*
   * inherited javadoc
   */
  public String getName() {
    return name;
  }

  /*
   * inherited javadoc
   */
  public Class getType() {
    return type;
  }

  /*
   * inherited javadoc
   */
  public boolean typeIsUri(Mapper mapper) {
    Class clazz = getComponentType();

    return URI.class.isAssignableFrom(clazz) || URL.class.isAssignableFrom(clazz)
            || mapper.isAssociation()
            || (String.class.isAssignableFrom(clazz) && mapper.hasInverseUri())
            || (Rdf.xsd + "anyURI").equals(mapper.getDataType());
  }

  /*
   * inherited javadoc
   */
  public Class getComponentType() {
    return componentType;
  }

  /*
   * inherited javadoc
   */
  public Serializer getSerializer() {
    return serializer;
  }


  /**
   * Run a value through the serializer. If no serializer is defined, the value is returned
   * as is.
   *
   * @param o the object to serialize.
   *
   * @return the returned value
   *
   * @throws OtmException on an error in serialize
   */
  protected Object serialize(Object o) throws OtmException {
    try {
      return (serializer != null) ? serializer.serialize(o) : o;
    } catch (Exception e) {
      throw new OtmException("Serialization error", e);
    }
  }

  /**
   * Run a value through the serializer. If no serializer is defined, the value is returned
   * as is.
   *
   * @param o the object to serialize.
   *
   * @return the returned value
   *
   * @throws OtmException on an error in serialize
   */
  protected Object deserialize(Object o) throws OtmException {
    try {
      return (serializer != null) ? serializer.deserialize((String) o, getComponentType()) : o;
    } catch (Exception e) {
      throw new OtmException("Deserialization error on " + toString(), e);
    }
  }

  /*
   * inherited javadoc
   */
  public String toString() {
    return getClass().getName() + "[field=" + name + ", type="
           + ((type != null) ? type.getName() : "-null-") + ", componentType="
           + ((componentType != null) ? componentType.getName() : "-null-")
           + ", serializer=" + serializer + "]";
  }
}
