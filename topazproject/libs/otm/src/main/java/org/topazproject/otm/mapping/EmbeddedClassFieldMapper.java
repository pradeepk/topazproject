package org.topazproject.otm.mapping;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A wrapping mapper that projects fields of an embedded class on to the embedding class. This
 * makes it possible for treating embedded class fields the same way as regular class fields.
 *
 * @author Pradeep Krishnan
 */
public class EmbeddedClassFieldMapper implements Mapper {
  private Mapper container;
  private Mapper field;

/**
   * Creates a new EmbeddedClassFieldMapper object.
   *
   * @param container the mapper for the embedded class field in the embedding class
   * @param field the mapper for a field in the embedded class
   */
  public EmbeddedClassFieldMapper(Mapper container, Mapper field) {
    this.container   = container;
    this.field       = field;
  }

  /*
   * inherited javadoc
   */
  public Object getRawValue(Object o, boolean create) {
    Object co = container.getRawValue(o, create);

    return (co == null) ? null : field.getRawValue(co, create);
  }

  /*
   * inherited javadoc
   */
  public void setRawValue(Object o, Object value) {
    field.setRawValue(container.getRawValue(o, true), value);
  }

  /*
   * inherited javadoc
   */
  public List get(Object o) {
    Object co = container.getRawValue(o, false);

    return (co == null) ? Collections.emptyList() : field.get(co);
  }

  /*
   * inherited javadoc
   */
  public void set(Object o, List vals) {
    field.set(container.getRawValue(o, true), vals);
  }

  /*
   * inherited javadoc
   */
  public Method getGetter() {
    return field.getGetter();
  }

  /*
   * inherited javadoc
   */
  public Method getSetter() {
    return field.getSetter();
  }

  /*
   * inherited javadoc
   */
  public Field getField() {
    return field.getField();
  }

  /*
   * inherited javadoc
   */
  public String getName() {
    return container.getName() + "." + field.getName();
  }

  /*
   * inherited javadoc
   */
  public Class getType() {
    return field.getType();
  }

  /*
   * inherited javadoc
   */
  public boolean typeIsUri() {
    return field.typeIsUri();
  }

  /*
   * inherited javadoc
   */
  public Class getComponentType() {
    return field.getComponentType();
  }

  /*
   * inherited javadoc
   */
  public String getUri() {
    return field.getUri();
  }

  /*
   * inherited javadoc
   */
  public Serializer getSerializer() {
    return field.getSerializer();
  }

  /*
   * inherited javadoc
   */
  public boolean hasInverseUri() {
    return field.hasInverseUri();
  }

  /*
   * inherited javadoc
   */
  public String getInverseModel() {
    return field.getInverseModel();
  }
}
