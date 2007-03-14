package org.topazproject.otm.mapping;


import java.lang.reflect.Field;
import java.lang.reflect.Method;

import java.net.URI;
import java.net.URL;

import java.util.List;

import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.annotations.Inverse;
import org.topazproject.otm.annotations.Model;

/**
 * A convenient base class for all mappers.
 *
 * @author Pradeep krishnan
 */
public abstract class AbstractMapper implements Mapper {
  private Serializer serializer;
  private Method     getter;
  private Method     setter;
  private Field      field;
  private String     name;
  private String     uri;
  private Class      type;
  private Class      componentType;
  private boolean    inverse;
  private String     inverseModel;
  private boolean    inverseModelInitialized;

/**
   * Creates a new AbstractMapper object.
   *
   * @param uri the rdf predicate
   * @param field the java class field
   * @param getter the field get method or null
   * @param setter the field set method or null
   * @param serializer the serializer or null
   * @param componentType of arrays and collections or type of functional properties
   */
  public AbstractMapper(String uri, Field field, Method getter, Method setter,
                        Serializer serializer, Class componentType) {
    this.uri                  = uri;
    this.field                = field;
    this.getter               = getter;
    this.setter               = setter;
    this.serializer           = serializer;
    this.name                 = field.getName();
    this.type                 = field.getType();
    this.componentType        = componentType;
    inverse                   = field.getAnnotation(Inverse.class) != null;
    inverseModel              = null;
    inverseModelInitialized   = false;
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
  public String getUri() {
    return uri;
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
  public boolean typeIsUri() {
    Class clazz = getComponentType();

    return URI.class.isAssignableFrom(clazz) || URL.class.isAssignableFrom(clazz)
            || (getSerializer() == null)
            || (String.class.isAssignableFrom(clazz) && hasInverseUri());
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

  /*
   * inherited javadoc
   */
  public boolean hasInverseUri() {
    return inverse;
  }

  /*
   * inherited javadoc
   */
  public String getInverseModel() {
    if (!inverseModelInitialized) {
      if (inverse) {
        if (serializer != null) {
          Model m = (Model) field.getAnnotation(Model.class);

          if (m != null)
            inverseModel = m.value();
        } else {
          try {
            ClassMetadata cm = new ClassMetadata(componentType);
            inverseModel = cm.getModel();
          } catch (OtmException oe) {
            throw new Error("Unexpected error creating class-metadata for " + componentType, oe);
          }
        }
      }

      inverseModelInitialized = true;
    }

    return inverseModel;
  }

  /**
   * Run a value throug the serializer. If no serializer is defined, the value is returned as
   * is.
   *
   * @param o the object to serialize.
   *
   * @return the returned value
   *
   * @throws OtmException DOCUMENT ME!
   */
  protected Object serialize(Object o) throws OtmException {
    try {
      return (serializer != null) ? serializer.serialize(o) : o;
    } catch (Exception e) {
      throw new OtmException("Serialization error", e);
    }
  }

  /**
   * Run a value throug the serializer. If no serializer is defined, the value is returned as
   * is.
   *
   * @param o the object to serialize.
   *
   * @return the returned value
   *
   * @throws OtmException DOCUMENT ME!
   */
  protected Object deserialize(Object o) throws OtmException {
    try {
      return (serializer != null) ? serializer.deserialize((String) o) : o;
    } catch (Exception e) {
      throw new OtmException("Deserialization error", e);
    }
  }

  public String toString() {
    return getClass().getName() + "[field=" + name + ", pred=" + uri + ", type=" +
           (type != null ? type.getName() : "-null-") + ", componentType=" +
           (componentType != null ? componentType.getName() : "-null-") + ", inverse=" + inverse +
           ", serializer=" + serializer + "]";
  }
}
