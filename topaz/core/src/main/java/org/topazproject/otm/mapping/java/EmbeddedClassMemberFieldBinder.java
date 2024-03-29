/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007-2008 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.topazproject.otm.mapping.java;

import java.lang.reflect.Method;

import java.util.Collections;
import java.util.List;

import org.topazproject.otm.OtmException;
import org.topazproject.otm.Session;
import org.topazproject.otm.mapping.RdfMapper;
import org.topazproject.otm.serializer.Serializer;

/**
 * A wrapping loader that projects fields of an embedded class on to the embedding class. This
 * makes it possible for treating embedded class fields the same way as regular class fields.
 *
 * @author Pradeep Krishnan
 */
class EmbeddedClassMemberFieldBinder implements FieldBinder {
  private EmbeddedClassFieldBinder container;
  private FieldBinder              field;

  /**
   * Creates a new EmbeddedClassMemberFieldBinder object.
   *
   * @param container the mapper for the embedded class field in the embedding class
   * @param field the mapper for a field in the embedded class
   */
  public EmbeddedClassMemberFieldBinder(EmbeddedClassFieldBinder container, FieldBinder field) {
    this.container   = container;
    this.field       = field;
  }

  /**
   * Get the mapper for the embedded class field in the embedding class
   *
   * @return the mapper
   */
  public EmbeddedClassFieldBinder getContainer() {
    return container;
  }

  /**
   * Get the mapper for the field in the embedded class
   *
   * @return the mapper
   */
  public FieldBinder getFieldBinder() {
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
  public void load(Object instance, List<String> values,
                   RdfMapper mapper, Session session) throws OtmException {
    load(instance, instance, values, mapper, session);
  }

  /*
   * inherited javadoc
   */
  public void load(Object root, Object instance, List<String> values,
                   RdfMapper mapper, Session session)
            throws OtmException {
    field.load(root, container.getRawValue(instance, true), values, mapper, session);
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
  public Class getComponentType() {
    return field.getComponentType();
  }

  /*
   * inherited javadoc
   */
  public Serializer getSerializer() {
    return field.getSerializer();
  }

  public Streamer getStreamer() {
    return field.getStreamer();
  }

  /*
   * inherited javadoc
   */
  public boolean isLoaded(Object o) throws OtmException {
    Object co = container.getRawValue(o, false);

    return (co == null) ? false : field.isLoaded(co);
  }

  /*
   * inherited javadoc
   */
  public RawFieldData getRawFieldData(Object o) throws OtmException {
    Object co = container.getRawValue(o, false);

    return (co == null) ? null : field.getRawFieldData(co);
  }

  /*
   * inherited javadoc
   */
  public String toString() {
    return "EmbeddedClassMemberFieldBinder[container=" + container + ", field=" + field + "]";
  }
}
