package org.topazproject.otm.mapping;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import java.util.List;

import org.topazproject.otm.ClassMetadata;
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
    this.type                 = field.getType();
    this.componentType        = componentType;
    inverse                   = field.getAnnotation(Inverse.class) != null;
    inverseModel              = null;
    inverseModelInitialized   = false;
  }

  /*
   * inherited javadoc
   */
  public Object getRawValue(Object o, boolean create) {
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
      throw new RuntimeException("Failed to get a value from '" + field.toGenericString() + "'", e);
    }

    return value;
  }

  /*
   * inherited javadoc
   */
  public void setRawValue(Object o, Object value) {
    try {
      if (setter != null)
        setter.invoke(o, value);
      else
        field.set(o, value);
    } catch (Exception e) {
      if ((value == null) && type.isPrimitive())
        setRawValue(o, 0);
      else
        throw new RuntimeException("Failed to set a value for '" + field.toGenericString() + "'", e);
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
  public Class getType() {
    return type;
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
          ClassMetadata cm = new ClassMetadata(componentType);
          inverseModel = cm.getModel();
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
   * @throws RuntimeException DOCUMENT ME!
   */
  protected Object serialize(Object o) {
    try {
      return (serializer != null) ? serializer.serialize(o) : o;
    } catch (Exception e) {
      throw new RuntimeException("Serialization error", e);
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
   * @throws RuntimeException DOCUMENT ME!
   */
  protected Object deserialize(Object o) {
    try {
      return (serializer != null) ? serializer.deserialize((String) o) : o;
    } catch (Exception e) {
      throw new RuntimeException("Deserialization error", e);
    }
  }
}
