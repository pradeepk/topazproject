/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.otm.criterion;

/**
 * A base class for all binary operations involving a field and its value. Note that to support
 * persisted Criterions, a serialized value must be set. This is because without knowing the
 * Serializer used by the field, there is no way for us to Serialize/Deserialize a value. A
 * serializer is only available during query generation. So during the query generation,  if a
 * field value is supplied that will be serialized and used instead of the already serialized
 * value.
 *
 * @author Pradeep Krishnan
 *
 * @see #getValue
 */
public abstract class AbstractBinaryCriterion extends Criterion {
  private String           fieldName;
  private String           serializedValue;
  private transient Object value;

  /**
   * Creates a new AbstractBinaryCriterion object.
   */
  public AbstractBinaryCriterion() {
  }

  /**
   * Creates a new AbstractBinaryCriterion object.
   *
   * @param name field/predicate name
   * @param value field/predicate value
   */
  public AbstractBinaryCriterion(String name, Object value) {
    this.fieldName   = name;
    this.value       = value;
  }

  /**
   * Get serializedValue.
   *
   * @return serializedValue as String.
   */
  public String getSerializedValue() {
    return serializedValue;
  }

  /**
   * Set serializedValue.
   *
   * @param serializedValue the value to set.
   */
  public void setSerializedValue(String serializedValue) {
    this.serializedValue = serializedValue;
  }

  /**
   * Gets the value to use in operations. Falls back to serializedValue if not set.
   *
   * @return value as Object.
   */
  public Object getValue() {
    return (value != null) ? value : serializedValue;
  }

  /**
   * Set value.
   *
   * @param value the value to set.
   */
  public void setValue(Object value) {
    this.value = value;
  }

  /**
   * Get fieldName.
   *
   * @return fieldName as String.
   */
  public String getFieldName() {
    return fieldName;
  }

  /**
   * Set fieldName.
   *
   * @param fieldName the value to set.
   */
  public void setFieldName(String fieldName) {
    this.fieldName = fieldName;
  }
}
