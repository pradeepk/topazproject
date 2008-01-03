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

import java.util.Collections;
import java.util.List;

import org.topazproject.otm.OtmException;
import org.topazproject.otm.mapping.Mapper;
import org.topazproject.otm.serializer.Serializer;

/**
 * A wrapping loader that projects fields of an embedded class on to the embedding class. This
 * makes it possible for treating embedded class fields the same way as regular class fields.
 *
 * @author Pradeep Krishnan
 */
public class EmbeddedClassMemberFieldLoader implements FieldLoader {
  private EmbeddedClassFieldLoader container;
  private FieldLoader              field;

  /**
   * Creates a new EmbeddedClassMemberFieldLoader object.
   *
   * @param container the mapper for the embedded class field in the embedding class
   * @param field the mapper for a field in the embedded class
   */
  public EmbeddedClassMemberFieldLoader(EmbeddedClassFieldLoader container, FieldLoader field) {
    this.container   = container;
    this.field       = field;
  }

  /** 
   * Get the mapper for the embedded class field in the embedding class
   * 
   * @return the mapper
   */
  public EmbeddedClassFieldLoader getContainer() {
    return container;
  }

  /** 
   * Get the mapper for the field in the embedded class
   * 
   * @return the mapper
   */
  public FieldLoader getFieldLoader() {
    return field;
  }

  /*
   * inherited javadoc
   */
  public Object getRawValue(Object o, boolean create) throws OtmException {
    Object co = container.getRawValue(o, create);

    return (co == null) ? null : field.getRawValue(co, create);
  }

  /*
   * inherited javadoc
   */
  public void setRawValue(Object o, Object value) throws OtmException {
    field.setRawValue(container.getRawValue(o, true), value);
  }

  /*
   * inherited javadoc
   */
  public List get(Object o) throws OtmException {
    Object co = container.getRawValue(o, false);

    return (co == null) ? Collections.emptyList() : field.get(co);
  }

  /*
   * inherited javadoc
   */
  public void set(Object o, List vals) throws OtmException {
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
  public boolean typeIsUri(Mapper mapper) {
    return field.typeIsUri(mapper);
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
  public Serializer getSerializer() {
    return field.getSerializer();
  }

  /**
   * Run a value through the serializer. If no serializer is defined, the value is returned
  /*
   * inherited javadoc
   */
  public String toString() {
    return "EmbeddedClassMemberFieldLoader[container=" + container + ", field=" + field + "]";
  }
}
